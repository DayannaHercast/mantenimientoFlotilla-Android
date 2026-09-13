-- ============================================================================
-- SCRIPT DE DATOS SEMILLA: TransAndina (Azure SQL Database)
-- Convenciones: Nombres en singular, tablas intermedias con 'x'
-- Servidor: tec-apdatabases.database.windows.net
-- Base de datos: transandina
-- ============================================================================

-- 1. Limpieza de datos existentes en orden inverso de dependencias
DELETE FROM usuarioxnotificacion;
DELETE FROM notificacion;
DELETE FROM registroKilometraje;
DELETE FROM evidenciaMantenimiento;
DELETE FROM mantenimientoxmecanico;
DELETE FROM mantenimientoxcategoria;
DELETE FROM mantenimiento;
DELETE FROM tipoVehiculoxcategoriaServicio;
DELETE FROM categoriaServicio;
DELETE FROM vehiculoxconductor;
DELETE FROM vehiculo;
DELETE FROM tipoVehiculo;
DELETE FROM usuarioxrol;
DELETE FROM usuario;
DELETE FROM rol;

-- Reinicio de identificadores identity
DBCC CHECKIDENT ('rol', RESEED, 0);
DBCC CHECKIDENT ('usuario', RESEED, 0);
DBCC CHECKIDENT ('usuarioxrol', RESEED, 0);
DBCC CHECKIDENT ('tipoVehiculo', RESEED, 0);
DBCC CHECKIDENT ('vehiculoxconductor', RESEED, 0);
DBCC CHECKIDENT ('categoriaServicio', RESEED, 0);
DBCC CHECKIDENT ('tipoVehiculoxcategoriaServicio', RESEED, 0);
DBCC CHECKIDENT ('mantenimiento', RESEED, 0);
DBCC CHECKIDENT ('mantenimientoxcategoria', RESEED, 0);
DBCC CHECKIDENT ('mantenimientoxmecanico', RESEED, 0);
DBCC CHECKIDENT ('evidenciaMantenimiento', RESEED, 0);
DBCC CHECKIDENT ('registroKilometraje', RESEED, 0);
DBCC CHECKIDENT ('notificacion', RESEED, 0);
DBCC CHECKIDENT ('usuarioxnotificacion', RESEED, 0);

-- ============================================================================
-- 2. TABLA: rol
-- ============================================================================
INSERT INTO rol (nombreRol, descripcion) VALUES
('administrador', 'Acceso total al sistema, administracion de flotilla, usuarios y reportes'),
('conductor', 'Registro de bitacora, odometro y consulta de vehiculo asignado'),
('mecanico', 'Registro de intervenciones tecnicas, reparaciones y mantenimiento');

-- ============================================================================
-- 3. TABLA: tipoVehiculo
-- ============================================================================
INSERT INTO tipoVehiculo (nombreTipo, descripcion) VALUES
('liviano', 'Vehiculos livianos, automoviles, sedan y pickups'),
('pesado', 'Camiones de carga pesada, autobuses y trailers'),
('especial', 'Maquinaria pesada, retroexcavadoras, gruas y montacargas');

-- ============================================================================
-- 4. TABLA: categoriaServicio
-- ============================================================================
INSERT INTO categoriaServicio (nombreCategoria, descripcion) VALUES
('Cambio de Aceite', 'Reemplazo de aceite de motor y filtros'),
('Frenos', 'Inspeccion y reemplazo de pastillas, discos y liquido de frenos'),
('Llantas', 'Rotacion, alineacion, balanceo y cambio de neumaticos'),
('Suspension', 'Amortiguadores, bujes y terminales de direccion'),
('Afinacion General', 'Bujias, filtros de aire y combustible, limpieza de inyectores'),
('Revision General', 'Chequeo preventivo de niveles, bateria, luces y escaneo'),
('Transmision', 'Cambio de fluido y revision de caja de cambios');

-- ============================================================================
-- 5. TABLA INTERMEDIA: tipoVehiculoxcategoriaServicio (Frecuencias)
-- ============================================================================
DECLARE @idTipoLiviano INT = (SELECT idTipoVehiculo FROM tipoVehiculo WHERE nombreTipo = 'liviano');
DECLARE @idTipoPesado INT = (SELECT idTipoVehiculo FROM tipoVehiculo WHERE nombreTipo = 'pesado');
DECLARE @idTipoEspecial INT = (SELECT idTipoVehiculo FROM tipoVehiculo WHERE nombreTipo = 'especial');

