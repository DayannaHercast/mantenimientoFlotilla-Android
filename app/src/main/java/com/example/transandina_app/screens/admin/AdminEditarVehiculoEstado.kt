package com.example.transandina_app.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.transandina_app.ui.theme.TransAndinaAppTheme

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Datos de la sección "Estado Inicial" y "Documentos" del formulario de edición
data class VehiculoEstadoInicial(
    val kilometrajeActual: String,
    val vencimientoRevisionTecnica: String,
    val vencimientoMarchamo: String,
    val vencimientoSeguro: String,
    val estadoVehiculo: EstadoVehiculo?
)

@Composable
fun AdminEditarVehiculoEstado(
    modifier: Modifier = Modifier,
    kilometrajeInicial: String = "",
    revisionTecnicaInicial: String = "",
    marchamoInicial: String = "",
    seguroInicial: String = "",
    estadoInicial: EstadoVehiculo? = null,
    onVolver: () -> Unit = {},
    onGuardar: (VehiculoEstadoInicial) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    var kilometraje by remember { mutableStateOf(kilometrajeInicial) }
    var revisionTecnica by remember { mutableStateOf(revisionTecnicaInicial) }
    var marchamo by remember { mutableStateOf(marchamoInicial) }
    var seguro by remember { mutableStateOf(seguroInicial) }
    var estadoSeleccionado by remember { mutableStateOf(estadoInicial) }
    var menuEstadoExpandido by remember { mutableStateOf(false) }

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
                    text = "Edición de Vehículo",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Paso 2 de 2: Kilometraje, Documentación y Estado",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. SECCIÓN: ESTADO INICIAL (KILOMETRAJE) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "KILOMETRAJE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        CampoEdicionEstado(
                            etiqueta = "Kilometraje actual",
                            valor = kilometraje,
                            placeholder = "Ej: 45000 km",
                            onValorCambia = { kilometraje = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 4. SECCIÓN: DOCUMENTOS (FECHAS DE VENCIMIENTO) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "VENCIMIENTO DE DOCUMENTOS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CampoEdicionEstado(
                            etiqueta = "Revisión Técnica",
                            valor = revisionTecnica,
                            placeholder = "DD/MM/AAAA",
                            onValorCambia = { revisionTecnica = it }
                        )
                        CampoEdicionEstado(
                            etiqueta = "Marchamo",
                            valor = marchamo,
                            placeholder = "DD/MM/AAAA",
                            onValorCambia = { marchamo = it }
                        )
                        CampoEdicionEstado(
                            etiqueta = "Seguro",
                            valor = seguro,
                            placeholder = "DD/MM/AAAA",
                            onValorCambia = { seguro = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 5. SECCIÓN: ESTADO DEL VEHÍCULO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "ESTADO OPERATIVO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Estado del Vehículo",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { menuEstadoExpandido = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFF8FAFC),
                                    contentColor = NavyBluePrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = estadoSeleccionado?.etiqueta ?: "Seleccionar estado...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (estadoSeleccionado != null) NavyBluePrimary else Color(0xFF94A3B8),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Desplegar",
                                        tint = NavyBluePrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = menuEstadoExpandido,
                                onDismissRequest = { menuEstadoExpandido = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                EstadoVehiculo.entries.forEach { opcion ->
                                    val isActivo = opcion == EstadoVehiculo.ACTIVO
                                    val dotColor = if (isActivo) Color(0xFF047857) else Color(0xFFB91C1C)
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(dotColor)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = opcion.etiqueta,
                                                    fontWeight = if (estadoSeleccionado == opcion) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (estadoSeleccionado == opcion) NavyBluePrimary else Color.Unspecified
                                                )
                                            }
                                        },
                                        onClick = {
                                            estadoSeleccionado = opcion
                                            menuEstadoExpandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // --- 6. BOTONES: VOLVER Y GUARDAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onVolver,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Volver",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }

                Button(
                    onClick = {
                        onGuardar(
                            VehiculoEstadoInicial(
                                kilometrajeActual = kilometraje,
                                vencimientoRevisionTecnica = revisionTecnica,
                                vencimientoMarchamo = marchamo,
                                vencimientoSeguro = seguro,
                                estadoVehiculo = estadoSeleccionado
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Guardar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Fila con etiqueta estilizada y campo OutlinedTextField
@Composable
private fun CampoEdicionEstado(
    etiqueta: String,
    valor: String,
    placeholder: String = "",
    onValorCambia: (String) -> Unit
) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF475569),
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambia,
            placeholder = { Text(placeholder, color = Color(0xFF94A3B8)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = NavyBluePrimary,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminEditarVehiculoEstadoPreview() {
    TransAndinaAppTheme {
        AdminEditarVehiculoEstado()
    }
}