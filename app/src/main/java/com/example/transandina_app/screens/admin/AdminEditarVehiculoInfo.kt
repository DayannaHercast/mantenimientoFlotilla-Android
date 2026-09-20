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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.transandina_app.data.repository.UsuarioRepository
import kotlinx.coroutines.launch
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

// Datos de la sección "Información General" del formulario de edición
data class VehiculoInfoGeneral(
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: String,
    val tipoVehiculo: String,
    val capacidad: String,
    val conductorAsignado: Conductor?
)

// Lista de ejemplo de conductores para el selector
private val conductoresParaAsignarDePrueba = listOf(
    Conductor(1, "Juan Pérez López", EstadoConductor.ACTIVO),
    Conductor(2, "Estefannía Portuguez Víquez", EstadoConductor.ACTIVO)
)

@Composable
fun AdminEditarVehiculoInfo(
    modifier: Modifier = Modifier,
    placaInicial: String = "",
    marcaInicial: String = "",
    modeloInicial: String = "",
    anioInicial: String = "",
    tipoVehiculoInicial: String = "",
    capacidadInicial: String = "",
    conductorAsignadoInicial: Conductor? = null,
    conductoresDisponibles: List<Conductor>? = null,
    usuarioRepository: UsuarioRepository = remember { UsuarioRepository() },
    onVolver: () -> Unit = {},
    onContinuar: (VehiculoInfoGeneral) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var placa by remember { mutableStateOf(placaInicial) }
    var marca by remember { mutableStateOf(marcaInicial) }
    var modelo by remember { mutableStateOf(modeloInicial) }
    var anio by remember { mutableStateOf(anioInicial) }
    var tipoVehiculo by remember { mutableStateOf(tipoVehiculoInicial) }
    var capacidad by remember { mutableStateOf(capacidadInicial) }
    var conductorSeleccionado by remember { mutableStateOf(conductorAsignadoInicial) }
    var menuConductorExpandido by remember { mutableStateOf(false) }

    var listaConductores by remember { mutableStateOf(conductoresDisponibles ?: emptyList()) }
    var cargandoConductores by remember { mutableStateOf(conductoresDisponibles == null) }

    LaunchedEffect(placaInicial) {
        if (conductoresDisponibles == null) {
            val res = usuarioRepository.obtenerConductoresDisponibles(placaInicial.ifBlank { null })
            res.onSuccess { list ->
                listaConductores = list
                cargandoConductores = false
                if (conductorSeleccionado != null) {
                    val matching = list.find { it.id == conductorSeleccionado?.id || it.nombreCompleto.equals(conductorSeleccionado?.nombreCompleto, ignoreCase = true) }
                    if (matching != null) {
                        conductorSeleccionado = matching
                    }
                }
            }.onFailure {
                cargandoConductores = false
            }
        }
    }

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
                    text = "Paso 1 de 2: Información General y Asignación",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. SECCIÓN: INFORMACIÓN GENERAL ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "INFORMACIÓN GENERAL",
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
                        CampoEdicion(etiqueta = "Placa", valor = placa, onValorCambia = { placa = it })
                        CampoEdicion(etiqueta = "Marca", valor = marca, onValorCambia = { marca = it })
                        CampoEdicion(etiqueta = "Modelo", valor = modelo, onValorCambia = { modelo = it })
                        CampoEdicion(etiqueta = "Año", valor = anio, onValorCambia = { anio = it })
                        CampoEdicion(etiqueta = "Tipo de Vehículo", valor = tipoVehiculo, onValorCambia = { tipoVehiculo = it })
                        CampoEdicion(etiqueta = "Capacidad", valor = capacidad, onValorCambia = { capacidad = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. SECCIÓN: ASIGNACIÓN INICIAL ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "ASIGNACIÓN DE CONDUCTOR",
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
                            text = "Conductor Asignado",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { menuConductorExpandido = true },
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = NavyBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = conductorSeleccionado?.nombreCompleto ?: "Seleccionar conductor...",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (conductorSeleccionado != null) NavyBluePrimary else Color(0xFF94A3B8),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Desplegar",
                                        tint = NavyBluePrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = menuConductorExpandido,
                                onDismissRequest = { menuConductorExpandido = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Sin conductor asignado",
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = Color(0xFF64748B)
                                        )
                                    },
                                    onClick = {
                                        conductorSeleccionado = null
                                        menuConductorExpandido = false
                                    }
                                )
                                if (cargandoConductores) {
                                    DropdownMenuItem(
                                        text = { Text("Cargando conductores...", color = Color.Gray) },
                                        onClick = {}
                                    )
                                } else {
                                    listaConductores.forEach { conductor ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = NavyBluePrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = conductor.nombreCompleto,
                                                        fontWeight = if (conductorSeleccionado?.id == conductor.id) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (conductorSeleccionado?.id == conductor.id) NavyBluePrimary else Color.Unspecified
                                                    )
                                                }
                                            },
                                            onClick = {
                                                conductorSeleccionado = conductor
                                                menuConductorExpandido = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // --- 5. BOTONES: VOLVER Y CONTINUAR ---
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
                        onContinuar(
                            VehiculoInfoGeneral(
                                placa = placa,
                                marca = marca,
                                modelo = modelo,
                                anio = anio,
                                tipoVehiculo = tipoVehiculo,
                                capacidad = capacidad,
                                conductorAsignado = conductorSeleccionado
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Continuar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Fila con etiqueta estilizada y campo de texto OutlinedTextField moderno
@Composable
private fun CampoEdicion(
    etiqueta: String,
    valor: String,
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
fun AdminEditarVehiculoInfoPreview() {
    TransAndinaAppTheme {
        AdminEditarVehiculoInfo(
            conductoresDisponibles = conductoresParaAsignarDePrueba
        )
    }
}