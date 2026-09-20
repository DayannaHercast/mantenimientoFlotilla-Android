package com.example.transandina_app.data.model

/**
 * Datos generales del vehículo que se muestran en el encabezado de la ficha de historial
 * (se obtienen con sp_ObtenerVehiculoPorPlaca).
 */
data class HistorialVehiculoInfo(
    val placa: String,
    val nombreVehiculo: String,
    val kilometrajeActual: Int
)

/**
 * Una fila del historial de servicios (tabla Fecha / Servicio / Taller / Costo).
 * Se obtiene con sp_ObtenerHistorialServicios (@Placa).
 */
data class HistorialServicioItem(
    val id: String,
    val fecha: String,
    val fechaMillis: Long,
    val servicio: String,
    val taller: String,
    val costo: java.math.BigDecimal
)