DECLARE @idCatAceite INT = (SELECT idCategoriaServicio FROM categoriaServicio WHERE nombreCategoria = 'Cambio de Aceite');
DECLARE @idCatFrenos INT = (SELECT idCategoriaServicio FROM categoriaServicio WHERE nombreCategoria = 'Frenos');
DECLARE @idCatLlantas INT = (SELECT idCategoriaServicio FROM categoriaServicio WHERE nombreCategoria = 'Llantas');
DECLARE @idCatSuspension INT = (SELECT idCategoriaServicio FROM categoriaServicio WHERE nombreCategoria = 'Suspension');
DECLARE @idCatAfinacion INT = (SELECT idCategoriaServicio FROM categoriaServicio WHERE nombreCategoria = 'Afinacion General');
DECLARE @idCatRevision INT = (SELECT idCategoriaServicio FROM categoriaServicio WHERE nombreCategoria = 'Revision General');
DECLARE @idCatTransmision INT = (SELECT idCategoriaServicio FROM categoriaServicio WHERE nombreCategoria = 'Transmision');

INSERT INTO tipoVehiculoxcategoriaServicio (idTipoVehiculo, idCategoriaServicio, frecuenciaKm, frecuenciaDias) VALUES
-- Liviano
(@idTipoLiviano, @idCatAceite, 5000, 90),
(@idTipoLiviano, @idCatFrenos, 15000, 180),
(@idTipoLiviano, @idCatLlantas, 10000, 180),
(@idTipoLiviano, @idCatAfinacion, 20000, 365),
(@idTipoLiviano, @idCatRevision, 10000, 180),
-- Pesado
(@idTipoPesado, @idCatAceite, 10000, 120),
(@idTipoPesado, @idCatFrenos, 20000, 180),
(@idTipoPesado, @idCatLlantas, 25000, 180),
(@idTipoPesado, @idCatSuspension, 30000, 365),
(@idTipoPesado, @idCatRevision, 15000, 180),
-- Especial
(@idTipoEspecial, @idCatAceite, 8000, 120),
(@idTipoEspecial, @idCatRevision, 10000, 180);

-- ============================================================================
-- 6. TABLA: usuario
-- ============================================================================
-- Contraseñas en texto plano (según requerimiento del usuario)
DECLARE @pwdAdmin NVARCHAR(50) = 'admin123';
DECLARE @pwdConductor NVARCHAR(50) = 'conductor123';
DECLARE @pwdMecanico NVARCHAR(50) = 'mecanico123';

INSERT INTO usuario (nombre, primerApellido, segundoApellido, cedula, correo, telefono, numLicencia, tipoLicencia, contrasena, estado, fechaCreacion) VALUES
-- Administradora
('Dayanna', 'Herrera', 'Castro', '112340567', 'dayanna.herrera@transandina.com', '88881111', NULL, NULL, @pwdAdmin, 'activo', '2026-01-15 08:00:00'),
-- Conductores
('Juan', 'Perez', 'Lopez', '108920341', 'juan.perez@transandina.com', '87654321', '108920341', 'B1', @pwdConductor, 'activo', '2026-02-01 09:00:00'),
('Estefannia', 'Portuguez', 'Viquez', '205430876', 'estefannia.portuguez@transandina.com', '83456789', '205430876', 'B2', @pwdConductor, 'activo', '2026-02-01 09:15:00'),
('Francisco', 'Solano', 'Molina', '304120987', 'francisco.solano@transandina.com', '85213698', '304120987', 'B3', @pwdConductor, 'activo', '2026-02-01 09:30:00'),
('Leticia', 'Rivera', 'Fernandez', '115670234', 'leticia.rivera@transandina.com', '89632587', '115670234', 'B1', @pwdConductor, 'activo', '2026-02-10 10:00:00'),
-- Mecanicos
('Leonardo', 'Viquez', 'Sanchez', '401980765', 'leonardo.viquez@transandina.com', '84561230', NULL, NULL, @pwdMecanico, 'activo', '2026-02-15 08:30:00'),
('Eduardo', 'Martinez', 'Soto', '109870654', 'eduardo.martinez@transandina.com', '83214567', NULL, NULL, @pwdMecanico, 'activo', '2026-02-15 08:45:00'),
('Fernando', 'Solano', 'Molina', '207650432', 'fernando.solano@transandina.com', '87896543', NULL, NULL, @pwdMecanico, 'activo', '2026-02-15 09:00:00'),
('Daniel', 'Perez', 'Jimenez', '308760543', 'daniel.perez@transandina.com', '86549870', NULL, NULL, @pwdMecanico, 'activo', '2026-02-15 09:15:00');

