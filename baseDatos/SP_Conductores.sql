-- Seleccionar la base transandina en SSMS antes de ejecutar.
/*
 TransAndina - consultas para las 8 pantallas entregadas.
 SQL Server 2016 SP1+; ejecutar en la BD que contiene las tablas, esquema dbo.
 No ejecutar de nuevo crearTablasTransandina.sql: contiene DROP TABLE.
 CREATE OR ALTER reemplaza procedimientos homonimos: revisar antes de instalar.
 @IdConductor / @IdUsuario deben proceder de la identidad autenticada del backend.
 Consultas de vehiculos: solamente asignaciones actuales de usuarios activos.
 Notificaciones: solamente destinatarios en usuarioxnotificacion.
 Los SELECT de los procedimientos tienen alias camelCase para los DTO Kotlin.
 Ver ANALISIS_Y_CONTRATO.md para nulos, decimales, fechas y reglas de negocio.
*/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ValidarAccesoVehiculoConductor
    @IdConductor INT,
    @Placa NVARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    IF @IdConductor IS NULL OR NULLIF(LTRIM(RTRIM(@Placa)), N'') IS NULL
        THROW 51000, N'Conductor y placa son obligatorios.', 1;
    IF NOT EXISTS (
        SELECT 1 FROM dbo.usuario u
        JOIN dbo.vehiculoxconductor a ON a.idConductor = u.idUsuario
        WHERE u.idUsuario = @IdConductor AND u.estado = N'activo'
          AND a.placa = @Placa AND a.esActual = 1 AND a.fechaDesasignacion IS NULL
          AND a.fechaAsignacion <= GETDATE()
    )
        THROW 51001, N'Usuario inactivo o vehiculo no asignado actualmente.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerVehiculosConductor
    @IdConductor INT
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM dbo.usuario WHERE idUsuario = @IdConductor AND estado = N'activo')
        THROW 51002, N'Usuario inexistente o inactivo.', 1;
    SELECT v.placa, CONCAT(v.marca, N' ', v.modelo, N' ', v.anio) AS nombreVehiculo,
           v.marca, v.modelo, v.anio, t.nombreTipo AS tipoVehiculo,
           v.capacidad, v.kilometrajeActual, v.estadoOperativo,
           v.vencimientoRtv, v.vencimientoMarchamo, v.vencimientoSeguro
    FROM dbo.vehiculo v
    JOIN dbo.tipoVehiculo t ON t.idTipoVehiculo = v.idTipoVehiculo
    WHERE EXISTS (SELECT 1 FROM dbo.vehiculoxconductor a
                  WHERE a.placa = v.placa AND a.idConductor = @IdConductor
                    AND a.esActual = 1 AND a.fechaDesasignacion IS NULL
                    AND a.fechaAsignacion <= GETDATE())
    ORDER BY v.placa;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerVehiculoPorPlaca
    @Placa NVARCHAR(20), @IdConductor INT
AS
BEGIN
    SET NOCOUNT ON;
    EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdConductor, @Placa;
    SELECT placa, CONCAT(marca, N' ', modelo, N' ', anio) AS nombreVehiculo,
           kilometrajeActual
    FROM dbo.vehiculo WHERE placa = @Placa;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerCategoriasServicio
AS
BEGIN
    SET NOCOUNT ON;
    -- Todas: tambien hay trabajos correctivos sin frecuencia preventiva configurada.
    SELECT idCategoriaServicio, nombreCategoria, descripcion
    FROM dbo.categoriaServicio ORDER BY nombreCategoria;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerHistorialServicios
    @Placa NVARCHAR(20), @IdConductor INT,
    @DesfaseUtcMinutos SMALLINT = 0
