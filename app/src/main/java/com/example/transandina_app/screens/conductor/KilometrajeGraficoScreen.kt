package com.example.transandina_app.screens.admin

import com.example.transandina_app.util.ConductorFormato
import kotlinx.coroutines.Job
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.model.KilometrajePuntoHistorial
import com.example.transandina_app.data.model.KilometrajeResumen
import com.example.transandina_app.data.repository.KilometrajeRepository
import com.example.transandina_app.ui.theme.TransAndinaAppTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val AccentBlue = Color(0xFF0284C7)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Colores de las series de la gráfica (coinciden con la leyenda del prototipo)
private val ColorKilometrajeActual = Color(0xFF6D28D9) // Morado
private val ColorKilometrajeRestante = Color(0xFF34D399) // Verde menta

@Composable
fun KilometrajeGraficoScreen(
    modifier: Modifier = Modifier,
    placa: String,
    kilometrajeRepository: KilometrajeRepository = remember { KilometrajeRepository() },
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var resumen by remember { mutableStateOf<KilometrajeResumen?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var consultaJob by remember { mutableStateOf<Job?>(null) }

    // Ejecuta sp_ObtenerHistorialKilometraje(@Placa) a través de KilometrajeRepository
    fun cargarResumen() {
        consultaJob?.cancel()
        resumen = null
        isLoading = true
        errorMessage = null
        consultaJob = coroutineScope.launch {
            val result = kilometrajeRepository.obtenerResumenKilometraje(placa)
            result.onSuccess { data ->
                resumen = data
                isLoading = false
            }.onFailure { error ->
                errorMessage = error.localizedMessage ?: "Error al conectar con la base de datos."
                isLoading = false
            }
        }
    }

    LaunchedEffect(placa) { cargarResumen() }

    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Surface(modifier = modifier.fillMaxSize(), color = BackgroundCanvas) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- 1. ENCABEZADO CORPORATIVO TRANSANDINA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.verticalGradient(colors = listOf(NavyBlueDark, NavyBluePrimary)))
                    .padding(top = 46.dp, bottom = 26.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Gestión de Mantenimiento de Flotillas",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TransAndina",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = LightBlueHeader,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Gráfico histórico del kilometraje recorrido",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = NavyBluePrimary, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Consultando historial...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = NavyBluePrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                    errorMessage != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF991B1B),
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { cargarResumen() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Reintentar conexión", color = Color.White)
                                }
                            }
                        }
                    }
                    resumen == null || resumen!!.historial.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay datos de kilometraje registrados para este vehículo.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        val data = resumen!!

                        // --- Tarjeta con la gráfica de barras ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                KilometrajeBarChart(
                                    puntos = data.historial,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Leyenda
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    LeyendaItem(color = ColorKilometrajeActual, texto = "Kilómetros recorridos")
                                    Spacer(modifier = Modifier.width(20.dp))
                                    LeyendaItem(color = ColorKilometrajeRestante, texto = "Kilometraje restante")
                                }
                            }
                        }

                        if (data.historial.any { it.kilometrajeRestante == null }) {
                            Text("Los períodos sin barra verde no tienen información suficiente para calcular el próximo mantenimiento.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        // --- Total histórico recorrido ---
                        ResumenTarjeta(
                            titulo = "Total histórico recorrido",
                            valor = "${numberFormatter.format(data.totalHistoricoRecorrido)} km"
                        )

                        // --- Kilometraje restante para el mantenimiento ---
                        ResumenTarjeta(
                            titulo = "Kilometraje restante para el mantenimiento",
                            valor = data.kilometrajeRestanteMantenimiento?.let { "${numberFormatter.format(it)} km" } ?: "Sin información"
                        )
                    }
                }
            }

            // --- BOTÓN: REGRESAR ---
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary)
                ) {
                    Text(
                        text = "Regresar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                    )
                }
            }
        }
    }
}

// Gráfico de barras agrupadas (Kilometraje actual vs. Kilometraje restante) dibujado con Canvas,
// sin dependencias externas, listo para recibir cualquier cantidad de periodos desde la BD.
@Composable
private fun KilometrajeBarChart(
    puntos: List<KilometrajePuntoHistorial>,
    modifier: Modifier = Modifier
) {
    val maxValor = remember(puntos) {
        ConductorFormato.maximoGrafica(puntos.map { it.kilometrajeActual }, puntos.map { it.kilometrajeRestante })
    }
    val ejeColor = Color(0xFFCBD5E1)
    val textoColor = android.graphics.Color.parseColor("#475569")

    Canvas(modifier = modifier) {
        val leftPadding = 46.dp.toPx()
        val bottomPadding = 26.dp.toPx()
        val topPadding = 10.dp.toPx()
        val chartWidth = size.width - leftPadding
        val chartHeight = size.height - bottomPadding - topPadding

        // Líneas guía horizontales + etiquetas del eje Y
        val pasos = 4
        for (i in 0..pasos) {
            val y = topPadding + chartHeight - (chartHeight * i / pasos)
            drawLine(
                color = ejeColor,
                start = Offset(leftPadding, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            val valorEtiqueta = (maxValor.toLong() * i / pasos)
            drawContext.canvas.nativeCanvas.drawText(
                valorEtiqueta.toString(),
                0f,
                y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textoColor
                    textSize = 9.dp.toPx()
                }
            )
        }

        if (puntos.isEmpty()) return@Canvas

        val slotWidth = chartWidth / puntos.size
        val groupPadding = slotWidth * 0.2f
        val barWidth = (slotWidth - groupPadding * 2f) / 2f

        puntos.forEachIndexed { index, punto ->
            val slotStart = leftPadding + slotWidth * index + groupPadding

            val alturaActual = chartHeight * (punto.kilometrajeActual.toFloat() / maxValor)
            drawRoundRect(
                color = ColorKilometrajeActual,
                topLeft = Offset(slotStart, topPadding + chartHeight - alturaActual),
                size = Size(barWidth, alturaActual),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            punto.kilometrajeRestante?.let { restante ->
                val alturaRestante = chartHeight * (restante.toFloat() / maxValor)
                drawRoundRect(
                    color = ColorKilometrajeRestante,
                    topLeft = Offset(slotStart + barWidth, topPadding + chartHeight - alturaRestante),
                    size = Size(barWidth, alturaRestante),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Etiqueta del periodo en el eje X
            drawContext.canvas.nativeCanvas.drawText(
                punto.periodo,
                slotStart + barWidth,
                size.height - 6.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textoColor
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 10.dp.toPx()
                }
            )
        }

        // Eje X base
        drawLine(
            color = ejeColor,
            start = Offset(leftPadding, topPadding + chartHeight),
            end = Offset(size.width, topPadding + chartHeight),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
private fun LeyendaItem(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569)))
    }
}

@Composable
private fun ResumenTarjeta(titulo: String, valor: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = titulo.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KilometrajeGraficoScreenPreview() {
    TransAndinaAppTheme {
        KilometrajeGraficoScreen(placa = "DDD-124")
    }
}
