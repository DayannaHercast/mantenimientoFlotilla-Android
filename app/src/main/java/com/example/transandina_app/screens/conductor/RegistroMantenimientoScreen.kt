package com.example.transandina_app.screens.admin

import androidx.compose.ui.platform.LocalContext
import com.example.transandina_app.data.model.CategoriaServicioConductor
import com.example.transandina_app.data.model.TipoServicioConductor
import com.example.transandina_app.util.ConductorFormato
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.transandina_app.data.model.MantenimientoRegistro
import com.example.transandina_app.data.model.VehiculoConductor
import com.example.transandina_app.data.repository.MantenimientoRegistroRepository
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
fun RegistroMantenimientoScreen(
    modifier: Modifier = Modifier,
    vehiculoRepository: ConductorCatalogoRepository = remember { ConductorCatalogoRepository() },
    mantenimientoRepository: MantenimientoRegistroRepository = remember { MantenimientoRegistroRepository() },
    onNavigateBack: () -> Unit = {},
    onRegistroExitoso: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormatterDisplay = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "CR")).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    val dateFormatterApi = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    }

    // --- Carga de vehículos desde la base de datos, para el dropdown "Placa del vehículo" ---
    var vehiculosList by remember { mutableStateOf<List<VehiculoConductor>>(emptyList()) }
    var isLoadingVehiculos by remember { mutableStateOf(true) }
    var loadErrorMessage by remember { mutableStateOf<String?>(null) }

    var categorias by remember { mutableStateOf<List<CategoriaServicioConductor>>(emptyList()) }

    fun cargarVehiculos() {
        isLoadingVehiculos = true
        loadErrorMessage = null
        coroutineScope.launch {
            val vehiculos = vehiculoRepository.obtenerFlotilla()
            val catalogo = vehiculoRepository.obtenerCategorias()
            vehiculosList = vehiculos.getOrNull().orEmpty()
            categorias = catalogo.getOrNull().orEmpty()
            loadErrorMessage = vehiculos.exceptionOrNull()?.localizedMessage
                ?: catalogo.exceptionOrNull()?.localizedMessage
                ?: when {
                    vehiculosList.isEmpty() -> "No tienes vehículos asignados."
                    categorias.isEmpty() -> "No hay categorías de servicio configuradas."
                    else -> null
                }
            isLoadingVehiculos = false
        }
    }

    LaunchedEffect(Unit) { cargarVehiculos() }
    val placasDisponibles = remember(vehiculosList) { vehiculosList.map { it.placa } }

    // --- Estado del formulario ---
    var selectedPlaca by remember { mutableStateOf("") }
    var isPlacaMenuOpen by remember { mutableStateOf(false) }
    LaunchedEffect(placasDisponibles) {
        if (selectedPlaca !in placasDisponibles) selectedPlaca = placasDisponibles.firstOrNull().orEmpty()
    }

    var kilometrajeActualTexto by remember { mutableStateOf("") }

    val tiposServicio = remember { TipoServicioConductor.values().map { it.label } }
    var selectedTipoServicio by remember { mutableStateOf(tiposServicio.first()) }
    var isTipoMenuOpen by remember { mutableStateOf(false) }

    val categoriasDisponibles = categorias.map { it.nombreCategoria }
    var selectedCategoria by remember { mutableStateOf("") }
    LaunchedEffect(categoriasDisponibles) {
        if (selectedCategoria !in categoriasDisponibles) selectedCategoria = categoriasDisponibles.firstOrNull().orEmpty()
    }
    var isCategoriaMenuOpen by remember { mutableStateOf(false) }

    var fechaMillis by remember { mutableStateOf(ConductorFormato.hoyDatePickerMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var taller by remember { mutableStateOf("") }
    var costoTexto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var comprobantes by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val pickComprobantesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> comprobantes = uris }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val esFormularioValido = selectedPlaca in placasDisponibles &&
        selectedCategoria in categoriasDisponibles &&
        (kilometrajeActualTexto.toIntOrNull()?.let { it >= 0 } == true) &&
        ConductorFormato.fechaValida(fechaMillis) &&
        taller.isNotBlank() && taller.length <= 150 &&
        ConductorFormato.costo(costoTexto) != null &&
        descripcion.isNotBlank() && comprobantes.size <= 5

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

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Registro de mantenimiento de Flotillas",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = NavyBluePrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                when {
                    isLoadingVehiculos -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = NavyBluePrimary, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Cargando vehículos...", style = MaterialTheme.typography.bodyMedium.copy(color = NavyBluePrimary))
                                }
                            }
                        }
                    }
                    loadErrorMessage != null -> {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = loadErrorMessage ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF991B1B)),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(onClick = { cargarVehiculos() }, colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary), shape = RoundedCornerShape(12.dp)) {
                                    Text(text = "Reintentar conexión", color = Color.White)
                                }
                            }
                        }
                    }
                    else -> {
                        item {
                            LabeledDropdown(
                                label = "Placa del vehículo",
                                selectedValue = selectedPlaca.ifBlank { "Seleccione un vehículo" },
                                options = placasDisponibles,
                                isOpen = isPlacaMenuOpen,
                                onOpenChange = { isPlacaMenuOpen = it },
                                onSelect = { selectedPlaca = it }
                            )
                        }

                        item {
                            LabeledTextField(
                                label = "Kilometraje actual",
                                value = kilometrajeActualTexto,
                                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) kilometrajeActualTexto = it },
                                placeholder = "Ej. 52000",
                                keyboardType = KeyboardType.Number
                            )
                        }

                        item {
                            LabeledDropdown(
                                label = "Tipo de Servicio",
                                selectedValue = selectedTipoServicio,
                                options = tiposServicio,
                                isOpen = isTipoMenuOpen,
                                onOpenChange = { isTipoMenuOpen = it },
                                onSelect = { selectedTipoServicio = it }
                            )
                        }

                        item {
                            LabeledDropdown(
                                label = "Categoría",
                                selectedValue = selectedCategoria,
                                options = categoriasDisponibles,
                                isOpen = isCategoriaMenuOpen,
                                onOpenChange = { isCategoriaMenuOpen = it },
                                onSelect = { selectedCategoria = it }
                            )
                        }

                        item {
                            Column {
                                Text(
                                    text = "Fecha",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                OutlinedTextField(
                                    value = dateFormatterDisplay.format(Date(fechaMillis)),
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Elegir fecha", tint = AccentBlue)
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
                        }

                        item {
                            LabeledTextField(label = "Taller", value = taller, onValueChange = { if (it.length <= 150) taller = it }, placeholder = "Nombre del taller")
                        }

                        item {
                            LabeledTextField(
                                label = "Costo monetario",
                                value = costoTexto,
                                onValueChange = { if (ConductorFormato.entradaCostoValida(it)) costoTexto = it },
                                placeholder = "Ej. 85000,50",
                                keyboardType = KeyboardType.Decimal,
                                leadingText = "₡"
                            )
                        }

                        item {
                            Column {
                                Text(
                                    text = "Descripción",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                OutlinedTextField(
                                    value = descripcion,
                                    onValueChange = { descripcion = it },
                                    placeholder = { Text("Detalle del trabajo realizado") },
                                    minLines = 3,
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
                        }

                        item {
                            Column {
                                Text(
                                    text = "Comprobantes",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                OutlinedButton(
                                    onClick = { pickComprobantesLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Icon(
                                        imageVector = if (comprobantes.isEmpty()) Icons.Default.UploadFile else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (comprobantes.isEmpty()) NavyBluePrimary else Color(0xFF059669)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (comprobantes.isEmpty()) "Adjuntar fotos (máximo 5)"
                                        else "${comprobantes.size} comprobante(s) seleccionado(s)"
                                    )
                                }
                            }
                        }

                        if (comprobantes.size > 5) {
                            item { Text("Selecciona un máximo de 5 comprobantes.", color = Color(0xFFDC2626)) }
                        }

                        submitError?.let { mensaje ->
                            item {
                                Text(text = mensaje, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFDC2626)))
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }

            // --- BOTONES: REGRESAR / REGISTRAR ---
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
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
                            val registro = MantenimientoRegistro(
                                placa = selectedPlaca,
                                kilometrajeActual = kilometrajeActualTexto.toInt(),
                                tipoServicio = TipoServicioConductor.values().first { it.label == selectedTipoServicio }.name,
                                categoria = selectedCategoria,
                                fecha = dateFormatterApi.format(Date(fechaMillis)),
                                taller = taller,
                                costo = requireNotNull(ConductorFormato.costo(costoTexto)),
                                descripcion = descripcion,
                                comprobantesUris = comprobantes.map { it.toString() }
                            )
                            val result = mantenimientoRepository.registrarMantenimiento(registro, context)
                            isSubmitting = false
                            result.onSuccess {
                                onRegistroExitoso()
                            }.onFailure { error ->
                                submitError = error.localizedMessage ?: "No se pudo registrar el mantenimiento."
                            }
                        }
                    },
                    enabled = esFormularioValido && !isSubmitting && !isLoadingVehiculos && loadErrorMessage == null,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(20.dp).width(20.dp))
                    } else {
                        Text(text = "Registrar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
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
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fechaMillis = it }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingText: String? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = leadingText?.let { { Text(it, style = MaterialTheme.typography.bodyLarge, color = NavyBluePrimary) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
}

@Composable
private fun LabeledDropdown(
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
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onOpenChange(!isOpen) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = NavyBluePrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedValue, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1E293B), fontWeight = FontWeight.Medium))
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Desplegar opciones", tint = NavyBluePrimary)
                }
            }
            DropdownMenu(expanded = isOpen, onDismissRequest = { onOpenChange(false) }, modifier = Modifier.fillMaxWidth(0.9f)) {
                if (options.isEmpty()) {
                    DropdownMenuItem(text = { Text("Sin opciones disponibles") }, onClick = {})
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

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegistroMantenimientoScreenPreview() {
    TransAndinaAppTheme {
        RegistroMantenimientoScreen()
    }
}
