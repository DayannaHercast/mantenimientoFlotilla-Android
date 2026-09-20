package com.example.transandina_app.data.database

import java.sql.SQLException
import kotlinx.coroutines.CancellationException

/** No suprime cancelaciones y no muestra detalles de conexion ni credenciales. */
internal suspend fun <T> resultadoSqlConductor(accion: suspend () -> T): Result<T> = try {
    Result.success(accion())
} catch (error: CancellationException) {
    throw error
} catch (error: SQLException) {
    val mensaje = when (error.errorCode) {
        51000 -> "Debes seleccionar un vehículo e iniciar sesión."
        51001, 51002 -> "Tu usuario está inactivo o no tienes asignado ese vehículo."
        51004 -> "Hay frecuencias duplicadas. Solicita que revisen el plan de mantenimiento."
        51006, 51007 -> "Hay lecturas de kilometraje inconsistentes. Solicita su revisión."
        51100 -> "La fecha no puede ser futura ni anterior a 1900."
        51101 -> "El kilometraje debe ser un entero no negativo."
        51103, 51104, 51105 -> "El kilometraje no coincide con las lecturas anteriores o posteriores del vehículo."
        51107 -> "Ya existe una lectura de este vehículo para esa fecha."
        51108, 51109, 51110, 51114 -> "Revisa el tipo, categoría, taller, descripción y costo del mantenimiento."
        51115 -> "La notificación no está asignada a tu usuario."
        51300, 51301, 51302, 51303 -> "Los comprobantes no son válidos o superan el límite permitido."
        52000, 52001 -> "Tu usuario está inactivo o no tiene el rol de mecánico."
        52003, 52004 -> "Revisa el rango de fechas de la consulta."
        52107 -> "El vehículo no existe o está inactivo. Vuelve a seleccionar uno."
        2812 -> "Falta instalar un procedimiento almacenado del módulo de conductor."
        else -> "No se pudo completar la operación con la base de datos. Si estabas guardando, verifica el historial antes de repetir el envío."
    }
    Result.failure(IllegalStateException(mensaje, error))
} catch (error: LinkageError) {
    Result.failure(IllegalStateException("El driver SQL no pudo inicializarse. Cierra la app y verifica que instalaste la versión corregida.", error))
} catch (error: Exception) {
    Result.failure(error)
}
