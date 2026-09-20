package com.example.transandina_app.data.model

/**
 * Datos que recibe el repositorio JDBC para registrar un nuevo mantenimiento de flotilla.
 * Corresponde a los parámetros esperados por el stored procedure sp_RegistrarMantenimientoConArchivos.
 *
 * "tipoServicio" usa las mismas etiquetas del enum TipoServicioConductor (PREVENTIVO / CORRECTIVO)
 * definido en ConductorSqlModels.kt, para mantener un único catálogo en toda la app.
 */
data class MantenimientoRegistro(
    val placa: String,
    val kilometrajeActual: Int,
    val tipoServicio: String,          // "PREVENTIVO" o "CORRECTIVO"
    val categoria: String,             // Ej. "Cambio de aceite", "Frenos", "Llantas"...
    val fecha: String,                 // Formato "yyyy-MM-dd"
    val taller: String,
    val costo: java.math.BigDecimal,
    val descripcion: String,
    val comprobantesUris: List<String> = emptyList() // Rutas/URIs locales de las fotos adjuntas
)
