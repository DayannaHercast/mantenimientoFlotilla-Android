package com.example.transandina_app.screens.mecanico

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.model.MecanicoRegistroState
import com.example.transandina_app.data.model.VehiculoMecanico
import com.example.transandina_app.data.repository.MecanicoRepository
import com.example.transandina_app.util.ConductorFormato

private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val MecanicoBackground = Color(0xFFF1F5F9)
private val CardBackground = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroIntervencionMecanicoScreen(
    estado: MecanicoRegistroState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSiguiente: (MecanicoRegistroState) -> Unit = {}
) {
    val repository = remember { MecanicoRepository() }
    var vehiculos by remember { mutableStateOf<List<VehiculoMecanico>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var placaSeleccionada by remember { mutableStateOf(estado.placa) }
    var fechaMillis by remember { mutableStateOf(estado.fechaMillis ?: ConductorFormato.hoyDatePickerMillis()) }
    var tipoServicio by remember { mutableStateOf(estado.tipoServicio) }
    var expandirVehiculo by remember { mutableStateOf(false) }
    var expandirTipo by remember { mutableStateOf(false) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cargando = true
        repository.obtenerVehiculosActivos()
            .onSuccess { vehiculos = it; error = null }
            .onFailure { error = it.localizedMessage ?: "No se pudo cargar la flotilla." }
        cargando = false
    }

    Surface(modifier = modifier.fillMaxSize(), color = MecanicoBackground, contentColor = Color(0xFF1E293B)) {
        Column(modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
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
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("Registro de Mantenimiento", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Paso 1 de 2 · Vehículo y fecha",
                        style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(20.dp))

                error?.let {
                    Text(it, color = Color(0xFF991B1B), modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- VEHICULO REPORTADO ---
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text(
                        "Vehículo reportado",
                        style = MaterialTheme.typography.labelLarge.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(expanded = expandirVehiculo, onExpandedChange = { expandirVehiculo = it }) {
                        OutlinedTextField(
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B),
                                focusedPlaceholderColor = Color(0xFF64748B),
                                unfocusedPlaceholderColor = Color(0xFF64748B),
                                focusedTrailingIconColor = NavyBluePrimary,
                                unfocusedTrailingIconColor = NavyBluePrimary,
                                cursorColor = Color(0xFF0284C7)
                            ),
                            singleLine = true,

                            value = vehiculos.find { it.placa == placaSeleccionada }
                                ?.let { "${it.placa} · ${it.nombreVehiculo}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text(if (cargando) "Cargando..." else "Selecciona un vehículo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirVehiculo) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandirVehiculo, onDismissRequest = { expandirVehiculo = false }) {
                            vehiculos.forEach { vehiculo ->
                                DropdownMenuItem(
                                    text = { Text("${vehiculo.placa} · ${vehiculo.nombreVehiculo}") },
                                    onClick = { placaSeleccionada = vehiculo.placa; expandirVehiculo = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- TARJETA: FECHA Y TIPO DE INTERVENCION ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Column {
                        Text("Fecha", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B),
                                focusedPlaceholderColor = Color(0xFF64748B),
                                unfocusedPlaceholderColor = Color(0xFF64748B),
                                focusedTrailingIconColor = NavyBluePrimary,
                                unfocusedTrailingIconColor = NavyBluePrimary,
                                cursorColor = Color(0xFF0284C7)
                            ),
                            singleLine = true,

                            value = ConductorFormato.fechaVisible(fechaMillis),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                androidx.compose.material3.IconButton(onClick = { mostrarSelectorFecha = true }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Elegir fecha",
                                        tint = Color(0xFF0284C7))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text(
                            "Tipo de intervención",
                            style = MaterialTheme.typography.labelLarge.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(expanded = expandirTipo, onExpandedChange = { expandirTipo = it }) {
                            OutlinedTextField(
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B),
                                focusedPlaceholderColor = Color(0xFF64748B),
                                unfocusedPlaceholderColor = Color(0xFF64748B),
                                focusedTrailingIconColor = NavyBluePrimary,
                                unfocusedTrailingIconColor = NavyBluePrimary,
                                cursorColor = Color(0xFF0284C7)
                            ),
                            singleLine = true,

                                value = tipoServicio?.replaceFirstChar { it.uppercase() } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Selecciona el tipo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirTipo) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandirTipo, onDismissRequest = { expandirTipo = false }) {
                                listOf("preventivo", "correctivo").forEach { tipo ->
                                    DropdownMenuItem(
                                        text = { Text(tipo.replaceFirstChar { it.uppercase() }) },
                                        onClick = { tipoServicio = tipo; expandirTipo = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

            }

            // --- BOTONES: REGRESAR / SIGUIENTE ---
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = NavyBluePrimary),
                        border = BorderStroke(1.5.dp, NavyBluePrimary),
                        onClick = onNavigateBack) { Text("Regresar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                Button(
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                        shape = RoundedCornerShape(16.dp),

                    onClick = {
                        onSiguiente(
                            estado.copy(
                                placa = placaSeleccionada,
                                nombreVehiculo = vehiculos.find { it.placa == placaSeleccionada }?.nombreVehiculo,
                                fechaMillis = fechaMillis,
                                tipoServicio = tipoServicio,
                                kilometrajeActual = if (estado.placa == placaSeleccionada && estado.kilometrajeActual != null)
                                    estado.kilometrajeActual else vehiculos.find { it.placa == placaSeleccionada }?.kilometrajeActual
                            )
                        )
                    },
                    enabled = !cargando && error == null && vehiculos.any { it.placa == placaSeleccionada } && !tipoServicio.isNullOrBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary, contentColor = Color.White)
                ) { Text("Siguiente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            }

        }
    }

    if (mostrarSelectorFecha) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = fechaMillis,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    ConductorFormato.fechaValida(utcTimeMillis)
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { seleccion ->
                        if (ConductorFormato.fechaValida(seleccion)) fechaMillis = seleccion
                    }
                    mostrarSelectorFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarSelectorFecha = false }) { Text("Cancelar") } }
        ) { DatePicker(state = estadoFecha) }
    }
}
