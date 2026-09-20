package com.example.transandina_app.data.repository

import com.example.transandina_app.data.database.*
import com.example.transandina_app.data.model.*
import java.sql.Types
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificacionRepository {
    companion object {
        private val revision = MutableStateFlow(0L)
        val cambios = revision.asStateFlow()
    }
    suspend fun obtenerNotificaciones(placa: String? = null): Result<List<NotificacionVehiculoItem>> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerNotificacionesVehiculo @IdUsuario=?, @Placa=?, @DesfaseUtcMinutos=?").use { sp ->
                sp.setInt(1, usuario)
                val filtro = placa?.trim()?.takeIf { it.isNotEmpty() }
                if (filtro == null) sp.setNull(2, Types.NVARCHAR) else sp.setString(2, filtro)
                sp.setInt(3, ConductorSqlClient.desfaseUtcMinutos)
                val filas = mutableListOf<NotificacionVehiculoItem>()
                sp.leerResultados { _, rs ->
                    while (rs.next()) filas.add(NotificacionVehiculoItem(
                        rs.getString("id"), rs.getString("placa"),
                        TipoNotificacion.valueOf(rs.getString("tipo").uppercase(Locale.ROOT)),
                        rs.getString("titulo"), rs.getString("descripcion"), rs.getString("fecha"),
                        rs.getLong("fechaMillis"), rs.getBoolean("leida")
                    ))
                }
                filas
            }
        }
    }
    suspend fun obtenerNoLeidas(): Result<Int> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerResumenInicioConductor @IdUsuario=?").use { sp ->
                sp.setInt(1, usuario)
                var cantidad: Int? = null
                sp.leerResultados { _, rs -> if (rs.next()) cantidad = rs.getInt("notificacionesNoLeidas") }
                requireNotNull(cantidad)
            }
        }
    }
    suspend fun marcarLeida(id: String): Result<Unit> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor("EXEC dbo.sp_MarcarNotificacionLeida @IdUsuario=?, @IdNotificacion=?").use { sp ->
                sp.setInt(1, usuario)
                sp.setInt(2, requireNotNull(id.toIntOrNull()) { "Identificador de notificación no válido." })
                var confirmada = false
                sp.leerResultados { _, rs -> if (rs.next()) confirmada = rs.getBoolean("leida") }
                check(confirmada) { "No se pudo confirmar la lectura de la notificación." }
            }
        }
        revision.update { it + 1 }
    }
}
