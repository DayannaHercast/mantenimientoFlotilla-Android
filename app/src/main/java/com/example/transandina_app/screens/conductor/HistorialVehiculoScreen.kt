package com.example.transandina_app.screens.admin

import kotlinx.coroutines.Job
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.transandina_app.data.model.HistorialServicioItem
import com.example.transandina_app.data.model.HistorialVehiculoInfo
import com.example.transandina_app.data.repository.HistorialVehiculoRepository
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

@Composable
fun HistorialVehiculoScreen(
    modifier: Modifier = Modifier,
    placaInicial: String = "",
    historialRepository: HistorialVehiculoRepository = remember { HistorialVehiculoRepository() },
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var placaBuscada by remember { mutableStateOf(placaInicial) }
    var vehiculoInfo by remember { mutableStateOf<HistorialVehiculoInfo?>(null) }
    var historialCompleto by remember { mutableStateOf<List<HistorialServicioItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var yaBusco by remember { mutableStateOf(false) }
    var consultaJob by remember { mutableStateOf<Job?>(null) }

    // Ejecuta sp_ObtenerVehiculoPorPlaca y sp_ObtenerHistorialServicios para la placa indicada
    fun buscarHistorial(placa: String) {
        if (placa.isBlank()) return
        consultaJob?.cancel()
        vehiculoInfo = null
        historialCompleto = emptyList()
        isLoading = true
        errorMessage = null
        yaBusco = true
        consultaJob = coroutineScope.launch {
            val infoResult = historialRepository.buscarVehiculo(placa.trim())
            val historialResult = historialRepository.obtenerHistorialServicios(placa.trim())

            if (infoResult.isFailure) {
                errorMessage = infoResult.exceptionOrNull()?.localizedMessage ?: "Vehículo no encontrado."
                vehiculoInfo = null
                historialCompleto = emptyList()
                isLoading = false
                return@launch
            }

            vehiculoInfo = infoResult.getOrNull()
            historialResult.onSuccess { lista ->
                historialCompleto = lista
                isLoading = false
            }.onFailure { error ->
                errorMessage = error.localizedMessage ?: "Error al conectar con la base de datos."
                isLoading = false
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(placaInicial) {
        if (placaInicial.isNotBlank()) buscarHistorial(placaInicial)
    }

    // Filtros de tabla
    val serviciosOpciones = remember(historialCompleto) {
        listOf("Todos") + historialCompleto.map { it.servicio }.distinct()
    }
    var selectedServicio by remember { mutableStateOf("Todos") }
    var isServicioMenuOpen by remember { mutableStateOf(false) }

    val rangosFechas = listOf("Todo el historial", "Últimos 3 meses", "Último año")
    var selectedRango by remember { mutableStateOf("Todo el historial") }
    var isRangoMenuOpen by remember { mutableStateOf(false) }

    var textoBusqueda by remember { mutableStateOf("") }
    androidx.compose.runtime.LaunchedEffect(vehiculoInfo?.placa) {
        selectedServicio = "Todos"
        selectedRango = "Todo el historial"
        textoBusqueda = ""
    }

    val historialFiltrado = remember(historialCompleto, selectedServicio, selectedRango, textoBusqueda) {
        historialCompleto.filter { item ->
            val coincideServicio = selectedServicio == "Todos" || item.servicio == selectedServicio
            val coincideRango = when (selectedRango) {
                "Últimos 3 meses" -> System.currentTimeMillis() - item.fechaMillis <= 90L * 24 * 60 * 60 * 1000
                "Último año" -> System.currentTimeMillis() - item.fechaMillis <= 365L * 24 * 60 * 60 * 1000
                else -> true
            }
            val coincideTexto = textoBusqueda.isBlank() ||
                item.servicio.contains(textoBusqueda, ignoreCase = true) ||
                item.taller.contains(textoBusqueda, ignoreCase = true)
            coincideServicio && coincideRango && coincideTexto
        }
    }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("es", "CR")).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 } }
    val numberFormatter = remember { NumberFormat.getIntegerInstance(Locale("es", "CR")) }

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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Historial del vehículo",
                    style = MaterialTheme.typography.headlineSmall.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // --- Búsqueda por placa ---
                OutlinedTextField(
                    value = placaBuscada,
                    onValueChange = { placaBuscada = it.uppercase() },
                    placeholder = { Text("Indique la placa del vehículo asignado") },
                    trailingIcon = {
                        IconButton(onClick = { buscarHistorial(placaBuscada) }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = AccentBlue)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = NavyBluePrimary, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Consultando historial...", style = MaterialTheme.typography.bodyMedium.copy(color = NavyBluePrimary))
                            }
                        }
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF991B1B)),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                        )
                    }
                    !yaBusco -> {
                        Text(
                            text = "Ingrese una placa para consultar el historial del vehículo.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)
                        )
                    }
                    else -> {
                        vehiculoInfo?.let { info ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = NavyBluePrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Nombre del vehículo: ${info.nombreVehiculo}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Placa: ${info.placa}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569)))
                                    Text(
                                        text = "Kilometraje: ${numberFormatter.format(info.kilometrajeActual)} km",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569))
                                    )
                                }
                            }
                        }

                        // --- Filtros: Servicio / Fecha ---
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CompactDropdown(
                                label = if (selectedServicio == "Todos") "Servicio" else selectedServicio,
                                options = serviciosOpciones,
                                isOpen = isServicioMenuOpen,
                                onOpenChange = { isServicioMenuOpen = it },
                                onSelect = { selectedServicio = it },
                                modifier = Modifier.weight(1f)
                            )
                            CompactDropdown(
                                label = selectedRango,
                                options = rangosFechas,
                                isOpen = isRangoMenuOpen,
                                onOpenChange = { isRangoMenuOpen = it },
                                onSelect = { selectedRango = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            placeholder = { Text("Buscar por servicio o taller") },
                            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AccentBlue) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        // --- Tabla: Fecha / Servicio / Taller / Costo ---
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            val scrollState = rememberScrollState()
                            Column(modifier = Modifier.horizontalScroll(scrollState).padding(12.dp)) {
                                HistorialTablaHeader()
                                HorizontalDivider(color = Color(0xFFCBD5E1))
                                if (historialFiltrado.isEmpty()) {
                                    Box(modifier = Modifier.widthIn(min = 560.dp).padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "No hay servicios registrados para los filtros seleccionados.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.widthIn(min = 560.dp).height(220.dp)) {
                                        items(historialFiltrado, key = { it.id }) { item ->
                                            HistorialTablaFila(item = item, currencyFormatter = currencyFormatter)
                                            HorizontalDivider(color = Color(0xFFE2E8F0))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- BOTÓN: REGRESAR ---
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaultsOutlined(),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary)
                ) {
                    Text(text = "Regresar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
                }
            }
        }
    }
}

