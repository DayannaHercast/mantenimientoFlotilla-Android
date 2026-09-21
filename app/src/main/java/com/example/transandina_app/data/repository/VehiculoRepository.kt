package com.example.transandina_app.data.repository

import com.example.transandina_app.data.db.DatabaseConfig
import com.example.transandina_app.data.model.VehiculoFlotilla
import com.example.transandina_app.screens.admin.EstadoVehiculo
import com.example.transandina_app.screens.admin.FichaTecnicaVehiculo
import com.example.transandina_app.screens.admin.VehiculoEstadoInicial
import com.example.transandina_app.screens.admin.VehiculoFlota
import com.example.transandina_app.screens.admin.VehiculoInfoGeneral
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Date
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.transandina_app.screens.admin.MantenimientoItem
import com.example.transandina_app.screens.admin.TipoMantenimiento
import com.example.transandina_app.screens.admin.EvidenciaDetalle
import org.json.JSONArray

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

    suspend fun obtenerFlotillaAsignacion(): Result<List<VehiculoFlota>> = withContext(Dispatchers.IO) {
        val sql = "EXEC sp_ObtenerFlotilla"
        val lista = mutableListOf<VehiculoFlota>()

        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        var idIndex = 1
                        while (rs.next()) {
                            val estadoStr = rs.getString("estadoOperativo") ?: "activo"
                            val estadoEnum = if (estadoStr.equals("inactivo", ignoreCase = true)) {
                                EstadoVehiculo.INACTIVO
                            } else {
                                EstadoVehiculo.ACTIVO
                            }
                            lista.add(
                                VehiculoFlota(
                                    id = idIndex++,
                                    placa = rs.getString("placa"),
                                    marca = rs.getString("marca"),
                                    modelo = rs.getString("modelo"),
                                    estado = estadoEnum,
                                    conductorActual = rs.getString("conductorActual") ?: "Sin asignar"
                                )
                            )
                        }
                    }
                }
            }
            Result.success(lista)
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al cargar flotilla: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }

    suspend fun obtenerFichaTecnica(placa: String): Result<FichaTecnicaVehiculo> = withContext(Dispatchers.IO) {
        try {
            val placaSanitizada = placa.trim().replace("'", "''")
            val sql = "EXEC sp_ObtenerFichaTecnicaVehiculo @placa = '$placaSanitizada'"
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            val estadoOp = rs.getString("estadoOperativo") ?: "Activo"
                            val estadoDisplay = estadoOp.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            val kmInt = rs.getInt("kilometrajeActual")
                            val rtv = rs.getString("vencimientoRtv") ?: ""
                            val marchamo = rs.getString("vencimientoMarchamo") ?: ""
                            val seguro = rs.getString("vencimientoSeguro") ?: ""
                            val tipo = rs.getString("tipoVehiculo") ?: ""
                            val ficha = FichaTecnicaVehiculo(
                                placa = rs.getString("placa") ?: placa,
                                marca = rs.getString("marca") ?: "",
                                modelo = rs.getString("modelo") ?: "",
                                estado = estadoDisplay,
                                conductorActual = rs.getString("conductorActual") ?: "Sin asignar",
                                anio = rs.getInt("anio").toString(),
                                capacidad = rs.getString("capacidad") ?: "",
                                tipoVehiculo = tipo,
                                kilometrajeActual = if (kmInt > 0) kmInt.toString() else "",
                                vencimientoRtv = rtv,
                                vencimientoMarchamo = marchamo,
                                vencimientoSeguro = seguro,
                                vencimientoDocumentos = rtv.ifBlank { marchamo }.ifBlank { "DD/MM/AAAA" },
                                kilometraje = if (kmInt > 0) "$kmInt KM" else "0 KM"
                            )
                            Result.success(ficha)
                        } else {
                            Result.failure(Exception("No se encontró información técnica para el vehículo con placa $placa."))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al cargar ficha técnica: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }

    suspend fun reasignarConductor(placa: String, idNuevoConductor: Int, idAsignador: Int = 1): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val placaSanitizada = placa.trim().replace("'", "''")
            val sql = "EXEC sp_ReasignarConductorVehiculo @placa = '$placaSanitizada', @idNuevoConductor = $idNuevoConductor, @idAsignador = $idAsignador"
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sql)
                }
            }
            Result.success(true)
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al reasignar conductor: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }

    suspend fun actualizarVehiculo(
        placa: String,
        infoGeneral: VehiculoInfoGeneral,
        estadoInicial: VehiculoEstadoInicial,
        idAsignador: Int = 1
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            fun parseFechaSql(fechaStr: String): String {
                return try {
                    val clean = fechaStr.trim()
                    if (clean.contains("/")) {
                        val parts = clean.split("/")
                        if (parts.size == 3) {
                            val d = parts[0].padStart(2, '0')
                            val m = parts[1].padStart(2, '0')
                            val y = parts[2]
                            "'$y-$m-$d'"
                        } else "NULL"
                    } else if (clean.contains("-")) {
                        "'$clean'"
                    } else "NULL"
                } catch (_: Exception) {
                    "NULL"
                }
            }

            fun sqlStr(s: String?): String {
                return if (s.isNullOrBlank()) "NULL" else "'${s.trim().replace("'", "''")}'"
            }

            val placaSql = sqlStr(placa)
            val marcaSql = sqlStr(infoGeneral.marca)
            val modeloSql = sqlStr(infoGeneral.modelo)
            val anioInt = infoGeneral.anio.filter { it.isDigit() }.toIntOrNull()
            val anioSql = anioInt?.toString() ?: "NULL"
            val capacidadSql = sqlStr(infoGeneral.capacidad)
            val tipoVehiculoSql = sqlStr(infoGeneral.tipoVehiculo)
            val kmInt = estadoInicial.kilometrajeActual.filter { it.isDigit() }.toIntOrNull()
            val kmSql = kmInt?.toString() ?: "NULL"

            val rtvSql = parseFechaSql(estadoInicial.vencimientoRevisionTecnica)
            val marchamoSql = parseFechaSql(estadoInicial.vencimientoMarchamo)
            val seguroSql = parseFechaSql(estadoInicial.vencimientoSeguro)
            val estadoOpSql = sqlStr(estadoInicial.estadoVehiculo?.etiqueta?.lowercase())

            val conductorId = infoGeneral.conductorAsignado?.id
            val conductorSql = conductorId?.toString() ?: "NULL"

            val sql = """
                EXEC sp_ActualizarVehiculo 
                    @placa = $placaSql,
                    @marca = $marcaSql,
                    @modelo = $modeloSql,
                    @anio = $anioSql,
                    @capacidad = $capacidadSql,
                    @tipoVehiculo = $tipoVehiculoSql,
                    @kilometrajeActual = $kmSql,
                    @vencimientoRtv = $rtvSql,
                    @vencimientoMarchamo = $marchamoSql,
                    @vencimientoSeguro = $seguroSql,
                    @estadoOperativo = $estadoOpSql,
                    @idConductor = $conductorSql,
                    @idAsignador = $idAsignador
            """.trimIndent()

            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sql)
                }
            }
            Result.success(true)
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.failure(Exception("Error al actualizar vehículo: ${e.localizedMessage}", e))
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(Exception("Error inesperado: ${t.localizedMessage}", t))
        }
    }

    /**
     * Consulta todos los mantenimientos registrados en la flotilla junto con sus evidencias reales desde Azure SQL.
     */
    suspend fun obtenerReporteMantenimientosFlotilla(): Result<List<MantenimientoItem>> = withContext(Dispatchers.IO) {
        val sql = "EXEC dbo.sp_ObtenerMantenimientosFlotillaAdmin"
        val lista = mutableListOf<MantenimientoItem>()

        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        val displayDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-ES"))
                        val sqlDateParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                        while (rs.next()) {
                            val id = rs.getInt("idMantenimiento").toString()
                            val placa = rs.getString("placa") ?: ""
                            val tipoRaw = rs.getString("tipoServicio") ?: "preventivo"
                            val tipo = if (tipoRaw.contains("correctivo", ignoreCase = true)) {
                                TipoMantenimiento.CORRECTIVO
                            } else {
                                TipoMantenimiento.PREVENTIVO
                            }
                            val taller = rs.getString("taller") ?: "Taller no especificado"
                            val descripcion = rs.getString("descripcion") ?: ""
                            val fechaStr = rs.getString("fechaStr") ?: "2026-01-01"
                            val fechaMillis = rs.getLong("fechaMillis")
                            val costo = rs.getInt("costo")
                            val evidenciasJson = rs.getString("evidenciasJson")

                            val evidenciasNombres = mutableListOf<String>()
                            val evidenciasDetalles = mutableListOf<EvidenciaDetalle>()

                            if (!evidenciasJson.isNullOrBlank()) {
                                try {
                                    val jsonArray = JSONArray(evidenciasJson)
                                    for (i in 0 until jsonArray.length()) {
                                        val obj = jsonArray.getJSONObject(i)
                                        val idEvidencia = obj.optInt("idEvidenciaMantenimiento")
                                        val nombre = obj.optString("nombreArchivo", "evidencia.jpg")
                                        val tipoEv = obj.optString("tipoEvidencia", "foto")
                                        val tieneArchivo = obj.optInt("tieneArchivo", 0) == 1
                                        evidenciasNombres.add(nombre)
                                        evidenciasDetalles.add(
                                            EvidenciaDetalle(
                                                idEvidencia = idEvidencia,
                                                nombreArchivo = nombre,
                                                tipoEvidencia = tipoEv,
                                                tieneArchivo = tieneArchivo
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            val fechaFormateada = try {
                                val d = sqlDateParser.parse(fechaStr)
                                if (d != null) displayDateFormatter.format(d) else fechaStr
                            } catch (_: Exception) {
                                fechaStr
                            }

                            lista.add(
                                MantenimientoItem(
                                    id = id,
                                    placa = placa,
                                    tipo = tipo,
                                    taller = taller,
                                    descripcion = descripcion,
                                    fecha = fechaFormateada,
                                    fechaMillis = fechaMillis,
                                    costo = costo,
                                    evidencias = evidenciasNombres,
                                    evidenciasDetalle = evidenciasDetalles
                                )
                            )
                        }
                    }
                }
            }
            Result.success(lista)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Descarga los bytes de la imagen real almacenada en dbo.archivoEvidenciaConductor para una evidencia específica.
     */
    suspend fun obtenerBytesEvidencia(idEvidencia: Int): Result<ByteArray> = withContext(Dispatchers.IO) {
        val sql = "EXEC dbo.sp_ObtenerArchivoEvidenciaAdmin @IdEvidenciaMantenimiento = $idEvidencia"
        try {
            DatabaseConfig.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            val bytes = rs.getBytes("contenido")
                            if (bytes != null && bytes.isNotEmpty()) {
                                Result.success(bytes)
                            } else {
                                Result.failure(Exception("La evidencia no contiene datos de imagen."))
                            }
                        } else {
                            Result.failure(Exception("No se encontró el archivo de evidencia en la base de datos."))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
