package com.example.transandina_app.data.repository

import com.example.transandina_app.data.database.*
import com.example.transandina_app.data.model.*
import java.sql.Date

class KilometrajeRepository {
    suspend fun registrarKilometraje(registro: KilometrajeRegistro): Result<Unit> = resultadoSqlConductor {
        require(registro.placa.isNotBlank() && registro.kilometrajeActual >= 0) { "Revisa la placa y el kilometraje." }
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor("EXEC dbo.sp_RegistrarKilometraje @Placa=?, @Fecha=?, @KilometrajeActual=?, @IdConductor=?").use { sp ->
                sp.setString(1, registro.placa.trim())
                sp.setDate(2, Date.valueOf(registro.fecha))
                sp.setInt(3, registro.kilometrajeActual)
                sp.setInt(4, usuario)
                var id: Int? = null
                sp.leerResultados { _, rs -> if (rs.next()) id = rs.getInt("idRegistroKilometraje") }
                check((id ?: 0) > 0) { "La base de datos no confirmó el registro de kilometraje." }
            }
        }
    }
    suspend fun obtenerResumenKilometraje(placa: String): Result<KilometrajeResumen> = resultadoSqlConductor {
        require(placa.isNotBlank()) { "Selecciona un vehículo para consultar la gráfica." }
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerHistorialKilometraje @Placa=?, @IdConductor=?, @Meses=?").use { sp ->
                sp.setString(1, placa.trim())
                sp.setInt(2, usuario)
                sp.setInt(3, 6)
                var placaResultado: String? = null
                var total: Int? = null
                var restante: Int? = null
                val puntos = mutableListOf<KilometrajePuntoHistorial>()
                val cantidad = sp.leerResultados { indice, rs ->
                    when (indice) {
                        0 -> {
                            check(rs.next()) { "No se recibió el resumen de kilometraje." }
                            placaResultado = rs.getString("placa")
                            total = rs.getInt("totalHistoricoRecorrido")
                            restante = rs.enteroNullable("kilometrajeRestanteMantenimiento")
                            check(!rs.next()) { "Se recibieron varios resúmenes para una placa." }
                        }
                        1 -> while (rs.next()) puntos.add(KilometrajePuntoHistorial(
                            rs.getString("periodo"), rs.getInt("kilometrajeActual"), rs.enteroNullable("kilometrajeRestante")
                        ))
                        else -> error("El procedimiento devolvió un resultado inesperado.")
                    }
                }
                check(cantidad == 2) { "El procedimiento debe devolver resumen e historial." }
                KilometrajeResumen(requireNotNull(placaResultado), requireNotNull(total), restante, puntos)
            }
        }
    }
}
