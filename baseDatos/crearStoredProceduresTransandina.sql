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

-- 5. SP: sp_ObtenerConductores
CREATE OR ALTER PROCEDURE sp_ObtenerConductores
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        u.idUsuario,
        (u.nombre + ' ' + u.primerApellido + ' ' + u.segundoApellido) AS nombreCompleto,
        u.nombre,
        u.primerApellido,
        u.segundoApellido,
        u.cedula,
        u.correo,
        u.telefono,
        u.numLicencia,
        u.tipoLicencia,
        u.estado
    FROM usuario u
    INNER JOIN usuarioxrol ur ON u.idUsuario = ur.idUsuario
    INNER JOIN rol r ON ur.idRol = r.idRol
    WHERE LOWER(r.nombreRol) = 'conductor'
    ORDER BY u.nombre ASC;
END;
GO

-- 6. SP: sp_ObtenerMecanicos
CREATE OR ALTER PROCEDURE sp_ObtenerMecanicos
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        u.idUsuario,
        (u.nombre + ' ' + u.primerApellido + ' ' + u.segundoApellido) AS nombreCompleto,
        u.nombre,
        u.primerApellido,
        u.segundoApellido,
        u.cedula,
        u.correo,
        u.telefono,
        u.estado
    FROM usuario u
    INNER JOIN usuarioxrol ur ON u.idUsuario = ur.idUsuario
    INNER JOIN rol r ON ur.idRol = r.idRol
    WHERE LOWER(r.nombreRol) = 'mecanico'
    ORDER BY u.nombre ASC;
END;
GO

-- 7. SP: sp_ActualizarEstadoUsuario
CREATE OR ALTER PROCEDURE sp_ActualizarEstadoUsuario
    @idUsuario INT,
    @nuevoEstado NVARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @estadoNorm NVARCHAR(20) = LOWER(LTRIM(RTRIM(@nuevoEstado)));

    IF @estadoNorm NOT IN ('activo', 'inactivo', 'suspendido')
    BEGIN
        THROW 50010, 'El estado especificado no es válido. Debe ser activo, inactivo o suspendido.', 1;
    END

    UPDATE usuario
    SET estado = @estadoNorm
    WHERE idUsuario = @idUsuario;

    -- Si el conductor pasa a inactivo o suspendido, desasignar automáticamente cualquier vehículo que tenga asignado
    IF @estadoNorm IN ('inactivo', 'suspendido')
    BEGIN
        UPDATE vehiculoxconductor
        SET esActual = 0,
            fechaDesasignacion = GETDATE(),
            motivo = 'Desasignado automáticamente al pasar a estado ' + @estadoNorm
        WHERE idConductor = @idUsuario AND esActual = 1;
    END

    SELECT @@ROWCOUNT AS filasAfectadas;
END;
GO

-- 8. SP: sp_ObtenerConductoresDisponibles
CREATE OR ALTER PROCEDURE sp_ObtenerConductoresDisponibles
    @placaActual NVARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        u.idUsuario,
        (u.nombre + ' ' + u.primerApellido + ' ' + u.segundoApellido) AS nombreCompleto,
        u.cedula,
        u.telefono,
        u.tipoLicencia,
        u.numLicencia,
        u.estado,
        ISNULL(vc.placa, '') AS vehiculoAsignadoActual
    FROM usuario u
    INNER JOIN usuarioxrol ur ON u.idUsuario = ur.idUsuario
    INNER JOIN rol r ON ur.idRol = r.idRol
    LEFT JOIN vehiculoxconductor vc ON u.idUsuario = vc.idConductor AND vc.esActual = 1
    WHERE LOWER(r.nombreRol) = 'conductor'
      AND LOWER(u.estado) = 'activo'
      AND (@placaActual IS NULL OR vc.placa IS NULL OR vc.placa != @placaActual)
    ORDER BY u.nombre ASC;
END;
GO

-- 9. SP: sp_ReasignarConductorVehiculo
CREATE OR ALTER PROCEDURE sp_ReasignarConductorVehiculo
    @placa NVARCHAR(20),
    @idNuevoConductor INT,
    @idAsignador INT = 1
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Verificar existencia del vehículo
        IF NOT EXISTS (SELECT 1 FROM vehiculo WHERE placa = @placa)
        BEGIN
            THROW 50020, 'El vehículo indicado no existe.', 1;
        END

        -- 2. Verificar existencia del conductor
        IF NOT EXISTS (SELECT 1 FROM usuario WHERE idUsuario = @idNuevoConductor)
        BEGIN
            THROW 50021, 'El conductor indicado no existe.', 1;
        END

        -- 3. Desasignar cualquier conductor actualmente asignado a este vehículo
        UPDATE vehiculoxconductor
        SET esActual = 0,
            fechaDesasignacion = GETDATE()
        WHERE placa = @placa AND esActual = 1;

        -- 4. Desasignar al conductor de cualquier otro vehículo que tuviera asignado
        UPDATE vehiculoxconductor
        SET esActual = 0,
            fechaDesasignacion = GETDATE()
        WHERE idConductor = @idNuevoConductor AND esActual = 1;

        -- 5. Insertar nueva asignación
        INSERT INTO vehiculoxconductor (
            placa, idConductor, idAsignador, fechaAsignacion, esActual, motivo
        ) VALUES (
            @placa, @idNuevoConductor, @idAsignador, GETDATE(), 1, 'Reasignación desde la aplicación de administración'
        );

        COMMIT TRANSACTION;

        SELECT 1 AS exito, 'Conductor reasignado exitosamente' AS mensaje;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 10. SP: sp_ObtenerFichaTecnicaVehiculo
