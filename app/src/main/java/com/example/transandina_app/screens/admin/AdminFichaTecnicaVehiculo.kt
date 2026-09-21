package com.example.transandina_app.screens.admin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.repository.VehiculoRepository
import com.example.transandina_app.ui.theme.TransAndinaAppTheme
import kotlinx.coroutines.launch

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Datos completos de la ficha técnica de un vehículo
data class FichaTecnicaVehiculo(
    val placa: String = "",
    val marca: String = "",
    val modelo: String = "",
    val estado: String = "Activo",
    val conductorActual: String = "Sin conductor asignado",
    val anio: String = "",
    val capacidad: String = "",
    val tipoVehiculo: String = "",
    val kilometrajeActual: String = "0",
    val vencimientoRtv: String = "",
    val vencimientoMarchamo: String = "",
    val vencimientoSeguro: String = "",
    val vencimientoDocumentos: String = "",
    val kilometraje: String = "0 KM"
)

@Composable
fun AdminFichaTecnicaVehiculo(
    modifier: Modifier = Modifier,
    placa: String = "",
    ficha: FichaTecnicaVehiculo = FichaTecnicaVehiculo(),
    vehiculoRepository: VehiculoRepository = remember { VehiculoRepository() },
    onReasignarConductor: (FichaTecnicaVehiculo) -> Unit = {},
    onEditarInformacionVehicular: (FichaTecnicaVehiculo) -> Unit = {},
    onVolver: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var fichaActual by remember { mutableStateOf(ficha) }
    var isLoading by remember { mutableStateOf(placa.isNotBlank()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun cargarFicha() {
        if (placa.isBlank()) return
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val res = vehiculoRepository.obtenerFichaTecnica(placa)
            res.onSuccess { f ->
                fichaActual = f
                isLoading = false
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Error al obtener ficha de la base de datos."
                isLoading = false
            }
        }
    }

    LaunchedEffect(placa) {
        if (placa.isNotBlank()) {
            cargarFicha()
        }
    }

    val isActivo = fichaActual.estado.equals("Activo", ignoreCase = true)
    val bgBadge = if (isActivo) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
    val textBadge = if (isActivo) Color(0xFF047857) else Color(0xFFB91C1C)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // --- 1. ENCABEZADO CON MARCA (Estilo corporativo TransAndina) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyBlueDark, NavyBluePrimary)
                        )
                    )
                    .padding(top = 46.dp, bottom = 26.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Gestión de Mantenimiento de Flotillas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TransAndina",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = LightBlueHeader,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 2. TÍTULO Y SUBTÍTULO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ficha Vehicular",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Especificaciones técnicas y estado de la unidad ${fichaActual.placa}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. CONTENIDO: ESTADO DE CARGA O TARJETA TÉCNICA ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NavyBluePrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Consultando ficha técnica en base de datos...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                        )
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Error",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFB91C1C)),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { cargarFicha() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Encabezado de la tarjeta con icono de vehículo y badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(LightBlueHeader),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = NavyBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${fichaActual.marca} ${fichaActual.modelo}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBluePrimary
                                        )
                                    )
                                    Text(
                                        text = "Placa: ${fichaActual.placa}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            // Badge de estado
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bgBadge)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = fichaActual.estado,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = textBadge,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Datos técnicos reales con espaciado limpio
                        DatoFichaRow(etiqueta = "Conductor actual", valor = fichaActual.conductorActual)
                        DatoFichaRow(etiqueta = "Año del vehículo", valor = fichaActual.anio)
                        DatoFichaRow(etiqueta = "Capacidad", valor = fichaActual.capacidad)
                        DatoFichaRow(etiqueta = "Vencimiento documentos", valor = fichaActual.vencimientoDocumentos)
                        DatoFichaRow(etiqueta = "Kilometraje registrado", valor = fichaActual.kilometraje, esUltimo = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. BOTONES DE ACCIÓN PRINCIPALES ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón: Reasignar Conductor
                Button(
                    onClick = { onReasignarConductor(fichaActual) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reasignar Conductor",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Botón: Editar Información Vehicular
                OutlinedButton(
                    onClick = { onEditarInformacionVehicular(fichaActual) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = NavyBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Editar Información Vehicular",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = NavyBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Botón: Volver
                OutlinedButton(
                    onClick = onVolver,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Volver",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Fila para mostrar cada dato técnico en la ficha
@Composable
private fun DatoFichaRow(
    etiqueta: String,
    valor: String,
    esUltimo: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.End
            )
        }
        if (!esUltimo) {
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminFichaTecnicaVehiculoPreview() {
    TransAndinaAppTheme {
        AdminFichaTecnicaVehiculo()
    }
}