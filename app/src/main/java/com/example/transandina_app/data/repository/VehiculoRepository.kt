package com.example.transandina_app.data.repository

import com.example.transandina_app.data.db.DatabaseConfig
import com.example.transandina_app.data.model.VehiculoFlotilla
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

class VehiculoRepository {

    suspend fun obtenerFlotilla(): Result<List<VehiculoFlotilla>> = withContext(Dispatchers.IO) {
        val sql = "EXEC sp_ObtenerFlotilla"

        val lista = mutableListOf<VehiculoFlotilla>()

        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            lista.add(
                                VehiculoFlotilla(
                                    placa = rs.getString("placa"),
                                    marca = rs.getString("marca"),
                                    modelo = rs.getString("modelo"),
                                    anio = rs.getInt("anio"),
                                    capacidad = rs.getString("capacidad"),
                                    kilometrajeActual = rs.getInt("kilometrajeActual"),
                                    vencimientoRtv = rs.getString("vencimientoRtv") ?: "2026-12-31",
                                    vencimientoMarchamo = rs.getString("vencimientoMarchamo") ?: "2026-12-31",
                                    vencimientoSeguro = rs.getString("vencimientoSeguro") ?: "2026-12-31",
                                    estadoOperativo = rs.getString("estadoOperativo") ?: "activo",
                                    tipoVehiculo = rs.getString("tipoVehiculo") ?: "General",
                                    conductorActual = rs.getString("conductorActual")
                                )
                            )
                        }
                    }
                }
            }
            Result.success(lista)
        } catch (e: SQLException) {
            e.printStackTrace()
            val errorMsg = when {
                e.message?.contains("Client with IP address", ignoreCase = true) == true ->
                    "Error de Firewall Azure: Tu dirección IP no está autorizada en la base de datos."
                else -> "Error de conexión con Azure SQL: ${e.localizedMessage ?: "Tiempo de espera agotado."}"
            }
            Result.failure(Exception(errorMsg, e))
        } catch (t: Throwable) {
            t.printStackTrace()
            val msg = if (t is AssertionError) {
                "Error de protocolo SSL en el driver SQL. Reintentando..."
            } else {
                "Error inesperado: ${t.localizedMessage}"
            }
            Result.failure(Exception(msg, t))
        }
    }
}
