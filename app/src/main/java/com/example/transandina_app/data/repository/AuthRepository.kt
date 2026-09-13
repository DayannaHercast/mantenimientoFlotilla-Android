package com.example.transandina_app.data.repository

import com.example.transandina_app.data.db.DatabaseConfig
import com.example.transandina_app.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.SQLException

class AuthRepository {

    suspend fun login(correo: String, contrasena: String): Result<Usuario> = withContext(Dispatchers.IO) {
        val emailTrimmed = correo.trim()
        val pwdTrimmed = contrasena.trim()

        if (emailTrimmed.isEmpty() || pwdTrimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Por favor ingrese correo y contraseña."))
        }

        val safeEmail = emailTrimmed.replace("'", "''")
        val safePwd = pwdTrimmed.replace("'", "''")
        val sql = "EXEC sp_IniciarSesion @correo = '$safeEmail', @contrasena = '$safePwd'"

        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            val usuario = Usuario(
                                idUsuario = rs.getInt("idUsuario"),
                                nombre = rs.getString("nombre"),
                                primerApellido = rs.getString("primerApellido"),
                                segundoApellido = rs.getString("segundoApellido"),
                                cedula = rs.getString("cedula"),
                                correo = rs.getString("correo"),
                                telefono = rs.getString("telefono"),
                                numLicencia = rs.getString("numLicencia"),
                                tipoLicencia = rs.getString("tipoLicencia"),
                                contrasena = rs.getString("contrasena"),
                                estado = rs.getString("estado"),
                                rol = rs.getString("rol")
                            )
                            Result.success(usuario)
                        } else {
                            Result.failure(Exception("No se recibieron datos del usuario."))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            val errorMsg = when {
                e.message?.contains("Client with IP address", ignoreCase = true) == true ->
                    "Error de Firewall Azure: Tu dirección IP no está autorizada en la base de datos."
                e.message?.contains("login failed", ignoreCase = true) == true ->
                    "Error de autenticación con el servidor de base de datos."
                else -> e.localizedMessage ?: "Tiempo de espera agotado con la base de datos."
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

    suspend fun registrarUsuario(req: RegistroUsuarioRequest): Result<Usuario> = withContext(Dispatchers.IO) {
        val safeNombre = req.nombre.trim().replace("'", "''")
        val safePrimerApellido = req.primerApellido.trim().replace("'", "''")
        val safeSegundoApellido = req.segundoApellido.trim().replace("'", "''")
        val safeCedula = req.cedula.trim().filter { it.isDigit() }
        val safeCorreo = req.correo.trim().lowercase().replace("'", "''")
        val safeTelefono = req.telefono.trim().filter { it.isDigit() }
        val safeContrasena = req.contrasena.trim().replace("'", "''")
        val safeNumLicencia = if (req.numLicencia.isNullOrBlank()) "NULL" else "'${req.numLicencia.trim().filter { it.isDigit() }}'"
        val safeTipoLicencia = if (req.tipoLicencia.isNullOrBlank()) "NULL" else "'${req.tipoLicencia.trim().replace("'", "''")}'"

        val rolNormalizado = when (req.rol.lowercase().trim()) {
            "administrador", "admin" -> "administrador"
            "mecánico", "mecanico" -> "mecanico"
            else -> "conductor"
        }

        if (safeNombre.isEmpty() || safePrimerApellido.isEmpty() || safeSegundoApellido.isEmpty() ||
            safeCedula.isEmpty() || safeCorreo.isEmpty() || safeTelefono.isEmpty() || safeContrasena.isEmpty()
        ) {
            return@withContext Result.failure(IllegalArgumentException("Por favor completa todos los campos obligatorios."))
        }

        val sql = """
            EXEC sp_RegistrarUsuario 
                @nombre = '$safeNombre',
                @primerApellido = '$safePrimerApellido',
                @segundoApellido = '$safeSegundoApellido',
                @cedula = '$safeCedula',
                @correo = '$safeCorreo',
                @telefono = '$safeTelefono',
                @rol = '$rolNormalizado',
                @numLicencia = $safeNumLicencia,
                @tipoLicencia = $safeTipoLicencia,
                @contrasena = '$safeContrasena'
        """.trimIndent()

        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            val usuarioCreado = Usuario(
                                idUsuario = rs.getInt("idUsuario"),
                                nombre = rs.getString("nombre"),
                                primerApellido = rs.getString("primerApellido"),
                                segundoApellido = rs.getString("segundoApellido"),
                                cedula = rs.getString("cedula"),
                                correo = rs.getString("correo"),
                                telefono = rs.getString("telefono"),
                                numLicencia = rs.getString("numLicencia"),
                                tipoLicencia = rs.getString("tipoLicencia"),
                                contrasena = rs.getString("contrasena"),
                                estado = rs.getString("estado"),
                                rol = rs.getString("rol")
                            )
                            Result.success(usuarioCreado)
                        } else {
                            Result.failure(Exception("No se pudo obtener la confirmación del usuario creado."))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            val msg = e.localizedMessage ?: "Error de base de datos al registrar usuario."
            Result.failure(Exception(msg, e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado al registrar usuario: ${t.localizedMessage}", t))
        }
    }
}

data class RegistroUsuarioRequest(
    val nombre: String,
    val primerApellido: String,
    val segundoApellido: String,
    val cedula: String,
    val correo: String,
    val telefono: String,
    val rol: String,
    val numLicencia: String? = null,
    val tipoLicencia: String? = null,
    val contrasena: String
)