-- ============================================================================
-- 7. TABLA INTERMEDIA: usuarioxrol
-- ============================================================================
DECLARE @idRolAdmin INT = (SELECT idRol FROM rol WHERE nombreRol = 'administrador');
DECLARE @idRolConductor INT = (SELECT idRol FROM rol WHERE nombreRol = 'conductor');
DECLARE @idRolMecanico INT = (SELECT idRol FROM rol WHERE nombreRol = 'mecanico');

DECLARE @idUserDayanna INT = (SELECT idUsuario FROM usuario WHERE cedula = '112340567');
DECLARE @idUserJuan INT = (SELECT idUsuario FROM usuario WHERE cedula = '108920341');
DECLARE @idUserEstefannia INT = (SELECT idUsuario FROM usuario WHERE cedula = '205430876');
DECLARE @idUserFrancisco INT = (SELECT idUsuario FROM usuario WHERE cedula = '304120987');
DECLARE @idUserLeticia INT = (SELECT idUsuario FROM usuario WHERE cedula = '115670234');
DECLARE @idUserLeonardo INT = (SELECT idUsuario FROM usuario WHERE cedula = '401980765');
DECLARE @idUserEduardo INT = (SELECT idUsuario FROM usuario WHERE cedula = '109870654');
DECLARE @idUserFernando INT = (SELECT idUsuario FROM usuario WHERE cedula = '207650432');
DECLARE @idUserDaniel INT = (SELECT idUsuario FROM usuario WHERE cedula = '308760543');

INSERT INTO usuarioxrol (idUsuario, idRol) VALUES
(@idUserDayanna, @idRolAdmin),
(@idUserJuan, @idRolConductor),
(@idUserEstefannia, @idRolConductor),
(@idUserFrancisco, @idRolConductor),
(@idUserLeticia, @idRolConductor),
(@idUserLeonardo, @idRolMecanico),
(@idUserEduardo, @idRolMecanico),
(@idUserFernando, @idRolMecanico),
(@idUserDaniel, @idRolMecanico);

-- ============================================================================
-- 8. TABLA: vehiculo
-- ============================================================================
INSERT INTO vehiculo (placa, idTipoVehiculo, marca, modelo, anio, capacidad, kilometrajeActual, vencimientoRtv, vencimientoMarchamo, vencimientoSeguro, estadoOperativo, fechaRegistro) VALUES
('DDD-123', @idTipoLiviano, 'Toyota', 'Hilux', 2022, '5 pasajeros / 1 ton', 48500, '2026-11-30', '2026-12-31', '2027-01-15', 'activo', '2026-01-10 10:00:00'),
('DDD-124', @idTipoPesado, 'Freightliner', 'Cascadia', 2020, '30 toneladas', 185200, '2026-10-15', '2026-12-31', '2026-11-20', 'activo', '2026-01-12 11:30:00'),
('DDD-125', @idTipoPesado, 'Isuzu', 'Forward', 2021, '12 toneladas', 92400, '2026-09-30', '2026-12-31', '2026-12-05', 'en_taller', '2026-01-15 09:00:00'),
('DDD-126', @idTipoLiviano, 'Nissan', 'Navara', 2023, '5 pasajeros', 31000, '2027-03-20', '2026-12-31', '2027-04-10', 'activo', '2026-01-20 14:00:00'),
('DDD-127', @idTipoEspecial, 'Caterpillar', '420F2', 2019, 'Retroexcavadora', 15800, '2026-12-15', '2026-12-31', '2027-02-28', 'activo', '2026-02-01 10:30:00'),
('DDD-128', @idTipoPesado, 'Mercedes-Benz', 'Actros', 2021, '25 toneladas', 142000, '2026-08-31', '2026-12-31', '2026-09-15', 'inactivo', '2026-02-05 08:00:00');

-- ============================================================================
-- 9. TABLA INTERMEDIA: vehiculoxconductor
-- ============================================================================
INSERT INTO vehiculoxconductor (placa, idConductor, idAsignador, fechaAsignacion, fechaDesasignacion, esActual, motivo) VALUES
('DDD-123', @idUserJuan, @idUserDayanna, '2026-02-01 08:00:00', NULL, 1, 'Asignacion principal de ruta regional norte'),
('DDD-124', @idUserEstefannia, @idUserDayanna, '2026-02-01 08:00:00', NULL, 1, 'Asignacion de transporte de carga pesada ruta San Jose - Limon'),
('DDD-125', @idUserFrancisco, @idUserDayanna, '2026-02-01 08:00:00', NULL, 1, 'Asignacion de distribucion central valle central'),
('DDD-126', @idUserLeticia, @idUserDayanna, '2026-02-10 09:00:00', NULL, 1, 'Asignacion de supervision y visitas operativas');

