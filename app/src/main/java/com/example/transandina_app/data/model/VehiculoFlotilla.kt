package com.example.transandina_app.data.model

import com.example.transandina_app.screens.admin.EstadoDocumento
import com.example.transandina_app.screens.admin.EstadoSemaforo
import com.example.transandina_app.screens.admin.FichaVehicular
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class VehiculoFlotilla(
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val capacidad: String,
    val kilometrajeActual: Int,
    val vencimientoRtv: String,
    val vencimientoMarchamo: String,
    val vencimientoSeguro: String,
    val estadoOperativo: String,
    val tipoVehiculo: String,
    val conductorActual: String?
) {
    val nombreCompleto: String
        get() = "$marca $modelo $anio"

    val estadoSemaforo: EstadoSemaforo
        get() {
            val rtvDoc = calcularEstadoDocumento(vencimientoRtv)
            val marchamoDoc = calcularEstadoDocumento(vencimientoMarchamo)
            val seguroDoc = calcularEstadoDocumento(vencimientoSeguro)

            return when {
                rtvDoc == EstadoDocumento.VENCIDO || marchamoDoc == EstadoDocumento.VENCIDO || seguroDoc == EstadoDocumento.VENCIDO -> EstadoSemaforo.ATRASADO
                rtvDoc == EstadoDocumento.PROXIMO || marchamoDoc == EstadoDocumento.PROXIMO || seguroDoc == EstadoDocumento.PROXIMO -> EstadoSemaforo.PROXIMO
                else -> EstadoSemaforo.AL_DIA
            }
        }

    fun toFichaVehicular(): FichaVehicular {
        val marchamoDoc = calcularEstadoDocumento(vencimientoMarchamo)
        val rtvDoc = calcularEstadoDocumento(vencimientoRtv)
        val seguroDoc = calcularEstadoDocumento(vencimientoSeguro)

        return FichaVehicular(
            placa = placa,
            marca = marca,
            modelo = modelo,
            anio = anio,
            estadoSemaforo = estadoSemaforo,
            estadoOperativo = when (estadoOperativo.lowercase()) {
                "activo" -> "Activo"
                "en_taller" -> "En Taller"
                else -> "Inactivo"
            },
            conductorActual = conductorActual ?: "Sin conductor asignado",
            capacidad = capacidad,
            tipoVehiculo = tipoVehiculo,
            marchamoVence = formatearFecha(vencimientoMarchamo),
            marchamoEstado = marchamoDoc,
            rtvVence = formatearFecha(vencimientoRtv),
            rtvEstado = rtvDoc,
            seguroEstado = if (seguroDoc == EstadoDocumento.PROXIMO) "Póliza por renovar" else "Póliza Activa (INS)",
            seguroEstadoDoc = seguroDoc,
            kilometrajeActual = String.format(Locale.getDefault(), "%,d km", kilometrajeActual)
        )
    }

    companion object {
        fun calcularEstadoDocumento(fechaStr: String): EstadoDocumento {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fecha = sdf.parse(fechaStr.take(10)) ?: return EstadoDocumento.AL_DIA
                val hoy = Date()
                val diffInMillis = fecha.time - hoy.time
                val diasRestantes = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                when {
                    diasRestantes < 0 -> EstadoDocumento.VENCIDO
                    diasRestantes <= 30 -> EstadoDocumento.PROXIMO
                    else -> EstadoDocumento.AL_DIA
                }
            } catch (e: Exception) {
                EstadoDocumento.AL_DIA
            }
        }

        fun formatearFecha(fechaStr: String): String {
            return try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fecha = inputSdf.parse(fechaStr.take(10)) ?: return fechaStr
                outputSdf.format(fecha)
            } catch (e: Exception) {
                fechaStr
            }
        }
    }
}