@Composable
private fun ButtonDefaultsOutlined() =
    androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary)

@Composable
private fun HistorialTablaHeader() {
    Row(modifier = Modifier.widthIn(min = 560.dp).padding(vertical = 8.dp)) {
        Text(text = "FECHA", modifier = Modifier.width(110.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
        Text(text = "SERVICIO", modifier = Modifier.width(160.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
        Text(text = "TALLER", modifier = Modifier.width(160.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
        Text(text = "COSTO", modifier = Modifier.width(130.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
    }
}

@Composable
private fun HistorialTablaFila(item: HistorialServicioItem, currencyFormatter: NumberFormat) {
    Row(modifier = Modifier.widthIn(min = 560.dp).padding(vertical = 10.dp)) {
        Text(text = item.fecha, modifier = Modifier.width(110.dp), style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155)))
        Text(text = item.servicio, modifier = Modifier.width(160.dp), style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155)))
        Text(text = item.taller, modifier = Modifier.width(160.dp), style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155)))
        Text(text = "₡${currencyFormatter.format(item.costo)}", modifier = Modifier.width(130.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
    }
}

@Composable
private fun CompactDropdown(
    label: String,
    options: List<String>,
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { onOpenChange(!isOpen) },
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFBAE6FD), contentColor = NavyBluePrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary), maxLines = 1)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Opciones", tint = NavyBluePrimary)
        }
        DropdownMenu(expanded = isOpen, onDismissRequest = { onOpenChange(false) }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); onOpenChange(false) })
            }
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistorialVehiculoScreenPreview() {
    TransAndinaAppTheme {
        HistorialVehiculoScreen()
    }
}
