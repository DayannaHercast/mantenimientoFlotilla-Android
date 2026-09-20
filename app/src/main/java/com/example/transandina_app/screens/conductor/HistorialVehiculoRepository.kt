package com.example.transandina_app.data.repository

import com.example.transandina_app.data.database.*
import com.example.transandina_app.data.model.*

class HistorialVehiculoRepository {
    suspend fun buscarVehiculo(placa: String): Result<HistorialVehiculoInfo> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerVehiculoPorPlaca @Placa=?, @IdConductor=?").use { sp ->
                sp.setString(1, placa.trim())
                sp.setInt(2, usuario)
                var vehiculo: HistorialVehiculoInfo? = null
                sp.leerResultados { _, rs ->
                    if (rs.next()) vehiculo = HistorialVehiculoInfo(rs.getString("placa"), rs.getString("nombreVehiculo"), rs.getInt("kilometrajeActual"))
                }
                checkNotNull(vehiculo) { "No se encontró el vehículo asignado." }
            }
        }
    }
    suspend fun obtenerHistorialServicios(placa: String): Result<List<HistorialServicioItem>> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerHistorialServicios @Placa=?, @IdConductor=?, @DesfaseUtcMinutos=?").use { sp ->
                sp.setString(1, placa.trim())
                sp.setInt(2, usuario)
                sp.setInt(3, ConductorSqlClient.desfaseUtcMinutos)
                val filas = mutableListOf<HistorialServicioItem>()
                sp.leerResultados { _, rs ->
                    while (rs.next()) filas.add(HistorialServicioItem(
                        rs.getString("id"), rs.getString("fecha"), rs.getLong("fechaMillis"),
                        rs.getString("servicio"), rs.getString("taller"), rs.getBigDecimal("costo")
                    ))
                }
                filas
            }
        }
    }
}
