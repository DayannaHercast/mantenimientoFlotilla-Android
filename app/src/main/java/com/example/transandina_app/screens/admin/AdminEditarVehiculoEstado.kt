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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.ui.theme.TransAndinaAppTheme

// Colores corporativos TransAndina
// TODO: mover a com.example.transandina_app.ui.theme.Color.kt y compartir entre todas las pantallas
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val SectionBackground = Color(0xFFD9D9D9)
private val BackgroundCanvas = Color(0xFFCDEFFB)

// Datos de la sección "Estado Inicial" y "Documentos" del formulario de edición.
// Se combina con VehiculoInfoGeneral (de la pantalla anterior) al tocar "Guardar".
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
            // --- 1. ENCABEZADO CON MARCA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyBlueDark, NavyBluePrimary)
                        )
                    )
                    .padding(top = 44.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
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
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LightBlueHeader,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 2. TÍTULO ---
            Text(
                text = "Edición de Vehículo",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. SECCIÓN: ESTADO INICIAL (KILOMETRAJE) ---
            Text(
                text = "Estado Inicial",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(SectionBackground, RoundedCornerShape(10.dp))
                    .padding(16.dp)
            ) {
                CampoEdicionEstado(
                    etiqueta = "Kilometraje\nactual",
                    valor = kilometraje,
                    onValorCambia = { kilometraje = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. SECCIÓN: DOCUMENTOS ---
            Text(
                text = "Documentos",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(SectionBackground, RoundedCornerShape(10.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Fechas de vencimiento",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                CampoEdicionEstado(
                    etiqueta = "Revisión Técnica",
                    valor = revisionTecnica,
                    onValorCambia = { revisionTecnica = it }
                )
                CampoEdicionEstado(
                    etiqueta = "Marchamo",
                    valor = marchamo,
                    onValorCambia = { marchamo = it }
                )
                CampoEdicionEstado(
                    etiqueta = "Seguro",
                    valor = seguro,
                    onValorCambia = { seguro = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. SECCIÓN: ESTADO DEL VEHÍCULO ---
            Text(
                text = "Estado del Vehículo",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SectionBackground, RoundedCornerShape(10.dp))
                            .clickable { menuEstadoExpandido = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = estadoSeleccionado?.etiqueta ?: "Seleccionar",
                            style = MaterialTheme.typography.bodyMedium.copy(color = NavyBluePrimary)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Seleccionar estado del vehículo",
                            tint = NavyBluePrimary
                        )
                    }

                    DropdownMenu(
                        expanded = menuEstadoExpandido,
                        onDismissRequest = { menuEstadoExpandido = false }
                    ) {
                        EstadoVehiculo.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.etiqueta) },
                                onClick = {
                                    menuEstadoExpandido = false
                                    estadoSeleccionado = opcion
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 6. BOTONES: VOLVER Y GUARDAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onVolver,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary)
                ) {
                    Text(
                        text = "Volver",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary)
                ) {
                    Text(
                        text = "Guardar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// Fila de etiqueta + campo de texto editable, usada en las secciones del formulario.
@Composable
private fun CampoEdicionEstado(
    etiqueta: String,
    valor: String,
    onValorCambia: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = NavyBluePrimary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorCambia,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
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