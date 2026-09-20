package com.example.transandina_app.data.model

/**
 * Payload enviado al backend para registrar el kilometraje reportado por el conductor.
 * Corresponde a los parámetros esperados por el stored procedure sp_RegistrarKilometraje.
 */
data class KilometrajeRegistro(
    val placa: String,
    val fecha: String,          // Formato "yyyy-MM-dd"
    val kilometrajeActual: Int
)

/**
 * Un punto del histórico de kilometraje, usado para dibujar cada barra de la gráfica.
 * "periodo" es la etiqueta que se muestra en el eje X (ej. "Ago", "Sep", "Oct").
 */
data class KilometrajePuntoHistorial(
    val periodo: String,
    val kilometrajeActual: Int,
    val kilometrajeRestante: Int?
)

/**
 * Resumen completo que consume la pantalla "Gráfica de Kilometraje".
 * Se obtiene ejecutando sp_ObtenerHistorialKilometraje (@Placa) en el backend.
 */
data class KilometrajeResumen(
    val placa: String,
    val totalHistoricoRecorrido: Int,
    val kilometrajeRestanteMantenimiento: Int?,
    val historial: List<KilometrajePuntoHistorial>
)