CREATE OR ALTER PROCEDURE sp_ObtenerFichaTecnicaVehiculo
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
        ISNULL((c.nombre + ' ' + c.primerApellido + ' ' + c.segundoApellido), 'Sin asignar') AS conductorActual,
        c.idUsuario AS idConductorActual
    FROM vehiculo v
    LEFT JOIN tipoVehiculo tv ON v.idTipoVehiculo = tv.idTipoVehiculo
    LEFT JOIN vehiculoxconductor vc ON v.placa = vc.placa AND vc.esActual = 1
    LEFT JOIN usuario c ON vc.idConductor = c.idUsuario
    WHERE v.placa = @placa;
END;
GO

-- 11. SP: sp_ActualizarVehiculo
CREATE OR ALTER PROCEDURE sp_ActualizarVehiculo
    @placa NVARCHAR(20),
    @marca NVARCHAR(50) = NULL,
    @modelo NVARCHAR(50) = NULL,
    @anio INT = NULL,
    @capacidad NVARCHAR(50) = NULL,
    @tipoVehiculo NVARCHAR(50) = NULL,
    @kilometrajeActual INT = NULL,
    @vencimientoRtv DATE = NULL,
    @vencimientoMarchamo DATE = NULL,
    @vencimientoSeguro DATE = NULL,
    @estadoOperativo NVARCHAR(30) = NULL,
    @idConductor INT = NULL,
    @idAsignador INT = 1
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1. Resolver idTipoVehiculo si se envía nombre
        DECLARE @idTipoVehiculo INT = NULL;
        IF @tipoVehiculo IS NOT NULL
        BEGIN
            SELECT @idTipoVehiculo = idTipoVehiculo 
            FROM tipoVehiculo 
            WHERE LOWER(nombreTipo) = LOWER(@tipoVehiculo);
        END

        -- 2. Normalizar estadoOperativo
        DECLARE @estadoOperativoNorm NVARCHAR(30) = NULL;
        IF @estadoOperativo IS NOT NULL
        BEGIN
            SET @estadoOperativoNorm = LOWER(LTRIM(RTRIM(@estadoOperativo)));
            IF @estadoOperativoNorm NOT IN ('activo', 'inactivo', 'en_taller')
            BEGIN
                SET @estadoOperativoNorm = 'activo';
            END
        END

        -- 3. Actualizar tabla vehiculo
        UPDATE vehiculo
        SET 
            marca = ISNULL(@marca, marca),
            modelo = ISNULL(@modelo, modelo),
            anio = ISNULL(@anio, anio),
            capacidad = ISNULL(@capacidad, capacidad),
            idTipoVehiculo = ISNULL(@idTipoVehiculo, idTipoVehiculo),
            kilometrajeActual = ISNULL(@kilometrajeActual, kilometrajeActual),
            vencimientoRtv = ISNULL(@vencimientoRtv, vencimientoRtv),
            vencimientoMarchamo = ISNULL(@vencimientoMarchamo, vencimientoMarchamo),
            vencimientoSeguro = ISNULL(@vencimientoSeguro, vencimientoSeguro),
            estadoOperativo = ISNULL(@estadoOperativoNorm, estadoOperativo)
        WHERE placa = @placa;

        -- 4. Si se especificó conductor, actualizar asignación
        IF @idConductor IS NOT NULL
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM vehiculoxconductor WHERE placa = @placa AND idConductor = @idConductor AND esActual = 1)
            BEGIN
                UPDATE vehiculoxconductor
                SET esActual = 0, fechaDesasignacion = GETDATE()
                WHERE placa = @placa AND esActual = 1;

                UPDATE vehiculoxconductor
                SET esActual = 0, fechaDesasignacion = GETDATE()
                WHERE idConductor = @idConductor AND esActual = 1;

                INSERT INTO vehiculoxconductor (placa, idConductor, idAsignador, fechaAsignacion, esActual, motivo)
                VALUES (@placa, @idConductor, @idAsignador, GETDATE(), 1, 'Asignación actualizada desde edición vehicular');
            END
        END
        ELSE
        BEGIN
            -- Si se envía NULL como conductor, desasignar el conductor actual de este vehículo
            UPDATE vehiculoxconductor
            SET esActual = 0, fechaDesasignacion = GETDATE(), motivo = 'Desasignado desde edición vehicular'
            WHERE placa = @placa AND esActual = 1;
        END

        COMMIT TRANSACTION;
        SELECT 1 AS exito, 'Vehículo actualizado exitosamente' AS mensaje;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

