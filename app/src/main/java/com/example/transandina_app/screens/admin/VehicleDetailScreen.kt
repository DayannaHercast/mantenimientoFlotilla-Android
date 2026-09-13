package com.example.transandina_app.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.ui.theme.TransAndinaAppTheme

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val AccentBlue = Color(0xFF0284C7)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Estados de vigencia de los documentos legales
enum class EstadoDocumento(
    val label: String,
    val icon: ImageVector,
    val color: Color
) {
    AL_DIA("Al día", Icons.Default.CheckCircle, Color(0xFF10B981)),            // Check verde
    PROXIMO("Próximo a vencer", Icons.Default.HourglassEmpty, Color(0xFFF59E0B)), // Standby ámbar
    VENCIDO("Vencido", Icons.Default.Cancel, Color(0xFFEF4444))                // X roja
}

// Datos detallados de la ficha técnica de un vehículo
data class FichaVehicular(
    val placa: String = "DDD-124",
    val marca: String = "Hyundai",
    val modelo: String = "Tucson",
    val anio: Int = 2023,
    val estadoSemaforo: EstadoSemaforo = EstadoSemaforo.AL_DIA,
    val estadoOperativo: String = "Activo",
    val conductorActual: String = "Juan Pérez López",
    val capacidad: String = "5 personas",
    val tipoVehiculo: String = "Liviano",
    val marchamoVence: String = "31/12/2026",
    val marchamoEstado: EstadoDocumento = EstadoDocumento.AL_DIA,
    val rtvVence: String = "15/10/2026",
    val rtvEstado: EstadoDocumento = EstadoDocumento.PROXIMO,
    val seguroEstado: String = "Póliza Activa (INS)",
    val seguroEstadoDoc: EstadoDocumento = EstadoDocumento.AL_DIA,
    val kilometrajeActual: String = "42,350 km"
)

@Composable
fun VehicleDetailScreen(
    ficha: FichaVehicular = FichaVehicular(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // --- 1. ENCABEZADO CORPORATIVO TRANSANDINA (SIN FLECHA SUPERIOR) ---
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


            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. TÍTULO DE LA PANTALLA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ficha Vehicular",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${ficha.marca} ${ficha.modelo} ${ficha.anio} • ${ficha.placa}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- 3. TARJETA PRINCIPAL DE INFORMACIÓN ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Barra superior de la tarjeta "Información"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBluePrimary)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Información General",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Contenido detallado del vehículo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Semáforo y Estado Operativo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estado de mantenimiento:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                            )
                            // Badge de Semáforo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ficha.estadoSemaforo.color)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = ficha.estadoSemaforo.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        DetailRow(label = "Placa", value = ficha.placa, icon = Icons.Default.DirectionsCar)
                        DetailRow(label = "Marca", value = ficha.marca)
                        DetailRow(label = "Modelo", value = ficha.modelo)
                        DetailRow(label = "Año", value = ficha.anio.toString())
                        DetailRow(label = "Tipo de vehículo", value = ficha.tipoVehiculo)
                        DetailRow(label = "Estado operativo", value = ficha.estadoOperativo)
                        DetailRow(label = "Conductor actual", value = ficha.conductorActual, icon = Icons.Default.Person)
                        DetailRow(label = "Capacidad", value = ficha.capacidad)

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = Color(0xFFE2E8F0)
                        )

                        // Sección: Vencimiento de Documentos Legales
                        Text(
                            text = "Vencimiento de documentos:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyBluePrimary
                            )
                        )

                        DocumentRow(
                            titulo = "Marchamo",
                            detalle = "${ficha.marchamoEstado.label} (Vence: ${ficha.marchamoVence})",
                            icon = Icons.Default.CalendarToday,
                            estado = ficha.marchamoEstado
                        )

                        DocumentRow(
                            titulo = "Revisión Técnica (RTV)",
                            detalle = "${ficha.rtvEstado.label} (Vence: ${ficha.rtvVence})",
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            estado = ficha.rtvEstado
                        )

                        DocumentRow(
                            titulo = "Seguro Obligatorio",
                            detalle = "${ficha.seguroEstado} (${ficha.seguroEstadoDoc.label})",
                            icon = Icons.Default.VerifiedUser,
                            estado = ficha.seguroEstadoDoc
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = Color(0xFFE2E8F0)
                        )

                        // Odómetro / Kilometraje
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kilometraje actual:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                )
                            }
                            Text(
                                text = ficha.kilometrajeActual,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. BOTÓN: HISTORIAL DE MANTENIMIENTO Y REPORTE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(
                    onClick = onNavigateToHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Historial de mantenimiento y reporte",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 5. BOTÓN: VOLVER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary)
                ) {
                    Text(
                        text = "Volver",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// Fila de detalle estándar
@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = NavyBluePrimary
            )
        )
    }
}

// Fila para documentos legales (Marchamo, RTV, Seguro)
@Composable
private fun DocumentRow(
    titulo: String,
    detalle: String,
    icon: ImageVector,
    estado: EstadoDocumento
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyBluePrimary
                    )
                )
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF475569)
                    )
                )
            }
        }

        // Icono dinámico según estado del documento:
        // - Al día / vigente: CheckCircle verde
        // - Próximo a vencer: HourglassEmpty (standby) ámbar
        // - Ya se venció: Cancel (X) roja
        Icon(
            imageVector = estado.icon,
            contentDescription = estado.label,
            tint = estado.color,
            modifier = Modifier.size(22.dp)
        )
    }
}


// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VehicleDetailScreenPreview() {
    TransAndinaAppTheme {
        VehicleDetailScreen()
    }
}
