-- ============================================================================
-- SCRIPT DE PROCEDIMIENTOS ALMACENADOS: TransAndina
-- ============================================================================

-- 1. SP: sp_RegistrarUsuario
CREATE OR ALTER PROCEDURE sp_RegistrarUsuario
    @nombre NVARCHAR(50),
    @primerApellido NVARCHAR(50),
    @segundoApellido NVARCHAR(50),
    @cedula NVARCHAR(30),
    @correo NVARCHAR(100),
    @telefono NVARCHAR(20),
    @rol NVARCHAR(30),
    @numLicencia NVARCHAR(50) = NULL,
    @tipoLicencia NVARCHAR(20) = NULL,
    @contrasena NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Validar correo duplicado
        IF EXISTS (SELECT 1 FROM usuario WHERE LOWER(correo) = LOWER(@correo))
        BEGIN
            THROW 50001, 'Ya existe una cuenta registrada con este correo electrónico.', 1;
        END

        -- 2. Validar cédula duplicada
        IF EXISTS (SELECT 1 FROM usuario WHERE cedula = @cedula)
        BEGIN
            THROW 50002, 'Ya existe una cuenta registrada con esta cédula.', 1;
        END

        -- 3. Normalizar y resolver idRol
        DECLARE @idRol INT;
        SELECT @idRol = idRol FROM rol WHERE LOWER(nombreRol) = LOWER(@rol);
        IF @idRol IS NULL
        BEGIN
            SELECT @idRol = idRol FROM rol WHERE LOWER(nombreRol) = 'conductor';
        END

        -- 4. Insertar en tabla usuario
        INSERT INTO usuario (
            nombre, primerApellido, segundoApellido, cedula, correo,
            telefono, numLicencia, tipoLicencia, contrasena, estado, fechaCreacion
        ) VALUES (
            @nombre, @primerApellido, @segundoApellido, @cedula, LOWER(@correo),
            @telefono, @numLicencia, @tipoLicencia, @contrasena, 'activo', GETDATE()
        );

        DECLARE @nuevoIdUsuario INT = SCOPE_IDENTITY();

        -- 5. Asignar rol en usuarioxrol
        INSERT INTO usuarioxrol (idUsuario, idRol, fechaAsignacion)
        VALUES (@nuevoIdUsuario, @idRol, GETDATE());

        COMMIT TRANSACTION;

        -- 6. Retornar datos del usuario creado
        SELECT 
            u.idUsuario,
            u.nombre,
            u.primerApellido,
            u.segundoApellido,
            u.cedula,
            u.correo,
            u.telefono,
            u.numLicencia,
            u.tipoLicencia,
            u.contrasena,
            u.estado,
            r.nombreRol AS rol
        FROM usuario u
        INNER JOIN usuarioxrol ur ON u.idUsuario = ur.idUsuario
        INNER JOIN rol r ON ur.idRol = r.idRol
        WHERE u.idUsuario = @nuevoIdUsuario;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 2. SP: sp_IniciarSesion
CREATE OR ALTER PROCEDURE sp_IniciarSesion
    @correo NVARCHAR(100),
    @contrasena NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @idUsuario INT;
    DECLARE @dbContrasena NVARCHAR(255);
    DECLARE @estado NVARCHAR(20);

    SELECT TOP 1
        @idUsuario = u.idUsuario,
        @dbContrasena = u.contrasena,
        @estado = u.estado
    FROM usuario u
    WHERE LOWER(u.correo) = LOWER(@correo);

    IF @idUsuario IS NULL
    BEGIN
        THROW 50003, 'No existe ningún usuario registrado con el correo ingresado.', 1;
    END

    IF @dbContrasena != @contrasena
    BEGIN
        THROW 50004, 'Contraseña incorrecta.', 1;
    END

    IF LOWER(@estado) != 'activo'
    BEGIN
        DECLARE @msgEstado NVARCHAR(200) = 'Tu cuenta se encuentra ' + @estado + '. Contacta al administrador.';
        THROW 50005, @msgEstado, 1;
    END

    -- Retornar usuario autenticado y su rol
    SELECT 
        u.idUsuario,
        u.nombre,
        u.primerApellido,
        u.segundoApellido,
        u.cedula,
        u.correo,
        u.telefono,
        u.numLicencia,
        u.tipoLicencia,
        u.contrasena,
        u.estado,
        ISNULL(r.nombreRol, 'conductor') AS rol
    FROM usuario u
    LEFT JOIN usuarioxrol ur ON u.idUsuario = ur.idUsuario
    LEFT JOIN rol r ON ur.idRol = r.idRol
    WHERE u.idUsuario = @idUsuario;
END;
GO

-- 3. SP: sp_ObtenerFlotilla
CREATE OR ALTER PROCEDURE sp_ObtenerFlotilla
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        v.placa,
        v.marca,
        v.modelo,
        v.anio,
        v.capacidad,
        v.kilometrajeActual,
        CONVERT(VARCHAR(10), v.vencimientoRtv, 120) AS vencimientoRtv,
        CONVERT(VARCHAR(10), v.vencimientoMarchamo, 120) AS vencimientoMarchamo,
        CONVERT(VARCHAR(10), v.vencimientoSeguro, 120) AS vencimientoSeguro,
        v.estadoOperativo,
        ISNULL(tv.nombreTipo, 'General') AS tipoVehiculo,
        (c.nombre + ' ' + c.primerApellido + ' ' + c.segundoApellido) AS conductorActual
    FROM vehiculo v
    LEFT JOIN tipoVehiculo tv ON v.idTipoVehiculo = tv.idTipoVehiculo
    LEFT JOIN vehiculoxconductor vc ON v.placa = vc.placa AND vc.esActual = 1
    LEFT JOIN usuario c ON vc.idConductor = c.idUsuario
    ORDER BY v.placa ASC;
END;
GO

-- 4. SP: sp_ObtenerDetalleVehiculo
CREATE OR ALTER PROCEDURE sp_ObtenerDetalleVehiculo
    @placa NVARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        v.placa,
        v.marca,
        v.modelo,
        v.anio,
        v.capacidad,
        v.kilometrajeActual,
        CONVERT(VARCHAR(10), v.vencimientoRtv, 120) AS vencimientoRtv,
        CONVERT(VARCHAR(10), v.vencimientoMarchamo, 120) AS vencimientoMarchamo,
        CONVERT(VARCHAR(10), v.vencimientoSeguro, 120) AS vencimientoSeguro,
        v.estadoOperativo,
        ISNULL(tv.nombreTipo, 'General') AS tipoVehiculo,
        (c.nombre + ' ' + c.primerApellido + ' ' + c.segundoApellido) AS conductorActual,
        c.telefono AS conductorTelefono
    FROM vehiculo v
    LEFT JOIN tipoVehiculo tv ON v.idTipoVehiculo = tv.idTipoVehiculo
    LEFT JOIN vehiculoxconductor vc ON v.placa = vc.placa AND vc.esActual = 1
    LEFT JOIN usuario c ON vc.idConductor = c.idUsuario
    WHERE v.placa = @placa;
END;
GO
