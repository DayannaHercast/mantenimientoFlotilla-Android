package com.example.transandina_app.data.database

import com.example.transandina_app.data.db.DatabaseConfig
import java.io.Closeable
import java.math.BigDecimal
import java.sql.Date
import java.sql.Types
import java.sql.Connection

import java.sql.ResultSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/** Reutiliza el driver y la conexion ya existentes. No contiene credenciales. */
object ConductorSqlClient {
    @Volatile private var conexionFactory: (() -> Connection)? = { DatabaseConfig.getConnection() }
    @Volatile var desfaseUtcMinutos: Int = 0
        set(value) { require(value in -840..840); field = value }
    private val sesion = MutableStateFlow<Int?>(null)
    val usuarioActual = sesion.asStateFlow()
    @Volatile private var versionSesion = 0L

    /** La funcion debe devolver una conexion NUEVA por llamada; el repositorio la cierra. */
    fun configurar(abrirConexion: () -> Connection) { conexionFactory = abrirConexion }

    /** Usar el idUsuario real obtenido por el login, nunca un id fijo o deducido de la placa. */
    @Synchronized fun iniciarSesion(idUsuario: Int) {
        require(idUsuario > 0) { "El identificador de usuario no es válido." }
        versionSesion++
        sesion.value = idUsuario
    }

    @Synchronized fun cerrarSesion() {
        versionSesion++
        sesion.value = null
    }

    suspend fun <T> ejecutar(accion: (Connection, Int) -> T): T {
        val version = versionSesion
        val usuario = checkNotNull(sesion.value) { "Inicia sesión antes de consultar los datos del conductor." }
        val factory = checkNotNull(conexionFactory) { "Falta configurar la conexión SQL del módulo de conductor." }
        return withContext(Dispatchers.IO) {
            coroutineContext.ensureActive()
            check(version == versionSesion) { "La sesión cambió. Vuelve a abrir la pantalla." }
            val resultado = factory().use { conexion ->
                coroutineContext.ensureActive()
                check(version == versionSesion) { "La sesión cambió. Vuelve a abrir la pantalla." }
                check(!conexion.isClosed) { "La conexión SQL está cerrada." }
                // Los SP de escritura gestionan su propia transaccion.
                check(conexion.autoCommit) { "La conexión debe ser nueva y usar autoCommit=true." }
                accion(conexion, usuario)
            }
            coroutineContext.ensureActive()
            check(version == versionSesion) { "La sesión cambió. Vuelve a abrir la pantalla." }
            resultado
        }
    }
}

internal fun ResultSet.enteroNullable(nombre: String): Int? {
    val valor = getInt(nombre)
    return if (wasNull()) null else valor
}

/**
 * Compatibilidad con el driver existente en Android: usa Statement, como admin.
 * Solo acepta EXEC de procedimientos conocidos; serializa valores tipados.
 * No es un PreparedStatement ni usa parametros enlazados por el servidor.
 */
internal fun Connection.prepararConsultaConductor(plantilla: String): ConsultaSqlConductor =
    ConsultaSqlConductor(this, plantilla)

internal class ConsultaSqlConductor(private val conexion: Connection, plantilla: String) : Closeable {
    private val procedimiento: String
    private val nombres: List<String>
    private val valores: Array<String?>
    private var cerrada = false

    init {
        val coincidencia = requireNotNull(
            Regex("""EXEC dbo\.(sp_[A-Za-z]+)(?: (.*))?""").matchEntire(plantilla)
        ) { "Plantilla SQL no permitida." }
        procedimiento = coincidencia.groupValues[1]
        require(procedimiento in setOf(
            "sp_ObtenerVehiculosConductor", "sp_ObtenerCategoriasServicio",
            "sp_ObtenerVehiculoPorPlaca", "sp_ObtenerHistorialServicios",
            "sp_RegistrarKilometraje", "sp_ObtenerHistorialKilometraje",
            "sp_RegistrarMantenimientoConArchivos", "sp_ObtenerNotificacionesVehiculo",
            "sp_ObtenerResumenInicioConductor", "sp_MarcarNotificacionLeida",
            "sp_ObtenerVehiculosActivosMecanico", "sp_RegistrarMantenimientoMecanico",
            "sp_ObtenerResumenMecanico", "sp_ObtenerHistorialMecanico"
        )) { "Procedimiento no permitido." }
        val argumentos = coincidencia.groupValues[2]
        nombres = if (argumentos.isBlank()) emptyList() else argumentos.split(",").map {
            requireNotNull(Regex("""@([A-Za-z][A-Za-z0-9_]*)\s*=\s*\?""").matchEntire(it.trim())) {
                "Parametro SQL no permitido."
            }.groupValues[1]
        }
        require(nombres.distinct().size == nombres.size)
        valores = arrayOfNulls(nombres.size)
    }

    private fun asignar(indice: Int, literal: String) {
        check(!cerrada)
        require(indice in 1..valores.size)
        valores[indice - 1] = literal
    }

    fun setString(indice: Int, valor: String) {
        require('\u0000' !in valor) { "El texto contiene un caracter no permitido." }
        asignar(indice, "N'" + valor.replace("'", "''") + "'")
    }
    fun setInt(indice: Int, valor: Int) = asignar(indice, valor.toString())
    fun setBigDecimal(indice: Int, valor: BigDecimal) =
        asignar(indice, valor.toPlainString())
    fun setDate(indice: Int, valor: Date) {
        val fecha = valor.toString().replace("-", "")
        require(Regex("[0-9]{8}").matches(fecha))
        asignar(indice, "'" + fecha + "'")
    }
    fun setNull(indice: Int, tipo: Int) {
        require(tipo == Types.NVARCHAR || tipo == Types.DATE)
        asignar(indice, "NULL")
    }

    fun leerResultados(timeoutSegundos: Int = 30, lector: (Int, ResultSet) -> Unit): Int {
        check(!cerrada)
        check(valores.all { it != null }) { "Faltan parametros SQL." }
        val sql = "EXEC dbo." + procedimiento + nombres.indices.joinToString(
            separator = ", ", prefix = if (nombres.isEmpty()) "" else " "
        ) { "@" + nombres[it] + "=" + valores[it] } + ";"
        return conexion.createStatement().use { sentencia ->
            sentencia.queryTimeout = timeoutSegundos
            var hayResultado = sentencia.execute(sql)
            var indice = 0
            while (true) {
                if (hayResultado) {
                    sentencia.resultSet.use { lector(indice++, it) }
                } else if (sentencia.updateCount == -1) break
                hayResultado = sentencia.moreResults
            }
            indice
        }
    }

    override fun close() { cerrada = true; valores.fill(null) }
}
