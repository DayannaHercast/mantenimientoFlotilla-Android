package com.example.transandina_app.data.repository

import com.example.transandina_app.data.db.DatabaseConfig
import com.example.transandina_app.screens.admin.Conductor
import com.example.transandina_app.screens.admin.EstadoConductor
import com.example.transandina_app.screens.admin.Mecanico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

class UsuarioRepository {

    suspend fun obtenerConductores(): Result<List<Conductor>> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Conductor>()
        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("EXEC sp_ObtenerConductores").use { rs ->
                        while (rs.next()) {
                            val estadoStr = rs.getString("estado") ?: "activo"
                            val estadoEnum = when (estadoStr.lowercase()) {
                                "inactivo" -> EstadoConductor.INACTIVO
                                "suspendido" -> EstadoConductor.SUSPENDIDO
                                else -> EstadoConductor.ACTIVO
                            }
                            lista.add(
                                Conductor(
                                    id = rs.getInt("idUsuario"),
                                    nombreCompleto = rs.getString("nombreCompleto") ?: "${rs.getString("nombre")} ${rs.getString("primerApellido")}",
                                    estado = estadoEnum
                                )
                            )
                        }
                    }
                }
            }
            Result.success(lista)
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al cargar conductores: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }

    suspend fun obtenerMecanicos(): Result<List<Mecanico>> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Mecanico>()
        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("EXEC sp_ObtenerMecanicos").use { rs ->
                        while (rs.next()) {
                            val estadoStr = rs.getString("estado") ?: "activo"
                            val estadoEnum = when (estadoStr.lowercase()) {
                                "inactivo" -> EstadoConductor.INACTIVO
                                "suspendido" -> EstadoConductor.SUSPENDIDO
                                else -> EstadoConductor.ACTIVO
                            }
                            lista.add(
                                Mecanico(
                                    id = rs.getInt("idUsuario"),
                                    nombreCompleto = rs.getString("nombreCompleto") ?: "${rs.getString("nombre")} ${rs.getString("primerApellido")}",
                                    estado = estadoEnum
                                )
                            )
                        }
                    }
                }
            }
            Result.success(lista)
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al cargar mecánicos: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }

    suspend fun actualizarEstadoUsuario(idUsuario: Int, nuevoEstado: EstadoConductor): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val nuevoEstadoSanitizado = nuevoEstado.etiqueta.lowercase().replace("'", "''")
            val sql = "EXEC sp_ActualizarEstadoUsuario @idUsuario = $idUsuario, @nuevoEstado = '$nuevoEstadoSanitizado'"
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sql)
                }
            }
            Result.success(true)
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al actualizar estado: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }

    suspend fun obtenerConductoresDisponibles(placaActual: String? = null): Result<List<Conductor>> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Conductor>()
        try {
            val sql = if (!placaActual.isNullOrBlank()) {
                val placaSanitizada = placaActual.trim().replace("'", "''")
                "EXEC sp_ObtenerConductoresDisponibles @placaActual = '$placaSanitizada'"
            } else {
                "EXEC sp_ObtenerConductoresDisponibles"
            }
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            lista.add(
                                Conductor(
                                    id = rs.getInt("idUsuario"),
                                    nombreCompleto = rs.getString("nombreCompleto"),
                                    estado = EstadoConductor.ACTIVO
                                )
                            )
                        }
                    }
                }
            }
            Result.success(lista)
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al cargar conductores disponibles: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }
}