AS
BEGIN
    SET NOCOUNT ON;
    EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdConductor, @Placa;
    IF @DesfaseUtcMinutos IS NULL OR @DesfaseUtcMinutos NOT BETWEEN -840 AND 840
        THROW 51003, N'Desfase UTC no valido.', 1;
    -- Una fila por mantenimiento, incluso si tiene varias categorias o evidencias.
    SELECT CONVERT(VARCHAR(11), m.idMantenimiento) AS id,
           CONVERT(CHAR(10), m.fechaMantenimiento, 103) AS fecha,
           DATEDIFF_BIG(MILLISECOND, CONVERT(DATETIME2, '19700101'),
               DATEADD(MINUTE, -@DesfaseUtcMinutos, CONVERT(DATETIME2, m.fechaMantenimiento))) AS fechaMillis,
           COALESCE(NULLIF(c.servicio, N''), m.tipoServicio) AS servicio,
           m.taller, m.costoTotal AS costo
    FROM dbo.mantenimiento m
    OUTER APPLY (
        SELECT STUFF((
            SELECT N', ' + d.nombreCategoria
            FROM (SELECT DISTINCT cs.nombreCategoria
                  FROM dbo.mantenimientoxcategoria mc
                  JOIN dbo.categoriaServicio cs ON cs.idCategoriaServicio = mc.idCategoriaServicio
                  WHERE mc.idMantenimiento = m.idMantenimiento) d
            ORDER BY d.nombreCategoria
            FOR XML PATH(''), TYPE
        ).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS servicio
    ) c
    WHERE m.placa = @Placa
    ORDER BY m.fechaMantenimiento DESC, m.idMantenimiento DESC;
END;
GO

/* Funcion auxiliar: estado por categoria a una fecha/odometro dados.
   No inventa una lectura inicial de cero si no existe mantenimiento previo.
   Frecuencias <= 0 se interpretan como dimension no configurada.
   Cualquier servicio de la categoria (preventivo o correctivo) reinicia su ciclo.
   El esquema no versiona frecuencias: las historicas se estiman con la actual.
*/
CREATE OR ALTER FUNCTION dbo.fn_EstadoMantenimientoConductor
    (@Placa NVARCHAR(20), @FechaCorte DATE, @Kilometraje INT)
RETURNS TABLE
AS RETURN
(
    SELECT cs.idCategoriaServicio, cs.nombreCategoria,
           f.frecuenciaKm, f.frecuenciaDias,
           m.idMantenimiento AS idUltimoMantenimiento,
           m.fechaMantenimiento AS fechaUltimoMantenimiento,
           m.kilometrajeServicio AS kilometrajeUltimoMantenimiento,
           CASE WHEN f.frecuenciaKm > 0 AND m.idMantenimiento IS NOT NULL
                     AND @Kilometraje >= m.kilometrajeServicio
                THEN CASE WHEN CONVERT(BIGINT, m.kilometrajeServicio) + f.frecuenciaKm <= @Kilometraje THEN 0
                          ELSE CONVERT(BIGINT, m.kilometrajeServicio) + f.frecuenciaKm - @Kilometraje END
           END AS kilometrajeRestante,
           CASE WHEN f.frecuenciaDias > 0 AND m.idMantenimiento IS NOT NULL
                THEN CONVERT(BIGINT, f.frecuenciaDias) - DATEDIFF(DAY, m.fechaMantenimiento, @FechaCorte)
           END AS diasRestantes
    FROM dbo.vehiculo v
    JOIN dbo.tipoVehiculoxcategoriaServicio f ON f.idTipoVehiculo = v.idTipoVehiculo
    JOIN dbo.categoriaServicio cs ON cs.idCategoriaServicio = f.idCategoriaServicio
    OUTER APPLY (
        SELECT TOP (1) mt.idMantenimiento, mt.fechaMantenimiento, mt.kilometrajeServicio
        FROM dbo.mantenimiento mt
        WHERE mt.placa = v.placa AND mt.fechaMantenimiento <= @FechaCorte
          AND EXISTS (SELECT 1 FROM dbo.mantenimientoxcategoria mc
                      WHERE mc.idMantenimiento = mt.idMantenimiento
                        AND mc.idCategoriaServicio = cs.idCategoriaServicio)
        ORDER BY mt.fechaMantenimiento DESC, mt.kilometrajeServicio DESC, mt.idMantenimiento DESC
    ) m
    WHERE v.placa = @Placa
);
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerEstadoMantenimientoVehiculo
    @Placa NVARCHAR(20), @IdConductor INT
AS
BEGIN
    SET NOCOUNT ON;
    EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdConductor, @Placa;
    IF EXISTS (SELECT 1 FROM dbo.tipoVehiculoxcategoriaServicio f
               JOIN dbo.vehiculo v ON v.idTipoVehiculo = f.idTipoVehiculo
               WHERE v.placa = @Placa GROUP BY f.idCategoriaServicio HAVING COUNT(*) > 1)
        THROW 51004, N'Hay frecuencias duplicadas para el tipo de vehiculo y categoria.', 1;
    DECLARE @Km INT = (SELECT kilometrajeActual FROM dbo.vehiculo WHERE placa = @Placa);
    SELECT e.*,
           CASE WHEN e.idUltimoMantenimiento IS NULL THEN N'sin_referencia'
                WHEN e.kilometrajeUltimoMantenimiento > @Km THEN N'datos_inconsistentes'
                WHEN e.kilometrajeRestante = 0 OR e.diasRestantes <= 0 THEN N'vencido'
                WHEN e.kilometrajeRestante IS NULL AND e.diasRestantes IS NULL THEN N'sin_frecuencia'
                ELSE N'vigente' END AS estado
    FROM dbo.fn_EstadoMantenimientoConductor(@Placa, CONVERT(DATE, GETDATE()), @Km) e
    ORDER BY e.nombreCategoria;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerHistorialKilometraje
    @Placa NVARCHAR(20), @IdConductor INT, @Meses INT = 6
AS
BEGIN
    SET NOCOUNT ON;
    EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdConductor, @Placa;
    IF @Meses IS NULL OR @Meses NOT BETWEEN 1 AND 120
        THROW 51005, N'Meses debe estar entre 1 y 120.', 1;
    IF EXISTS (SELECT 1 FROM dbo.tipoVehiculoxcategoriaServicio f
               JOIN dbo.vehiculo v ON v.idTipoVehiculo = f.idTipoVehiculo
               WHERE v.placa = @Placa GROUP BY f.idCategoriaServicio HAVING COUNT(*) > 1)
        THROW 51004, N'Hay frecuencias duplicadas para el tipo de vehiculo y categoria.', 1;
    DECLARE @Hoy DATE = CONVERT(DATE, GETDATE());
    DECLARE @Desde DATE = DATEADD(MONTH, 1 - @Meses, DATEFROMPARTS(YEAR(@Hoy), MONTH(@Hoy), 1));
    DECLARE @Km INT = (SELECT kilometrajeActual FROM dbo.vehiculo WHERE placa = @Placa);

    -- Calcular LAG ANTES del filtro mensual: incluye la lectura previa al rango.
    SELECT r.idRegistroKilometraje, r.fechaRegistro, r.kilometraje,
           CONVERT(BIGINT, r.kilometraje) - LAG(CONVERT(BIGINT, r.kilometraje))
               OVER (ORDER BY r.fechaRegistro, r.idRegistroKilometraje) AS recorrido
    INTO #Lecturas
    FROM dbo.registroKilometraje r
    WHERE r.placa = @Placa AND CONVERT(DATE, r.fechaRegistro) <= @Hoy;
    IF EXISTS (SELECT 1 FROM #Lecturas WHERE recorrido < 0 OR kilometraje < 0)
        THROW 51006, N'El historial contiene lecturas decrecientes o negativas. Corregir antes de graficar.', 1;
    IF @Km < 0 OR EXISTS (SELECT 1 FROM #Lecturas WHERE kilometraje > @Km)
        THROW 51007, N'El kilometraje del vehiculo es menor que una lectura registrada.', 1;

    -- RESULTADO 1: una fila, incluso sin lecturas.
    -- Total conocido entre primera y ultima lectura; no es la vida completa del vehiculo.
    SELECT @Placa AS placa,
           CONVERT(INT, COALESCE((SELECT SUM(recorrido) FROM #Lecturas), 0)) AS totalHistoricoRecorrido,
           CONVERT(INT, CASE WHEN COUNT(*) = 0 OR COUNT(kilometrajeRestante) <> COUNT(*) THEN NULL
                            ELSE MIN(kilometrajeRestante) END) AS kilometrajeRestanteMantenimiento
    FROM dbo.fn_EstadoMantenimientoConductor(@Placa, @Hoy, @Km)
    WHERE frecuenciaKm > 0;

    -- RESULTADO 2: filas para historial[]. Solo meses con lecturas.
    -- La primera lectura es una referencia: no equivale a distancia recorrida.
    ;WITH Mensual AS (
        SELECT DATEFROMPARTS(YEAR(fechaRegistro), MONTH(fechaRegistro), 1) AS mes,
               SUM(COALESCE(recorrido, 0)) AS recorrido,
               MAX(kilometraje) AS odometroCierre,
               MAX(CONVERT(DATE, fechaRegistro)) AS fechaCierre
        FROM #Lecturas WHERE fechaRegistro >= @Desde
        GROUP BY YEAR(fechaRegistro), MONTH(fechaRegistro)
    )
    SELECT CONVERT(CHAR(7), p.mes, 126) AS periodo,
           CONVERT(INT, p.recorrido) AS kilometrajeActual,
           CONVERT(INT, e.restante) AS kilometrajeRestante
    FROM Mensual p
    OUTER APPLY (
        SELECT CASE WHEN COUNT(*) = 0 OR COUNT(kilometrajeRestante) <> COUNT(*) THEN NULL
                    ELSE MIN(kilometrajeRestante) END AS restante
        FROM dbo.fn_EstadoMantenimientoConductor(@Placa, p.fechaCierre, p.odometroCierre)
        WHERE frecuenciaKm > 0
    ) e
    ORDER BY p.mes;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerNotificacionesVehiculo
    @IdUsuario INT, @Placa NVARCHAR(20) = NULL,
    @Tipo NVARCHAR(20) = NULL, @Leida BIT = NULL,
    @DesfaseUtcMinutos SMALLINT = 0
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM dbo.usuario WHERE idUsuario = @IdUsuario AND estado = N'activo')
        THROW 51002, N'Usuario inexistente o inactivo.', 1;
    IF @DesfaseUtcMinutos IS NULL OR @DesfaseUtcMinutos NOT BETWEEN -840 AND 840
        THROW 51003, N'Desfase UTC no valido.', 1;
    SET @Tipo = LOWER(NULLIF(LTRIM(RTRIM(@Tipo)), N''));
    IF @Tipo IS NOT NULL AND @Tipo NOT IN (N'general', N'importante', N'urgente')
        THROW 51008, N'Tipo debe ser GENERAL, IMPORTANTE o URGENTE, o NULL.', 1;
    -- Agrupar destinatarios evita duplicados permitidos por el esquema entregado.
    ;WITH Destinatario AS (
        SELECT idNotificacion, CONVERT(BIT, MIN(CONVERT(INT, leida))) AS leida
        FROM dbo.usuarioxnotificacion WHERE idUsuario = @IdUsuario GROUP BY idNotificacion
    )
    SELECT CONVERT(VARCHAR(11), n.idNotificacion) AS id, n.placa,
           UPPER(n.prioridad) AS tipo, n.titulo, n.mensaje AS descripcion,
           CONVERT(CHAR(10), n.fechaCreacion, 103) AS fecha,
           DATEDIFF_BIG(MILLISECOND, CONVERT(DATETIME2, '19700101'),
               DATEADD(MINUTE, -@DesfaseUtcMinutos, n.fechaCreacion)) AS fechaMillis,
           d.leida
    FROM dbo.notificacion n
    JOIN Destinatario d ON d.idNotificacion = n.idNotificacion
    WHERE (@Placa IS NULL OR n.placa = @Placa)
      AND (@Tipo IS NULL OR n.prioridad = @Tipo)
      AND (@Leida IS NULL OR d.leida = @Leida)
    ORDER BY n.fechaCreacion DESC, n.idNotificacion DESC;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerResumenInicioConductor
    @IdUsuario INT
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM dbo.usuario WHERE idUsuario = @IdUsuario AND estado = N'activo')
        THROW 51002, N'Usuario inexistente o inactivo.', 1;
    SELECT COUNT(DISTINCT un.idNotificacion) AS notificacionesNoLeidas
    FROM dbo.usuarioxnotificacion un
    WHERE un.idUsuario = @IdUsuario AND un.leida = 0;
END;
GO

/* Complemento de escritura para los botones Registrar y el estado leida.
   Instalar DESPUES de 01_consultas_conductor.sql. SQL Server 2016 SP1+,
   compatibilidad >= 130 para OPENJSON. No ejecuta registros al instalarse.
   Los procedimientos de registro deben invocarse fuera de otra transaccion.
   Las lecturas se serializan por vehiculo; otros escritores deben respetar el
   mismo bloqueo para preservar la monotonia. No incluye correccion de odometros.
*/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ValidarLecturaConductor
    @Placa NVARCHAR(20), @Fecha DATE, @KilometrajeActual INT
AS
BEGIN
    SET NOCOUNT ON;
    IF @Fecha IS NULL OR @Fecha < CONVERT(DATE, '19000101') OR @Fecha > CONVERT(DATE, GETDATE())
        THROW 51100, N'Fecha obligatoria, desde 1900 y no futura.', 1;
    IF @KilometrajeActual IS NULL OR @KilometrajeActual < 0
        THROW 51101, N'Kilometraje debe ser un entero no negativo.', 1;
    DECLARE @Actual INT = (SELECT kilometrajeActual FROM dbo.vehiculo WHERE placa = @Placa);
    IF @Actual IS NULL THROW 51102, N'Vehiculo inexistente.', 1;
    DECLARE @UltimaFecha DATE;
    SELECT @UltimaFecha = MAX(x.fecha)
    FROM (
        SELECT CONVERT(DATE, fechaRegistro) AS fecha FROM dbo.registroKilometraje WHERE placa = @Placa
        UNION ALL
        SELECT fechaMantenimiento FROM dbo.mantenimiento WHERE placa = @Placa
    ) x;
    IF EXISTS (
        SELECT 1 FROM (
            SELECT CONVERT(DATE, fechaRegistro) AS fecha, kilometraje AS km
            FROM dbo.registroKilometraje WHERE placa = @Placa
            UNION ALL
            SELECT fechaMantenimiento, kilometrajeServicio FROM dbo.mantenimiento WHERE placa = @Placa
        ) x
        WHERE (x.fecha < @Fecha AND x.km > @KilometrajeActual)
           OR (x.fecha > @Fecha AND x.km < @KilometrajeActual)
    ) THROW 51103, N'Kilometraje incompatible con lecturas anteriores o posteriores.', 1;
    IF (@UltimaFecha IS NULL OR @Fecha >= @UltimaFecha) AND @KilometrajeActual < @Actual
        THROW 51104, N'Una nueva lectura actual no puede reducir el odometro del vehiculo.', 1;
    IF @Fecha < @UltimaFecha AND @KilometrajeActual > @Actual
        THROW 51105, N'Una lectura historica no puede superar el odometro actual.', 1;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RegistrarKilometraje
    @Placa NVARCHAR(20), @Fecha DATE, @KilometrajeActual INT, @IdConductor INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF @@TRANCOUNT <> 0 THROW 51106, N'Invocar fuera de una transaccion existente.', 1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Bloqueo INT;
        SELECT @Bloqueo = kilometrajeActual FROM dbo.vehiculo WITH (UPDLOCK, HOLDLOCK) WHERE placa = @Placa;
        EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdConductor, @Placa;
        EXEC dbo.sp_ValidarLecturaConductor @Placa, @Fecha, @KilometrajeActual;
        -- Politica explicita: una lectura por placa y fecha seleccionada.
        -- El formulario no proporciona hora ni identificador de idempotencia.
        IF EXISTS (SELECT 1 FROM dbo.registroKilometraje
                   WHERE placa = @Placa AND CONVERT(DATE, fechaRegistro) = @Fecha)
            THROW 51107, N'Ya hay una lectura para este vehiculo y fecha.', 1;
        INSERT dbo.registroKilometraje (placa, idConductor, kilometraje, fechaRegistro)
        VALUES (@Placa, @IdConductor, @KilometrajeActual, CONVERT(DATETIME2, @Fecha));
        DECLARE @Id INT = CONVERT(INT, SCOPE_IDENTITY());
        UPDATE dbo.vehiculo
        SET kilometrajeActual = CASE WHEN kilometrajeActual < @KilometrajeActual THEN @KilometrajeActual ELSE kilometrajeActual END
        WHERE placa = @Placa;
        COMMIT;
        SELECT @Id AS idRegistroKilometraje, @Placa AS placa,
               CONVERT(CHAR(10), @Fecha, 23) AS fecha, @KilometrajeActual AS kilometrajeActual;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RegistrarMantenimiento
    @Placa NVARCHAR(20), @KilometrajeActual INT,
    @TipoServicio NVARCHAR(30), @Categoria NVARCHAR(100),
    @Fecha DATE, @Taller NVARCHAR(150), @Costo DECIMAL(12,2),
    @Descripcion NVARCHAR(MAX), @IdUsuarioRegistra INT,
    @ComprobantesJson NVARCHAR(MAX) = N'[]'
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF @@TRANCOUNT <> 0 THROW 51106, N'Invocar fuera de una transaccion existente.', 1;
    SET @TipoServicio = LOWER(LTRIM(RTRIM(@TipoServicio)));
    IF @TipoServicio IS NULL OR @TipoServicio NOT IN (N'preventivo', N'correctivo')
        THROW 51108, N'Tipo de servicio no valido.', 1;
    IF NULLIF(LTRIM(RTRIM(@Taller)), N'') IS NULL OR NULLIF(LTRIM(RTRIM(@Descripcion)), N'') IS NULL
        THROW 51109, N'Taller y descripcion son obligatorios.', 1;
    IF @Costo IS NULL OR @Costo < 0 THROW 51110, N'Costo debe ser no negativo.', 1;
    IF @ComprobantesJson IS NULL OR ISJSON(@ComprobantesJson) <> 1
        THROW 51111, N'Comprobantes debe ser un arreglo JSON valido.', 1;
    -- Se tolera whitespace JSON antes de '['.
    IF LEFT(LTRIM(REPLACE(REPLACE(REPLACE(@ComprobantesJson, CHAR(9), N' '), CHAR(10), N' '), CHAR(13), N' ')), 1) <> N'['
        THROW 51111, N'Comprobantes debe ser un arreglo JSON.', 1;
    IF EXISTS (SELECT 1 FROM OPENJSON(@ComprobantesJson) WHERE [type] <> 5)
        THROW 51112, N'Cada comprobante debe ser un objeto.', 1;
    DECLARE @Evidencias TABLE (
        urlArchivo NVARCHAR(MAX), nombreArchivo NVARCHAR(MAX),
        tipoEvidencia NVARCHAR(MAX), descripcion NVARCHAR(MAX)
    );
    INSERT @Evidencias
    SELECT urlArchivo, nombreArchivo, COALESCE(tipoEvidencia, N'foto'), descripcion
    FROM OPENJSON(@ComprobantesJson) WITH (
        urlArchivo NVARCHAR(MAX) '$.urlArchivo', nombreArchivo NVARCHAR(MAX) '$.nombreArchivo',
        tipoEvidencia NVARCHAR(MAX) '$.tipoEvidencia', descripcion NVARCHAR(MAX) '$.descripcion'
    );
    IF EXISTS (SELECT 1 FROM @Evidencias
               WHERE NULLIF(LTRIM(RTRIM(urlArchivo)), N'') IS NULL OR DATALENGTH(urlArchivo) > 1000
                  OR urlArchivo NOT LIKE N'https://%'
                  OR NULLIF(LTRIM(RTRIM(nombreArchivo)), N'') IS NULL OR DATALENGTH(nombreArchivo) > 510
                  OR tipoEvidencia NOT IN (N'foto', N'factura') OR DATALENGTH(descripcion) > 510)
        THROW 51113, N'Comprobante no valido: requiere URL HTTPS, nombre y tipo foto o factura.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Bloqueo INT, @IdCategoria INT;
        SELECT @Bloqueo = kilometrajeActual FROM dbo.vehiculo WITH (UPDLOCK, HOLDLOCK) WHERE placa = @Placa;
        EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdUsuarioRegistra, @Placa;
        EXEC dbo.sp_ValidarLecturaConductor @Placa, @Fecha, @KilometrajeActual;
        SELECT @IdCategoria = idCategoriaServicio FROM dbo.categoriaServicio
        WHERE nombreCategoria = LTRIM(RTRIM(@Categoria));
        IF @IdCategoria IS NULL THROW 51114, N'Categoria inexistente. Consultar el catalogo de la base de datos.', 1;
        INSERT dbo.mantenimiento (placa, idUsuarioRegistra, tipoServicio, taller,
            kilometrajeServicio, costoTotal, descripcion, fechaMantenimiento)
        VALUES (@Placa, @IdUsuarioRegistra, @TipoServicio, LTRIM(RTRIM(@Taller)),
            @KilometrajeActual, @Costo, @Descripcion, @Fecha);
        DECLARE @Id INT = CONVERT(INT, SCOPE_IDENTITY());
        INSERT dbo.mantenimientoxcategoria (idMantenimiento, idCategoriaServicio)
        VALUES (@Id, @IdCategoria);
        INSERT dbo.evidenciaMantenimiento (idMantenimiento, urlArchivo, nombreArchivo, tipoEvidencia, descripcion)
        SELECT @Id, urlArchivo, nombreArchivo, tipoEvidencia, descripcion FROM @Evidencias;
        UPDATE dbo.vehiculo
        SET kilometrajeActual = CASE WHEN kilometrajeActual < @KilometrajeActual THEN @KilometrajeActual ELSE kilometrajeActual END
        WHERE placa = @Placa;
        COMMIT;
        SELECT @Id AS idMantenimiento, @Placa AS placa,
               CONVERT(CHAR(10), @Fecha, 23) AS fecha, @Costo AS costo,
               (SELECT COUNT(*) FROM @Evidencias) AS cantidadComprobantes;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_MarcarNotificacionLeida
    @IdUsuario INT, @IdNotificacion INT
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM dbo.usuario WHERE idUsuario = @IdUsuario AND estado = N'activo')
        THROW 51002, N'Usuario inexistente o inactivo.', 1;
    UPDATE dbo.usuarioxnotificacion
    SET leida = 1, fechaLectura = COALESCE(fechaLectura, GETDATE())
    WHERE idUsuario = @IdUsuario AND idNotificacion = @IdNotificacion;
    IF @@ROWCOUNT = 0 THROW 51115, N'Notificacion no asignada al usuario.', 1;
    SELECT CONVERT(VARCHAR(11), @IdNotificacion) AS id, CONVERT(BIT, 1) AS leida;
END;
GO

/*
 Complemento para Android con conexion JDBC directa, sin servidor de archivos.
 Ejecutar DESPUES de 01_consultas_conductor.sql y 02_registros_conductor.sql.
 Agrega una tabla de bytes; NO elimina ni modifica las tablas existentes.
 Maximo: 5 imagenes, 5 MiB por archivo, 10 MiB en total.
 La cuenta que instala requiere CREATE TABLE/PROCEDURE y permisos de esquema.
 La app solo necesita EXECUTE de los procedimientos que utiliza.
*/
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO
IF OBJECT_ID(N'dbo.archivoEvidenciaConductor', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.archivoEvidenciaConductor (
        idEvidenciaMantenimiento INT NOT NULL PRIMARY KEY,
        tipoMime NVARCHAR(100) NOT NULL,
        contenido VARBINARY(MAX) NOT NULL,
        CONSTRAINT FK_archivoConductor_evidencia FOREIGN KEY (idEvidenciaMantenimiento)
            REFERENCES dbo.evidenciaMantenimiento(idEvidenciaMantenimiento) ON DELETE CASCADE,
        CONSTRAINT CK_archivoConductor_tamano CHECK (DATALENGTH(contenido) BETWEEN 1 AND 5242880)
    );
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_RegistrarMantenimientoConArchivos
    @Placa NVARCHAR(20), @KilometrajeActual INT,
    @TipoServicio NVARCHAR(30), @Categoria NVARCHAR(100),
    @Fecha DATE, @Taller NVARCHAR(150), @Costo DECIMAL(12,2),
    @Descripcion NVARCHAR(MAX), @IdUsuarioRegistra INT,
    @ArchivosJson NVARCHAR(MAX) = N'[]'
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF @@TRANCOUNT <> 0 THROW 51106, N'Invocar fuera de una transaccion existente.', 1;
    EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdUsuarioRegistra, @Placa;
    SET @TipoServicio = LOWER(LTRIM(RTRIM(@TipoServicio)));
    IF @TipoServicio IS NULL OR @TipoServicio NOT IN (N'preventivo', N'correctivo')
        THROW 51108, N'Tipo de servicio no valido.', 1;
    IF NULLIF(LTRIM(RTRIM(@Taller)), N'') IS NULL OR NULLIF(LTRIM(RTRIM(@Descripcion)), N'') IS NULL
        THROW 51109, N'Taller y descripcion son obligatorios.', 1;
    IF @Costo IS NULL OR @Costo < 0 THROW 51110, N'Costo no valido.', 1;
    IF @ArchivosJson IS NULL OR DATALENGTH(@ArchivosJson) > 31457280 OR ISJSON(@ArchivosJson) <> 1
        THROW 51300, N'Archivos JSON no validos o demasiado grandes.', 1;
    IF LEFT(LTRIM(REPLACE(REPLACE(REPLACE(@ArchivosJson, CHAR(9), N' '), CHAR(10), N' '), CHAR(13), N' ')), 1) <> N'['
        THROW 51300, N'Se requiere un arreglo de archivos.', 1;
    IF (SELECT COUNT(*) FROM OPENJSON(@ArchivosJson)) > 5
        THROW 51301, N'Maximo cinco comprobantes.', 1;
    IF EXISTS (SELECT 1 FROM OPENJSON(@ArchivosJson) WHERE [type] <> 5)
        THROW 51300, N'Cada archivo debe ser un objeto JSON.', 1;

    DECLARE @Entrada TABLE (
        nombreArchivo NVARCHAR(MAX), tipoMime NVARCHAR(MAX),
        tipoEvidencia NVARCHAR(MAX), contenidoBase64 NVARCHAR(MAX)
    );
    INSERT @Entrada
    SELECT nombreArchivo, tipoMime, COALESCE(tipoEvidencia, N'foto'), contenidoBase64
    FROM OPENJSON(@ArchivosJson) WITH (
        nombreArchivo NVARCHAR(MAX) '$.nombreArchivo',
        tipoMime NVARCHAR(MAX) '$.tipoMime',
        tipoEvidencia NVARCHAR(MAX) '$.tipoEvidencia',
        contenidoBase64 NVARCHAR(MAX) '$.contenidoBase64'
    );
    IF EXISTS (SELECT 1 FROM @Entrada
        WHERE NULLIF(LTRIM(RTRIM(nombreArchivo)), N'') IS NULL OR DATALENGTH(nombreArchivo) > 510
           OR tipoMime IS NULL OR tipoMime NOT LIKE N'image/%' OR DATALENGTH(tipoMime) > 200
           OR tipoEvidencia NOT IN (N'foto', N'factura')
           OR NULLIF(contenidoBase64, N'') IS NULL OR DATALENGTH(contenidoBase64) > 13981016)
        THROW 51302, N'Nombre, tipo o contenido de archivo no valido.', 1;

    DECLARE @Archivos TABLE (
        numero INT IDENTITY(1,1), nombreArchivo NVARCHAR(255), tipoMime NVARCHAR(100),
        tipoEvidencia NVARCHAR(50), contenido VARBINARY(MAX)
    );
    DECLARE @Xml XML = N'';
    BEGIN TRY
        INSERT @Archivos(nombreArchivo, tipoMime, tipoEvidencia, contenido)
        SELECT j.nombreArchivo, j.tipoMime, j.tipoEvidencia,
               @Xml.value('xs:base64Binary(sql:column("j.contenidoBase64"))', 'VARBINARY(MAX)')
        FROM @Entrada j;
    END TRY
    BEGIN CATCH
        THROW 51303, N'Contenido Base64 no valido.', 1;
    END CATCH;
    IF EXISTS (SELECT 1 FROM @Archivos WHERE contenido IS NULL OR DATALENGTH(contenido) NOT BETWEEN 1 AND 5242880)
       OR (SELECT COALESCE(SUM(DATALENGTH(contenido)), 0) FROM @Archivos) > 10485760
        THROW 51302, N'Limite: 5 MiB por archivo y 10 MiB en total.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Bloqueo INT, @IdCategoria INT;
        SELECT @Bloqueo = kilometrajeActual FROM dbo.vehiculo WITH (UPDLOCK, HOLDLOCK) WHERE placa = @Placa;
        EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdUsuarioRegistra, @Placa;
        EXEC dbo.sp_ValidarLecturaConductor @Placa, @Fecha, @KilometrajeActual;
        SELECT @IdCategoria = idCategoriaServicio FROM dbo.categoriaServicio
        WHERE nombreCategoria = LTRIM(RTRIM(@Categoria));
        IF @IdCategoria IS NULL THROW 51114, N'Categoria inexistente.', 1;

        INSERT dbo.mantenimiento(placa, idUsuarioRegistra, tipoServicio, taller,
            kilometrajeServicio, costoTotal, descripcion, fechaMantenimiento)
        VALUES (@Placa, @IdUsuarioRegistra, @TipoServicio, LTRIM(RTRIM(@Taller)),
            @KilometrajeActual, @Costo, @Descripcion, @Fecha);
        DECLARE @Id INT = CONVERT(INT, SCOPE_IDENTITY());
        INSERT dbo.mantenimientoxcategoria(idMantenimiento, idCategoriaServicio) VALUES (@Id, @IdCategoria);

        DECLARE @Numero INT = 1, @Cantidad INT = (SELECT COUNT(*) FROM @Archivos), @IdEvidencia INT;
        WHILE @Numero <= @Cantidad
        BEGIN
            INSERT dbo.evidenciaMantenimiento(idMantenimiento, urlArchivo, nombreArchivo, tipoEvidencia)
            SELECT @Id, N'sql://pendiente', nombreArchivo, tipoEvidencia FROM @Archivos WHERE numero = @Numero;
            SET @IdEvidencia = CONVERT(INT, SCOPE_IDENTITY());
            UPDATE dbo.evidenciaMantenimiento
            SET urlArchivo = CONCAT(N'sql://evidencia/', @IdEvidencia)
            WHERE idEvidenciaMantenimiento = @IdEvidencia;
            INSERT dbo.archivoEvidenciaConductor(idEvidenciaMantenimiento, tipoMime, contenido)
            SELECT @IdEvidencia, tipoMime, contenido FROM @Archivos WHERE numero = @Numero;
            SET @Numero += 1;
        END;

        UPDATE dbo.vehiculo
        SET kilometrajeActual = CASE WHEN kilometrajeActual < @KilometrajeActual THEN @KilometrajeActual ELSE kilometrajeActual END
        WHERE placa = @Placa;
        COMMIT;
        SELECT @Id AS idMantenimiento, @Placa AS placa,
               CONVERT(CHAR(10), @Fecha, 23) AS fecha, @Costo AS costo, @Cantidad AS cantidadComprobantes;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

-- Permite recuperar los bytes; sql://evidencia/... es una referencia, no una URL web.
CREATE OR ALTER PROCEDURE dbo.sp_ObtenerComprobanteConductor
    @IdEvidenciaMantenimiento INT, @IdConductor INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Placa NVARCHAR(20);
    SELECT @Placa = m.placa
    FROM dbo.evidenciaMantenimiento e
    JOIN dbo.mantenimiento m ON m.idMantenimiento = e.idMantenimiento
    WHERE e.idEvidenciaMantenimiento = @IdEvidenciaMantenimiento;
    EXEC dbo.sp_ValidarAccesoVehiculoConductor @IdConductor, @Placa;
    SELECT e.idEvidenciaMantenimiento, e.nombreArchivo, e.tipoEvidencia, a.tipoMime, a.contenido
    FROM dbo.evidenciaMantenimiento e
    JOIN dbo.archivoEvidenciaConductor a ON a.idEvidenciaMantenimiento = e.idEvidenciaMantenimiento
    WHERE e.idEvidenciaMantenimiento = @IdEvidenciaMantenimiento;
END;
GO