-- ============================================================================
-- 10. TABLA: mantenimiento
-- ============================================================================
INSERT INTO mantenimiento (placa, idUsuarioRegistra, tipoServicio, taller, kilometrajeServicio, costoTotal, descripcion, fechaMantenimiento, fechaRegistro) VALUES
('DDD-123', @idUserDayanna, 'preventivo', 'Taller Central TransAndina', 45000, 125000.00, 'Cambio de aceite sintetico 5W-30, filtro de aceite y sustitucion de pastillas de freno delanteras.', '2026-07-10', '2026-07-10 11:45:00'),
('DDD-124', @idUserDayanna, 'correctivo', 'Servicentro Diesel del Este', 180000, 450000.00, 'Reemplazo de amortiguadores delanteros por fuga de aceite y alineacion completa de tren delantero con cambio de dos neumaticos traseros.', '2026-08-05', '2026-08-05 17:00:00'),
('DDD-125', @idUserDayanna, 'correctivo', 'Taller Central TransAndina', 92400, 320000.00, 'Rectificacion de tambores traseros, cambio de zapatas y revision integral de lineas de freno neumaticas.', '2026-09-10', '2026-09-10 16:30:00');

-- ============================================================================
-- 11. TABLA INTERMEDIA: mantenimientoxcategoria
-- ============================================================================
DECLARE @idMantenimiento1 INT = (SELECT idMantenimiento FROM mantenimiento WHERE placa = 'DDD-123' AND kilometrajeServicio = 45000);
DECLARE @idMantenimiento2 INT = (SELECT idMantenimiento FROM mantenimiento WHERE placa = 'DDD-124' AND kilometrajeServicio = 180000);
DECLARE @idMantenimiento3 INT = (SELECT idMantenimiento FROM mantenimiento WHERE placa = 'DDD-125' AND kilometrajeServicio = 92400);

INSERT INTO mantenimientoxcategoria (idMantenimiento, idCategoriaServicio, notas) VALUES
-- Mantenimiento 1 (DDD-123)
(@idMantenimiento1, @idCatAceite, 'Aceite sintetico 5W-30 Valvoline y filtro OEM'),
(@idMantenimiento1, @idCatFrenos, 'Pastillas ceramicas delanteras nuevas'),
-- Mantenimiento 2 (DDD-124)
(@idMantenimiento2, @idCatSuspension, 'Par de amortiguadores delanteros Monroe Heavy Duty'),
(@idMantenimiento2, @idCatLlantas, 'Dos neumaticos Bridgestone 295/80R22.5 con balanceo'),
-- Mantenimiento 3 (DDD-125)
(@idMantenimiento3, @idCatFrenos, 'Rectificacion de tambores y zapatas nuevas'),
(@idMantenimiento3, @idCatRevision, 'Escaneo computarizado y purga de circuito de frenos');

-- ============================================================================
-- 12. TABLA INTERMEDIA: mantenimientoxmecanico
-- ============================================================================
INSERT INTO mantenimientoxmecanico (idMantenimiento, idMecanico, horasTrabajadas, observaciones, fechaAsignacion) VALUES
(@idMantenimiento1, @idUserLeonardo, 4.50, 'Mantenimiento preventivo completado segun pauta. Niveles correctos y frenos asentados.', '2026-07-10 08:00:00'),
(@idMantenimiento2, @idUserEduardo, 8.00, 'Sustitucion de amortiguadores y alineacion laser realizada. Prueba de carga satisfactoria.', '2026-08-05 08:30:00'),
(@idMantenimiento3, @idUserFernando, 6.00, 'Frenos purgados y probados en rampa. Se recomienda programar cambio de liquido general.', '2026-09-10 09:00:00');

