-- ============================================================================
-- SCRIPT DE CREACION DE TABLAS: TransAndina
-- ============================================================================

-- Limpieza previa en orden inverso de dependencias
DROP TABLE IF EXISTS usuarioxnotificacion;
DROP TABLE IF EXISTS usuarioNotificacion;
DROP TABLE IF EXISTS notificacion;
DROP TABLE IF EXISTS registroKilometraje;
DROP TABLE IF EXISTS evidenciaMantenimiento;
DROP TABLE IF EXISTS mantenimientoxmecanico;
DROP TABLE IF EXISTS mantenimientoxcategoria;
DROP TABLE IF EXISTS mantenimientoCategoria;
DROP TABLE IF EXISTS mantenimiento;
DROP TABLE IF EXISTS tipoVehiculoxcategoriaServicio;
DROP TABLE IF EXISTS parametroMantenimiento;
DROP TABLE IF EXISTS categoriaServicio;
DROP TABLE IF EXISTS vehiculoxconductor;
DROP TABLE IF EXISTS vehiculoConductor;
DROP TABLE IF EXISTS vehiculo;
DROP TABLE IF EXISTS tipoVehiculo;
DROP TABLE IF EXISTS usuarioxrol;
DROP TABLE IF EXISTS usuarioRol;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS rol;

-- 1. TABLA: rol
CREATE TABLE rol (
    idRol INT IDENTITY(1,1) PRIMARY KEY,
    nombreRol NVARCHAR(30) NOT NULL UNIQUE,
    descripcion NVARCHAR(200) NULL
);

-- 2. TABLA: usuario
CREATE TABLE usuario (
    idUsuario INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(50) NOT NULL,
    primerApellido NVARCHAR(50) NOT NULL,
    segundoApellido NVARCHAR(50) NOT NULL,
    cedula NVARCHAR(30) NOT NULL UNIQUE,
    correo NVARCHAR(100) NOT NULL UNIQUE,
    telefono NVARCHAR(20) NOT NULL,
    numLicencia NVARCHAR(50) NULL,
    tipoLicencia NVARCHAR(20) NULL,
    contrasena NVARCHAR(255) NOT NULL,
    estado NVARCHAR(20) NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'suspendido', 'inactivo')),
    fechaCreacion DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- 3. TABLA INTERMEDIA: usuarioxrol
CREATE TABLE usuarioxrol (
    idUsuarioxrol INT IDENTITY(1,1) PRIMARY KEY,
    idUsuario INT NOT NULL,
    idRol INT NOT NULL,
    fechaAsignacion DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_usuarioxrol_usuario FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario) ON DELETE CASCADE,
    CONSTRAINT FK_usuarioxrol_rol FOREIGN KEY (idRol) REFERENCES rol(idRol)
);

-- 4. TABLA: tipoVehiculo
CREATE TABLE tipoVehiculo (
    idTipoVehiculo INT IDENTITY(1,1) PRIMARY KEY,
    nombreTipo NVARCHAR(30) NOT NULL UNIQUE,
    descripcion NVARCHAR(200) NULL
);

-- 5. TABLA: vehiculo
CREATE TABLE vehiculo (
    placa NVARCHAR(20) PRIMARY KEY,
    idTipoVehiculo INT NOT NULL,
    marca NVARCHAR(50) NOT NULL,
    modelo NVARCHAR(50) NOT NULL,
    anio INT NOT NULL,
    capacidad NVARCHAR(50) NOT NULL,
    kilometrajeActual INT NOT NULL DEFAULT 0,
    vencimientoRtv DATE NOT NULL,
    vencimientoMarchamo DATE NOT NULL,
    vencimientoSeguro DATE NOT NULL,
    estadoOperativo NVARCHAR(30) NOT NULL DEFAULT 'activo' CHECK (estadoOperativo IN ('activo', 'inactivo', 'en_taller')),
    fechaRegistro DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_vehiculo_tipoVehiculo FOREIGN KEY (idTipoVehiculo) REFERENCES tipoVehiculo(idTipoVehiculo)
);

-- 6. TABLA INTERMEDIA: vehiculoxconductor
CREATE TABLE vehiculoxconductor (
    idVehiculoxconductor INT IDENTITY(1,1) PRIMARY KEY,
    placa NVARCHAR(20) NOT NULL,
    idConductor INT NOT NULL,
    idAsignador INT NOT NULL,
    fechaAsignacion DATETIME2 NOT NULL DEFAULT GETDATE(),
    fechaDesasignacion DATETIME2 NULL,
    esActual BIT NOT NULL DEFAULT 1,
    motivo NVARCHAR(255) NULL,
    CONSTRAINT FK_vehiculoxconductor_vehiculo FOREIGN KEY (placa) REFERENCES vehiculo(placa),
    CONSTRAINT FK_vehiculoxconductor_conductor FOREIGN KEY (idConductor) REFERENCES usuario(idUsuario),
    CONSTRAINT FK_vehiculoxconductor_asignador FOREIGN KEY (idAsignador) REFERENCES usuario(idUsuario)
);

-- 7. TABLA: categoriaServicio
CREATE TABLE categoriaServicio (
    idCategoriaServicio INT IDENTITY(1,1) PRIMARY KEY,
    nombreCategoria NVARCHAR(100) NOT NULL UNIQUE,
    descripcion NVARCHAR(255) NULL
);

