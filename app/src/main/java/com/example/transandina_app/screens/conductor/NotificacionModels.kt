package com.example.transandina_app.data.model

import androidx.compose.ui.graphics.Color

/**
 * Nivel/etiqueta de una notificación, con los colores de badge usados en la pantalla
 * "Notificaciones del vehículo".
 */
enum class TipoNotificacion(
    val label: String,
    val bgBadge: Color,
    val textBadge: Color
) {
    GENERAL("General", Color(0xFFDBEAFE), Color(0xFF1D4ED8)),
    IMPORTANTE("Importante", Color(0xFFFEF3C7), Color(0xFFB45309)),
    URGENTE("Urgente", Color(0xFFFEE2E2), Color(0xFFDC2626))
}

/**
 * Notificación puntual asociada a un vehículo (mantenimiento próximo, documentos vencidos,
 * reasignación, etc.). Se obtiene con sp_ObtenerNotificacionesVehiculo (@Placa).
 */
data class NotificacionVehiculoItem(
    val id: String,
    val placa: String?,
    val tipo: TipoNotificacion,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val fechaMillis: Long,
    val leida: Boolean = false
)
