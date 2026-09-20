package com.example.transandina_app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.example.transandina_app.data.database.*
import com.example.transandina_app.data.model.MantenimientoRegistro
import java.io.ByteArrayOutputStream
import java.sql.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MantenimientoRegistroRepository {
    suspend fun registrarMantenimiento(registro: MantenimientoRegistro, context: Context): Result<Unit> = resultadoSqlConductor {
        require(registro.costo.signum() >= 0 && registro.costo.scale() <= 2) { "El costo debe tener como máximo dos decimales." }
        require(registro.comprobantesUris.size <= 5) { "Selecciona un máximo de 5 comprobantes." }
        val archivos = withContext(Dispatchers.IO) { prepararArchivos(context.applicationContext, registro.comprobantesUris) }
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor(
                "EXEC dbo.sp_RegistrarMantenimientoConArchivos " +
                    "@Placa=?, @KilometrajeActual=?, @TipoServicio=?, @Categoria=?, @Fecha=?, " +
                    "@Taller=?, @Costo=?, @Descripcion=?, @IdUsuarioRegistra=?, @ArchivosJson=?"
            ).use { sp ->
                sp.setString(1, registro.placa.trim())
                sp.setInt(2, registro.kilometrajeActual)
                sp.setString(3, registro.tipoServicio)
                sp.setString(4, registro.categoria)
                sp.setDate(5, Date.valueOf(registro.fecha))
                sp.setString(6, registro.taller.trim())
                sp.setBigDecimal(7, registro.costo)
                sp.setString(8, registro.descripcion.trim())
                sp.setInt(9, usuario)
                sp.setString(10, archivos)
                var id: Int? = null
                var cantidad: Int? = null
                sp.leerResultados(timeoutSegundos = 60) { _, rs ->
                    if (rs.next()) {
                        id = rs.getInt("idMantenimiento")
                        cantidad = rs.getInt("cantidadComprobantes")
                    }
                }
                check((id ?: 0) > 0 && cantidad == registro.comprobantesUris.size) {
                    "No se recibió confirmación completa. Verifica el historial antes de repetir el envío."
                }
            }
        }
    }
    private fun prepararArchivos(context: Context, referencias: List<String>): String {
        val archivos = JSONArray()
        var total = 0
        referencias.forEachIndexed { indice, referencia ->
            val uri = Uri.parse(referencia)
            require(uri.scheme == "content") { "Vuelve a seleccionar los comprobantes desde el dispositivo." }
            val resolver = context.contentResolver
            val tipo = resolver.getType(uri).orEmpty()
            require(tipo.startsWith("image/") && tipo.length <= 100) { "Los comprobantes deben ser imágenes." }
            var nombre = "comprobante-" + (indice + 1)
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columna = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columna >= 0) nombre = cursor.getString(columna) ?: nombre
                }
            }
            nombre = nombre.replace(Regex("[\\r\\n\\\\/]"), "_").take(255)
            val bytes = ByteArrayOutputStream().use { salida ->
                requireNotNull(resolver.openInputStream(uri)) { "No se pudo abrir un comprobante. Vuelve a seleccionarlo." }.use { entrada ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val leidos = entrada.read(buffer)
                        if (leidos == -1) break
                        require(salida.size() + leidos <= 5 * 1024 * 1024) { "Cada comprobante puede ocupar hasta 5 MB." }
                        require(total + salida.size() + leidos <= 10 * 1024 * 1024) { "Los comprobantes juntos pueden ocupar hasta 10 MB." }
                        salida.write(buffer, 0, leidos)
                    }
                }
                salida.toByteArray()
            }
            require(bytes.isNotEmpty()) { "Un comprobante está vacío." }
            total += bytes.size
            archivos.put(JSONObject()
                .put("nombreArchivo", nombre)
                .put("tipoMime", tipo)
                .put("tipoEvidencia", "foto")
                .put("contenidoBase64", Base64.encodeToString(bytes, Base64.NO_WRAP)))
        }
        return archivos.toString()
    }
}
