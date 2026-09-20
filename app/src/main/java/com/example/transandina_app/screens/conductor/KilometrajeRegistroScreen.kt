package com.example.transandina_app.screens.admin

import com.example.transandina_app.util.ConductorFormato
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.model.KilometrajeRegistro
import com.example.transandina_app.data.model.VehiculoConductor
import com.example.transandina_app.data.repository.KilometrajeRepository
import com.example.transandina_app.data.repository.ConductorCatalogoRepository
import com.example.transandina_app.ui.theme.TransAndinaAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val AccentBlue = Color(0xFF0284C7)
private val BackgroundCanvas = Color(0xFFF1F5F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KilometrajeRegistroScreen(
    modifier: Modifier = Modifier,
    vehiculoRepository: ConductorCatalogoRepository = remember { ConductorCatalogoRepository() },
    kilometrajeRepository: KilometrajeRepository = remember { KilometrajeRepository() },
    onNavigateBack: () -> Unit = {},
    onRegistroExitoso: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val dateFormatterDisplay = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "CR")).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    val dateFormatterApi = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    }

    // --- Carga de los vehículos asignados desde la base de datos ---
    var vehiculosList by remember { mutableStateOf<List<VehiculoConductor>>(emptyList()) }
    var isLoadingVehiculos by remember { mutableStateOf(true) }
    var loadErrorMessage by remember { mutableStateOf<String?>(null) }

    fun cargarVehiculos() {
        isLoadingVehiculos = true
        loadErrorMessage = null
        coroutineScope.launch {
            val result = vehiculoRepository.obtenerFlotilla()
            result.onSuccess { lista ->
                vehiculosList = lista
                loadErrorMessage = if (lista.isEmpty()) "No tienes vehículos asignados." else null
                isLoadingVehiculos = false
            }.onFailure { error ->
                loadErrorMessage = error.localizedMessage ?: "Error al conectar con la base de datos."
                isLoadingVehiculos = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarVehiculos() }

    val placasDisponibles = remember(vehiculosList) { vehiculosList.map { it.placa } }

    // --- Estado del formulario ---
    var selectedPlaca by remember { mutableStateOf("") }
    var isPlacaMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(placasDisponibles) {
        if (selectedPlaca !in placasDisponibles) {
            selectedPlaca = placasDisponibles.firstOrNull().orEmpty()
        }
    }

    var fechaMillis by remember { mutableStateOf(ConductorFormato.hoyDatePickerMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var kilometrajeActualTexto by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val esFormularioValido = selectedPlaca in placasDisponibles &&
        (kilometrajeActualTexto.toIntOrNull()?.let { it >= 0 } == true) &&
        ConductorFormato.fechaValida(fechaMillis)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundCanvas) {
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
                    text = "Registro de Kilometraje",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                when {
                    isLoadingVehiculos -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = NavyBluePrimary, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Cargando vehículos asignados...",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = NavyBluePrimary)
                                )
                            }
                        }
                    }
                    loadErrorMessage != null -> {
                        ErrorRetryBlock(
                            message = loadErrorMessage ?: "",
                            onRetry = { cargarVehiculos() }
                        )
                    }
                    else -> {
                        // --- Vehículo asignado ---
                        LabeledDropdownField(
                            label = "Vehículo asignado",
                            selectedValue = selectedPlaca.ifBlank { "Seleccione un vehículo" },
                            options = placasDisponibles,
                            isOpen = isPlacaMenuOpen,
                            onOpenChange = { isPlacaMenuOpen = it },
                            onSelect = { selectedPlaca = it }
                        )

                        androidx.compose.material3.HorizontalDivider(
                            color = Color(0xFFCBD5E1),
                            thickness = 1.dp
                        )

                        // --- Fecha ---
                        Column {
                            Text(
                                text = "Fecha",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = dateFormatterDisplay.format(Date(fechaMillis)),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Elegir fecha",
                                            tint = AccentBlue
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }

                        // --- Kilometraje actual ---
                        Column {
                            Text(
                                text = "Kilometraje actual",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = kilometrajeActualTexto,
                                onValueChange = { nuevo ->
                                    if (nuevo.length <= 7 && nuevo.all { it.isDigit() }) {
                                        kilometrajeActualTexto = nuevo
                                    }
                                },
                                placeholder = { Text("Ej. 45000") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }

                        submitError?.let { mensaje ->
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFDC2626))
                            )
                        }
                    }
                }
            }

            // --- BOTONES: REGRESAR / REGISTRAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary)
                ) {
                    Text(text = "Regresar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = {
                        if (!esFormularioValido || isSubmitting) return@Button
                        submitError = null
                        isSubmitting = true
                        coroutineScope.launch {
                            val registro = KilometrajeRegistro(
                                placa = selectedPlaca,
                                fecha = dateFormatterApi.format(Date(fechaMillis)),
                                kilometrajeActual = kilometrajeActualTexto.toInt()
                            )
                            val result = kilometrajeRepository.registrarKilometraje(registro)
                            isSubmitting = false
                            result.onSuccess {
                                onRegistroExitoso()
                            }.onFailure { error ->
                                submitError = error.localizedMessage ?: "No se pudo registrar el kilometraje."
                            }
                        }
                    },
                    enabled = esFormularioValido && !isSubmitting && !isLoadingVehiculos && loadErrorMessage == null,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(20.dp).width(20.dp)
                        )
                    } else {
                        Text(
                            text = "Registrar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaMillis,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = ConductorFormato.fechaValida(utcTimeMillis)
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fechaMillis = it }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}

// Selector desplegable reutilizable con etiqueta superior (estilo TransAndina)
@Composable
private fun LabeledDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = NavyBluePrimary
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onOpenChange(!isOpen) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = NavyBluePrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedValue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar opciones",
                        tint = NavyBluePrimary
                    )
                }
            }

            DropdownMenu(
                expanded = isOpen,
                onDismissRequest = { onOpenChange(false) },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (options.isEmpty()) {
                    DropdownMenuItem(text = { Text("No hay vehículos disponibles") }, onClick = {})
                }
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontWeight = if (selectedValue == option) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedValue == option) NavyBluePrimary else Color.Unspecified
                            )
                        },
                        onClick = {
                            onSelect(option)
                            onOpenChange(false)
                        }
                    )
                }
            }
        }
    }
}

// Bloque reutilizable de error con botón de reintento
@Composable
private fun ErrorRetryBlock(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF991B1B)),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Reintentar conexión", color = Color.White)
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KilometrajeRegistroScreenPreview() {
    TransAndinaAppTheme {
        KilometrajeRegistroScreen()
    }
}
