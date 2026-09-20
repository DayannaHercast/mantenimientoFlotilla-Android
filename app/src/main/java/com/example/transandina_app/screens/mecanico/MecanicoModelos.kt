package com.example.transandina_app.data.model

import java.math.BigDecimal

/** Vehiculo disponible para que el mecanico reporte una intervencion (pantalla "Registro Mantenimientos"). */
data class VehiculoMecanico(
    val placa: String,
    val nombreVehiculo: String,
    val kilometrajeActual: Int
)

/** Ficha resumida que se muestra en el historial: "Nombre del usuario / ID / Activo desde". */
data class ResumenMecanico(
    val id: Int,
    val nombreUsuario: String,
    val activoDesde: String
)

/** Una fila de la tabla de historial: Fecha, Servicio, Taller, Costo. */
data class IntervencionMecanico(
    val id: String,
    val fecha: String,
    val fechaMillis: Long,
    val placa: String,
    val servicio: String,
    val taller: String,
    val costo: BigDecimal
)

/**
 * Estado del flujo de registro de mantenimiento del mecanico, acumulado entre
 * la pantalla "Registro Mantenimientos" (paso 1: vehiculo, fecha, tipo) y
 * "Registro de mantenimiento" (paso 2: kilometraje, categoria, taller, costo,
 * descripcion). Se mantiene en MainActivity igual que "selectedFicha" para el
 * flujo de admin.
 */
data class MecanicoRegistroState(
    val placa: String? = null,
    val nombreVehiculo: String? = null,
    val fechaMillis: Long? = null,
    val tipoServicio: String? = null,
    val kilometrajeActual: Int? = null,
    val categoria: String? = null,
    val taller: String? = null,
    val costo: String? = null,
    val descripcion: String? = null,
    val comprobantesUris: List<String> = emptyList()
)