-- ============================================================================
-- 13. TABLA: evidenciaMantenimiento (Fotos y Facturas)
-- ============================================================================
INSERT INTO evidenciaMantenimiento (idMantenimiento, urlArchivo, nombreArchivo, tipoEvidencia, descripcion, fechaHoraCaptura) VALUES
(@idMantenimiento1, 'https://transandinastorage.blob.core.windows.net/evidencias/factura_m1_45000km.pdf', 'factura_m1_45000km.pdf', 'factura', 'Factura fiscal electronica #FE-8942 por repuestos y lubricantes', '2026-07-10 11:30:00'),
(@idMantenimiento1, 'https://transandinastorage.blob.core.windows.net/evidencias/pastillas_freno_m1.jpg', 'pastillas_freno_m1.jpg', 'foto', 'Fotografia de pastillas de freno nuevas instaladas', '2026-07-10 10:45:00'),
(@idMantenimiento2, 'https://transandinastorage.blob.core.windows.net/evidencias/factura_m2_amortiguadores.pdf', 'factura_m2_amortiguadores.pdf', 'factura', 'Factura electronica #FE-9120 Servicentro Diesel repuestos de suspension', '2026-08-05 16:15:00'),
(@idMantenimiento2, 'https://transandinastorage.blob.core.windows.net/evidencias/amortiguador_nuevo_m2.jpg', 'amortiguador_nuevo_m2.jpg', 'foto', 'Fotografia de amortiguador delantero instalado', '2026-08-05 15:20:00'),
(@idMantenimiento3, 'https://transandinastorage.blob.core.windows.net/evidencias/tambores_rectificados_m3.jpg', 'tambores_rectificados_m3.jpg', 'foto', 'Tambores rectificados en rampa antes de cierre de ruedas', '2026-09-10 14:10:00');

-- ============================================================================
-- 14. TABLA: registroKilometraje (Bitacora de odometro)
-- ============================================================================
INSERT INTO registroKilometraje (placa, idConductor, kilometraje, fechaRegistro) VALUES
('DDD-123', @idUserJuan, 47200, '2026-08-25 07:30:00'),
('DDD-123', @idUserJuan, 48500, '2026-09-12 18:00:00'),
('DDD-124', @idUserEstefannia, 183000, '2026-08-28 06:45:00'),
('DDD-124', @idUserEstefannia, 185200, '2026-09-11 19:30:00'),
('DDD-125', @idUserFrancisco, 91000, '2026-08-20 08:00:00'),
('DDD-125', @idUserFrancisco, 92400, '2026-09-09 17:15:00'),
('DDD-126', @idUserLeticia, 30100, '2026-08-30 09:00:00'),
('DDD-126', @idUserLeticia, 31000, '2026-09-12 16:45:00');

-- ============================================================================
-- 15. TABLA: notificacion
-- ============================================================================
INSERT INTO notificacion (placa, titulo, mensaje, tipoAlerta, prioridad, fechaCreacion) VALUES
('DDD-125', 'RTV Proximo a Vencer', 'El vehiculo Isuzu Forward placa DDD-125 tiene la revision tecnica vehicular programada para el 30/09/2026.', 'documentacion', 'urgente', '2026-09-01 08:00:00'),
('DDD-123', 'Mantenimiento Preventivo Proximo', 'El vehiculo Toyota Hilux placa DDD-123 alcanzara 50,000 km pronto. Programar cambio de aceite y filtros.', 'mantenimiento', 'importante', '2026-09-10 09:15:00'),
(NULL, 'Revision General de Seguridad', 'Recordatorio a todo el personal: inspeccionar vigencia de extintores y botiquines en todas las unidades asignadas.', 'general', 'general', '2026-09-12 07:00:00');

-- ============================================================================
-- 16. TABLA INTERMEDIA: usuarioxnotificacion
-- ============================================================================
DECLARE @idNotif1 INT = (SELECT idNotificacion FROM notificacion WHERE titulo = 'RTV Proximo a Vencer');
DECLARE @idNotif2 INT = (SELECT idNotificacion FROM notificacion WHERE titulo = 'Mantenimiento Preventivo Proximo');
DECLARE @idNotif3 INT = (SELECT idNotificacion FROM notificacion WHERE titulo = 'Revision General de Seguridad');

INSERT INTO usuarioxnotificacion (idNotificacion, idUsuario, leida, fechaLectura) VALUES
-- Notificacion 1 (Urgente RTV DDD-125)
(@idNotif1, @idUserDayanna, 1, '2026-09-01 08:30:00'),
(@idNotif1, @idUserFrancisco, 0, NULL),
-- Notificacion 2 (Mantenimiento DDD-123)
(@idNotif2, @idUserDayanna, 1, '2026-09-10 09:30:00'),
(@idNotif2, @idUserJuan, 1, '2026-09-10 10:15:00'),
(@idNotif2, @idUserLeonardo, 0, NULL),
-- Notificacion 3 (General de Seguridad)
(@idNotif3, @idUserDayanna, 1, '2026-09-12 07:15:00'),
(@idNotif3, @idUserJuan, 1, '2026-09-12 08:00:00'),
(@idNotif3, @idUserEstefannia, 0, NULL),
(@idNotif3, @idUserFrancisco, 0, NULL),
(@idNotif3, @idUserLeticia, 1, '2026-09-12 09:30:00'),
(@idNotif3, @idUserLeonardo, 0, NULL),
(@idNotif3, @idUserEduardo, 0, NULL);
