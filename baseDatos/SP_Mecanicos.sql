SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ValidarAccesoMecanico
    @IdMecanico INT
AS
BEGIN
    SET NOCOUNT ON;
    IF @IdMecanico IS NULL OR @IdMecanico <= 0
        THROW 52000, N'El identificador del mecanico es obligatorio.', 1;
    IF NOT EXISTS (
        SELECT 1 FROM dbo.usuario u
        JOIN dbo.usuarioxrol ur ON ur.idUsuario = u.idUsuario
        JOIN dbo.rol r ON r.idRol = ur.idRol
        WHERE u.idUsuario = @IdMecanico
          AND u.estado = N'activo'
          AND r.nombreRol COLLATE Latin1_General_100_CI_AI = N'mecanico'
    )
        THROW 52001, N'Usuario inactivo o sin rol de mecanico.', 1;
END;
GO

-- ============================================================================
-- PANTALLA "Registro Mantenimientos" (paso 1): selector de vehiculo
-- ============================================================================
CREATE OR ALTER PROCEDURE dbo.sp_ObtenerVehiculosActivosMecanico
    @IdMecanico INT
AS
BEGIN
    SET NOCOUNT ON;
    EXEC dbo.sp_ValidarAccesoMecanico @IdMecanico;
    -- El mecanico puede reportar sobre cualquier vehiculo que siga en operacion
    -- o que ya este en taller; los inactivos (de baja) se excluyen.
    SELECT v.placa,
           CONCAT(v.marca, N' ', v.modelo, N' ', v.anio) AS nombreVehiculo,
           v.kilometrajeActual
    FROM dbo.vehiculo v
    WHERE v.estadoOperativo IN (N'activo', N'en_taller')
    ORDER BY v.placa;
END;
GO


CREATE OR ALTER PROCEDURE dbo.sp_RegistrarMantenimientoMecanico
    @Placa NVARCHAR(20), @KilometrajeActual INT,
    @TipoServicio NVARCHAR(30), @Categoria NVARCHAR(100),
    @Fecha DATE, @Taller NVARCHAR(150), @Costo DECIMAL(12,2),
    @Descripcion NVARCHAR(MAX), @IdMecanico INT,
    @ArchivosJson NVARCHAR(MAX) = N'[]'
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    IF @@TRANCOUNT <> 0 THROW 51106, N'Invocar fuera de una transaccion existente.', 1;
    SET @Placa = NULLIF(LTRIM(RTRIM(@Placa)), N'');
    IF @Placa IS NULL THROW 52107, N'Vehiculo obligatorio.', 1;
    EXEC dbo.sp_ValidarAccesoMecanico @IdMecanico;
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
        SELECT @Bloqueo = kilometrajeActual FROM dbo.vehiculo WITH (UPDLOCK, HOLDLOCK)
        WHERE placa = @Placa AND estadoOperativo IN (N'activo', N'en_taller');
        IF @Bloqueo IS NULL THROW 52107, N'Vehiculo inexistente o inactivo.', 1;
        EXEC dbo.sp_ValidarAccesoMecanico @IdMecanico;
        EXEC dbo.sp_ValidarLecturaConductor @Placa, @Fecha, @KilometrajeActual;
        SELECT @IdCategoria = idCategoriaServicio FROM dbo.categoriaServicio
        WHERE nombreCategoria = LTRIM(RTRIM(@Categoria));
        IF @IdCategoria IS NULL THROW 51114, N'Categoria inexistente.', 1;

        INSERT dbo.mantenimiento(placa, idUsuarioRegistra, tipoServicio, taller,
            kilometrajeServicio, costoTotal, descripcion, fechaMantenimiento)
        VALUES (@Placa, @IdMecanico, @TipoServicio, LTRIM(RTRIM(@Taller)),
            @KilometrajeActual, @Costo, @Descripcion, @Fecha);
        DECLARE @Id INT = CONVERT(INT, SCOPE_IDENTITY());
        INSERT dbo.mantenimientoxcategoria(idMantenimiento, idCategoriaServicio) VALUES (@Id, @IdCategoria);
        INSERT dbo.mantenimientoxmecanico(idMantenimiento, idMecanico) VALUES (@Id, @IdMecanico);

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


CREATE OR ALTER PROCEDURE dbo.sp_ObtenerResumenMecanico
    @IdMecanico INT
AS
BEGIN
    SET NOCOUNT ON;
    EXEC dbo.sp_ValidarAccesoMecanico @IdMecanico;
    SELECT u.idUsuario AS id,
           CONCAT(u.nombre, N' ', u.primerApellido, N' ', u.segundoApellido) AS nombreUsuario,
           CONVERT(CHAR(10), u.fechaCreacion, 103) AS activoDesde
    FROM dbo.usuario u
    WHERE u.idUsuario = @IdMecanico;
END;
GO

CREATE OR ALTER PROCEDURE dbo.sp_ObtenerHistorialMecanico
    @IdMecanico INT,
    @Servicio NVARCHAR(100) = NULL,
    @Desde DATE = NULL,
    @Hasta DATE = NULL,
    @DesfaseUtcMinutos SMALLINT = 0
AS
BEGIN
    SET NOCOUNT ON;
    EXEC dbo.sp_ValidarAccesoMecanico @IdMecanico;
    IF @DesfaseUtcMinutos IS NULL OR @DesfaseUtcMinutos NOT BETWEEN -840 AND 840
        THROW 52003, N'Desfase UTC no valido.', 1;
    IF @Desde IS NOT NULL AND @Hasta IS NOT NULL AND @Desde > @Hasta
        THROW 52004, N'El rango de fechas no es valido.', 1;
    SET @Servicio = NULLIF(LTRIM(RTRIM(@Servicio)), N'');

    -- Una fila por mantenimiento en el que participo este mecanico, aunque el
    -- servicio tenga varias categorias asociadas.
    SELECT CONVERT(VARCHAR(11), m.idMantenimiento) AS id,
           CONVERT(CHAR(10), m.fechaMantenimiento, 103) AS fecha,
           DATEDIFF_BIG(MILLISECOND, CONVERT(DATETIME2, '19700101'),
               DATEADD(MINUTE, -@DesfaseUtcMinutos, CONVERT(DATETIME2, m.fechaMantenimiento))) AS fechaMillis,
           m.placa,
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
    WHERE EXISTS (SELECT 1 FROM dbo.mantenimientoxmecanico mx
                  WHERE mx.idMantenimiento = m.idMantenimiento AND mx.idMecanico = @IdMecanico)
      AND (@Desde IS NULL OR m.fechaMantenimiento >= @Desde)
      AND (@Hasta IS NULL OR m.fechaMantenimiento <= @Hasta)
      AND (@Servicio IS NULL
           OR CHARINDEX(@Servicio, m.tipoServicio COLLATE Latin1_General_100_CI_AI) > 0
           OR CHARINDEX(@Servicio, COALESCE(c.servicio, N'') COLLATE Latin1_General_100_CI_AI) > 0)
    ORDER BY m.fechaMantenimiento DESC, m.idMantenimiento DESC;
END;
GO