-- 8. TABLA INTERMEDIA: tipoVehiculoxcategoriaServicio (Frecuencias de mantenimiento)
CREATE TABLE tipoVehiculoxcategoriaServicio (
    idTipoVehiculoxcategoriaServicio INT IDENTITY(1,1) PRIMARY KEY,
    idTipoVehiculo INT NOT NULL,
    idCategoriaServicio INT NOT NULL,
    frecuenciaKm INT NOT NULL,
    frecuenciaDias INT NOT NULL,
    CONSTRAINT FK_tipoVehiculoxcategoria_tipoVehiculo FOREIGN KEY (idTipoVehiculo) REFERENCES tipoVehiculo(idTipoVehiculo),
    CONSTRAINT FK_tipoVehiculoxcategoria_categoriaServicio FOREIGN KEY (idCategoriaServicio) REFERENCES categoriaServicio(idCategoriaServicio)
);

-- 9. TABLA: mantenimiento
CREATE TABLE mantenimiento (
    idMantenimiento INT IDENTITY(1,1) PRIMARY KEY,
    placa NVARCHAR(20) NOT NULL,
    idUsuarioRegistra INT NOT NULL,
    tipoServicio NVARCHAR(30) NOT NULL CHECK (tipoServicio IN ('preventivo', 'correctivo')),
    taller NVARCHAR(150) NOT NULL,
    kilometrajeServicio INT NOT NULL,
    costoTotal DECIMAL(12,2) NOT NULL,
    descripcion NVARCHAR(MAX) NOT NULL,
    fechaMantenimiento DATE NOT NULL,
    fechaRegistro DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_mantenimiento_vehiculo FOREIGN KEY (placa) REFERENCES vehiculo(placa),
    CONSTRAINT FK_mantenimiento_usuarioRegistra FOREIGN KEY (idUsuarioRegistra) REFERENCES usuario(idUsuario)
);

-- 10. TABLA INTERMEDIA: mantenimientoxcategoria (Ejemplo exacto del usuario)
CREATE TABLE mantenimientoxcategoria (
    idMantenimientoxcategoria INT IDENTITY(1,1) PRIMARY KEY,
    idMantenimiento INT NOT NULL,
    idCategoriaServicio INT NOT NULL,
    notas NVARCHAR(255) NULL,
    CONSTRAINT FK_mantenimientoxcategoria_mantenimiento FOREIGN KEY (idMantenimiento) REFERENCES mantenimiento(idMantenimiento) ON DELETE CASCADE,
    CONSTRAINT FK_mantenimientoxcategoria_categoriaServicio FOREIGN KEY (idCategoriaServicio) REFERENCES categoriaServicio(idCategoriaServicio)
);

-- 11. TABLA INTERMEDIA: mantenimientoxmecanico (Mecánicos responsables de la intervención)
CREATE TABLE mantenimientoxmecanico (
    idMantenimientoxmecanico INT IDENTITY(1,1) PRIMARY KEY,
    idMantenimiento INT NOT NULL,
    idMecanico INT NOT NULL,
    horasTrabajadas DECIMAL(5,2) NULL,
    observaciones NVARCHAR(255) NULL,
    fechaAsignacion DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_mantenimientoxmecanico_mantenimiento FOREIGN KEY (idMantenimiento) REFERENCES mantenimiento(idMantenimiento) ON DELETE CASCADE,
    CONSTRAINT FK_mantenimientoxmecanico_mecanico FOREIGN KEY (idMecanico) REFERENCES usuario(idUsuario)
);

-- 12. TABLA: evidenciaMantenimiento (Sección de fotos y facturas)
CREATE TABLE evidenciaMantenimiento (
    idEvidenciaMantenimiento INT IDENTITY(1,1) PRIMARY KEY,
    idMantenimiento INT NOT NULL,
    urlArchivo NVARCHAR(500) NOT NULL,
    nombreArchivo NVARCHAR(255) NOT NULL,
    tipoEvidencia NVARCHAR(50) NOT NULL DEFAULT 'foto',
    descripcion NVARCHAR(255) NULL,
    fechaHoraCaptura DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_evidenciaMantenimiento_mantenimiento FOREIGN KEY (idMantenimiento) REFERENCES mantenimiento(idMantenimiento) ON DELETE CASCADE
);

-- 13. TABLA: registroKilometraje
CREATE TABLE registroKilometraje (
    idRegistroKilometraje INT IDENTITY(1,1) PRIMARY KEY,
    placa NVARCHAR(20) NOT NULL,
    idConductor INT NOT NULL,
    kilometraje INT NOT NULL,
    fechaRegistro DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_registroKilometraje_vehiculo FOREIGN KEY (placa) REFERENCES vehiculo(placa),
    CONSTRAINT FK_registroKilometraje_conductor FOREIGN KEY (idConductor) REFERENCES usuario(idUsuario)
);

-- 14. TABLA: notificacion
CREATE TABLE notificacion (
    idNotificacion INT IDENTITY(1,1) PRIMARY KEY,
    placa NVARCHAR(20) NULL,
    titulo NVARCHAR(100) NOT NULL,
    mensaje NVARCHAR(300) NOT NULL,
    tipoAlerta NVARCHAR(50) NOT NULL,
    prioridad NVARCHAR(20) NOT NULL CHECK (prioridad IN ('general', 'importante', 'urgente')),
    fechaCreacion DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_notificacion_vehiculo FOREIGN KEY (placa) REFERENCES vehiculo(placa)
);

-- 15. TABLA INTERMEDIA: usuarioxnotificacion
CREATE TABLE usuarioxnotificacion (
    idUsuarioxnotificacion INT IDENTITY(1,1) PRIMARY KEY,
    idNotificacion INT NOT NULL,
    idUsuario INT NOT NULL,
    leida BIT NOT NULL DEFAULT 0,
    fechaLectura DATETIME2 NULL,
    CONSTRAINT FK_usuarioxnotificacion_notificacion FOREIGN KEY (idNotificacion) REFERENCES notificacion(idNotificacion) ON DELETE CASCADE,
    CONSTRAINT FK_usuarioxnotificacion_usuario FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario)
);
