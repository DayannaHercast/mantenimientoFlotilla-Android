package com.example.transandina_app.util

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ConductorFormato {
    /** DatePicker representa una fecha de calendario como medianoche UTC. */
    fun hoyDatePickerMillis(ahora: Long = System.currentTimeMillis(), zona: TimeZone = TimeZone.getDefault()): Long {
        val local = Calendar.getInstance(zona).apply { timeInMillis = ahora }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
    fun fechaApi(millis: Long): String = formatoFecha("yyyy-MM-dd", millis)
    fun fechaVisible(millis: Long): String = formatoFecha("dd/MM/yyyy", millis)
    private fun formatoFecha(patron: String, millis: Long): String =
        SimpleDateFormat(patron, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(millis))
    fun fechaValida(millis: Long): Boolean = fechaApi(millis) >= "1900-01-01" && millis <= hoyDatePickerMillis()
    fun entradaCostoValida(texto: String): Boolean = Regex("\\d{0,10}([.,]\\d{0,2})?").matches(texto)
    fun costo(texto: String): BigDecimal? {
        val normalizado = texto.trim().replace(',', '.')
        if (!Regex("\\d{1,10}(\\.\\d{1,2})?").matches(normalizado)) return null
        return normalizado.toBigDecimalOrNull()?.takeIf { it.signum() >= 0 }?.setScale(2)
    }
    fun maximoGrafica(actuales: List<Int>, restantes: List<Int?>): Int =
        maxOf(actuales.maxOrNull() ?: 0, restantes.filterNotNull().maxOrNull() ?: 0).coerceAtLeast(1)
}
