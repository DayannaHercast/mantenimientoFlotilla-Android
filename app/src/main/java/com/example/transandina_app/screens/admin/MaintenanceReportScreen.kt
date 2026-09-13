package com.example.transandina_app.screens.admin

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.ui.theme.TransAndinaAppTheme
import java.text.NumberFormat
import java.util.Locale

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val AccentBlue = Color(0xFF0284C7)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Tipo de Mantenimiento según Requerimiento 3
enum class TipoMantenimiento(
    val label: String,
    val bgBadge: Color,
    val textBadge: Color
) {
    PREVENTIVO("PREVENTIVO", Color(0xFFD1FAE5), Color(0xFF047857)),
    CORRECTIVO("CORRECTIVO", Color(0xFFFFEDD5), Color(0xFFC2410C))
}

// Modelo de datos para cada registro de mantenimiento
data class MantenimientoItem(
    val id: String,
    val placa: String,
    val tipo: TipoMantenimiento,
    val taller: String,
    val descripcion: String,
    val fecha: String,
    val fechaMillis: Long,
    val costo: Int,
    val evidencias: List<String> = listOf("Factura_Taller.jpg", "Reparacion_Foto1.jpg")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceReportScreen(
    placaInicial: String = "DDD-124",
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    // Base de datos de prueba de mantenimientos completa para todos los vehículos
    val mantenimientosIniciales = remember {
        listOf(
            // Mantenimientos DDD-123
            MantenimientoItem(
                id = "1",
                placa = "DDD-123",
                tipo = TipoMantenimiento.PREVENTIVO,
                taller = "Lubricentro Total",
                descripcion = "Cambio de aceite sintético y filtro de motor",
                fecha = "15 Ene 2026",
                fechaMillis = 1768435200000L,
                costo = 52000
            ),
            MantenimientoItem(
                id = "2",
                placa = "DDD-123",
                tipo = TipoMantenimiento.CORRECTIVO,
                taller = "Frenos del Valle",
                descripcion = "Sustitución de pastillas de freno delanteras",
                fecha = "28 Ago 2025",
                fechaMillis = 1756339200000L,
                costo = 85000
            ),
            MantenimientoItem(
                id = "3",
                placa = "DDD-123",
                tipo = TipoMantenimiento.PREVENTIVO,
                taller = "Taller Central",
                descripcion = "Alineación y revisión general",
                fecha = "10 May 2024",
                fechaMillis = 1715299200000L,
                costo = 45000
            ),
            // Mantenimientos DDD-124
            MantenimientoItem(
                id = "4",
                placa = "DDD-124",
                tipo = TipoMantenimiento.PREVENTIVO,
                taller = "Taller Mecánico Central",
                descripcion = "Cambio de aceite, filtros y afinación general",
                fecha = "15 Mar 2026",
                fechaMillis = 1773532800000L,
                costo = 98700
            ),
            MantenimientoItem(
                id = "5",
                placa = "DDD-124",
                tipo = TipoMantenimiento.CORRECTIVO,
                taller = "AutoServ López",
                descripcion = "Reparación de frenos traseros",
                fecha = "10 Nov 2025",
                fechaMillis = 1762732800000L,
                costo = 258500
            ),
            MantenimientoItem(
                id = "6",
                placa = "DDD-124",
                tipo = TipoMantenimiento.PREVENTIVO,
                taller = "Lubricentro Total",
                descripcion = "Mantenimiento preventivo 20,000 km",
                fecha = "14 Jun 2024",
                fechaMillis = 1718323200000L,
                costo = 65000
            ),
            // Mantenimientos DDD-125
            MantenimientoItem(
                id = "7",
                placa = "DDD-125",
                tipo = TipoMantenimiento.PREVENTIVO,
                taller = "Taller Central",
                descripcion = "Mantenimiento preventivo 40,000 km",
                fecha = "05 Feb 2026",
                fechaMillis = 1770249600000L,
                costo = 110000
            ),
            MantenimientoItem(
                id = "8",
                placa = "DDD-125",
                tipo = TipoMantenimiento.CORRECTIVO,
                taller = "Suspensión Pro",
                descripcion = "Cambio de amortiguadores y terminales",
                fecha = "18 Nov 2025",
                fechaMillis = 1763424000000L,
                costo = 220000
            ),
            // Mantenimientos DDD-126
            MantenimientoItem(
                id = "9",
                placa = "DDD-126",
                tipo = TipoMantenimiento.CORRECTIVO,
                taller = "Transmisiones CR",
                descripcion = "Reparación de embrague y disco",
                fecha = "12 Jul 2026",
                fechaMillis = 1783814400000L,
                costo = 340000
            ),
            MantenimientoItem(
                id = "10",
                placa = "DDD-126",
                tipo = TipoMantenimiento.PREVENTIVO,
                taller = "Taller 4x4",
                descripcion = "Cambio de fluidos y revisión de diferenciales",
                fecha = "20 Jul 2024",
                fechaMillis = 1721433600000L,
                costo = 125000
            ),
            // Mantenimientos DDD-127
            MantenimientoItem(
                id = "11",
                placa = "DDD-127",
                tipo = TipoMantenimiento.PREVENTIVO,
                taller = "Servicio Diesel Nissan",
                descripcion = "Alineación, balanceo y rotación de llantas",
                fecha = "08 Ago 2026",
                fechaMillis = 1786147200000L,
                costo = 75000
            ),
            // Mantenimientos DDD-128
            MantenimientoItem(
                id = "12",
                placa = "DDD-128",
                tipo = TipoMantenimiento.CORRECTIVO,
                taller = "Repuestos & Talleres Sur",
                descripcion = "Cambio de bomba de agua y faja de distribución",
                fecha = "19 Feb 2025",
                fechaMillis = 1739923200000L,
                costo = 195000
            )
        )
    }

    // Opciones de filtros
    val vehiculosOpciones = listOf("Todos los Vehículos", "DDD-123", "DDD-124", "DDD-125", "DDD-126", "DDD-127", "DDD-128")
    val tiposOpciones = listOf("Todos", "Preventivo", "Correctivo")
    val rangosFechas = listOf(
        "Todo el historial",
        "Últimos 3 meses",
        "Seleccionar año...",
        "📅 Personalizado (Elegir en calendario)..."
    )

    // Estados de filtros: selectedVehiculo inicia EXCLUSIVAMENTE con el vehículo seleccionado
    var selectedVehiculo by remember(placaInicial) {
        mutableStateOf(if (placaInicial.isNotBlank() && placaInicial != "Todos los Vehículos") placaInicial else "Todos los Vehículos")
    }
    var selectedTipo by remember { mutableStateOf("Todos") }
    var selectedRango by remember { mutableStateOf("Todo el historial") }

    // Estado para rango personalizado con DateRangePicker y selector de año
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showYearPickerDialog by remember { mutableStateOf(false) }
    var customDateStartMillis by remember { mutableStateOf<Long?>(null) }
    var customDateEndMillis by remember { mutableStateOf<Long?>(null) }

    // Estados de apertura de dropdowns
    var isVehiculoMenuOpen by remember { mutableStateOf(false) }
    var isTipoMenuOpen by remember { mutableStateOf(false) }
    var isRangoMenuOpen by remember { mutableStateOf(false) }

    // Estado del diálogo de evidencias fotográficas
    var selectedItemParaEvidencia by remember { mutableStateOf<MantenimientoItem?>(null) }

    // Filtrado interactivo en tiempo real por vehículo, tipo y rango de fechas
    val mantenimientosFiltrados = remember(
        selectedVehiculo,
        selectedTipo,
        selectedRango,
        customDateStartMillis,
        customDateEndMillis,
        mantenimientosIniciales
    ) {
        mantenimientosIniciales.filter { item ->
            // Filtro por vehículo
            val coincideVehiculo = selectedVehiculo == "Todos los Vehículos" || item.placa == selectedVehiculo

            // Filtro por tipo de mantenimiento
            val coincideTipo = when (selectedTipo) {
                "Preventivo" -> item.tipo == TipoMantenimiento.PREVENTIVO
                "Correctivo" -> item.tipo == TipoMantenimiento.CORRECTIVO
                else -> true
            }

            // Filtro por fecha (preestablecida, por año o personalizada con calendario)
            val coincideFecha = when {
                selectedRango == "Todo el historial" -> true
                selectedRango.startsWith("Año ") -> {
                    val anio = selectedRango.removePrefix("Año ").trim()
                    item.fecha.contains(anio)
                }
                selectedRango == "Últimos 3 meses" -> {
                    item.fecha.contains("Jul 2026") || item.fecha.contains("Ago 2026") || item.fecha.contains("Sep 2026") || item.fecha.contains("Set 2026")
                }
                customDateStartMillis != null && customDateEndMillis != null -> {
                    item.fechaMillis in customDateStartMillis!!..customDateEndMillis!!
                }
                else -> true
            }

            coincideVehiculo && coincideTipo && coincideFecha
        }
    }

    // Cálculo dinámico de la inversión total (Suma en Colones ₡)
    val costoTotal = remember(mantenimientosFiltrados) {
        mantenimientosFiltrados.sumOf { it.costo }
    }

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US)
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundCanvas
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

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

            // --- CONTENIDO CON SCROLL: FILTROS, RESUMEN E HISTORIAL ---
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
                        text = "Generación de Reportes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = NavyBluePrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // --- 2. FILTROS EN CASCADA ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Filtro: Vehículo
                        FilterDropdownSelector(
                            label = "VEHÍCULO",
                            selectedValue = selectedVehiculo,
                            options = vehiculosOpciones,
                            isOpen = isVehiculoMenuOpen,
                            onOpenChange = { isVehiculoMenuOpen = it },
                            onSelect = { selectedVehiculo = it }
                        )

                        // Filtro: Tipo de mantenimiento
                        FilterDropdownSelector(
                            label = "TIPO DE MANTENIMIENTO",
                            selectedValue = selectedTipo,
                            options = tiposOpciones,
                            isOpen = isTipoMenuOpen,
                            onOpenChange = { isTipoMenuOpen = it },
                            onSelect = { selectedTipo = it }
                        )

                        // Filtro: Rango de fechas
                        FilterDropdownSelector(
                            label = "RANGO DE FECHAS",
                            selectedValue = selectedRango,
                            options = rangosFechas,
                            isOpen = isRangoMenuOpen,
                            onOpenChange = { isRangoMenuOpen = it },
                            onSelect = { opcion ->
                                when (opcion) {
                                    "Elegir en calendario" -> {
                                        showDateRangePicker = true
                                    }
                                    "Seleccionar año" -> {
                                        showYearPickerDialog = true
                                    }
                                    else -> {
                                        selectedRango = opcion
                                        customDateStartMillis = null
                                        customDateEndMillis = null
                                    }
                                }
                            }
                        )
                    }
                }

                // --- 3. TARJETA DE INVERSIÓN TOTAL EN MANTENIMIENTO ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyBluePrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 22.dp)
                        ) {
                            Text(
                                text = "INVERSIÓN TOTAL EN MANTENIMIENTO",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = LightBlueHeader,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₡${currencyFormatter.format(costoTotal)}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Periodo: $selectedRango",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                // Título de la sección Historial
                item {
                    Text(
                        text = "HISTORIAL DE MANTENIMIENTOS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = NavyBluePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }

                // Lista de Registros de Mantenimiento
                if (mantenimientosFiltrados.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay registros para los filtros seleccionados",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(mantenimientosFiltrados, key = { it.id }) { item ->
                        MaintenanceCardItem(
                            item = item,
                            currencyFormatter = currencyFormatter,
                            onVerEvidencia = { selectedItemParaEvidencia = item }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // --- 4. BARRA DE BOTONES INFERIORES: VOLVER Y EXPORTAR ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón Volver
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
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

                    // Botón Exportar Reporte Excel
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Exportando reporte de flotilla a Excel (.xlsx)...",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .weight(2.2f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exportar Reporte Excel",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // --- 5. DIÁLOGO MODAL DE EVIDENCIA FOTOGRÁFICA (Req. 3) ---
        selectedItemParaEvidencia?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedItemParaEvidencia = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evidencias: ${item.placa}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyBluePrimary
                            )
                        )
                        IconButton(onClick = { selectedItemParaEvidencia = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "${item.descripcion} • ${item.fecha}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                        Text(
                            text = "Fotos y facturas adjuntadas (Requerimiento 3):",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )

                        // Simulación de fotos de evidencia adjuntas
                        item.evidencias.forEach { foto ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFBAE6FD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = foto,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Evidencia fotográfica adjunta",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedItemParaEvidencia = null }) {
                        Text("Cerrar", fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                    }
                }
            )
        }

        // --- 6. DIÁLOGO MODAL DE CALENDARIO PARA RANGO DE FECHAS PERSONALIZADO ---
        if (showDateRangePicker) {
            val dateRangePickerState = rememberDateRangePickerState()

            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val start = dateRangePickerState.selectedStartDateMillis
                            val end = dateRangePickerState.selectedEndDateMillis
                            if (start != null && end != null) {
                                customDateStartMillis = start
                                // Fin del día seleccionado (23:59:59.999 UTC)
                                customDateEndMillis = end + (24 * 60 * 60 * 1000L - 1)
                                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-ES")).apply {
                                    timeZone = TimeZone.getTimeZone("UTC")
                                }
                                selectedRango = "${formatter.format(Date(start))} – ${formatter.format(Date(end))}"
                            } else if (start != null) {
                                customDateStartMillis = start
                                customDateEndMillis = start + (24 * 60 * 60 * 1000L - 1)
                                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-ES")).apply {
                                    timeZone = TimeZone.getTimeZone("UTC")
                                }
                                selectedRango = formatter.format(Date(start))
                            }
                            showDateRangePicker = false
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null
                    ) {
                        Text("Aplicar", fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = Color.White)
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = {
                        Text(
                            text = "Rango de Fechas Personalizado",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyBluePrimary
                            ),
                            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp)
                        )
                    },
                    headline = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-ES")).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        val text = if (start != null && end != null) {
                            "${formatter.format(Date(start))} – ${formatter.format(Date(end))}"
                        } else if (start != null) {
                            "${formatter.format(Date(start))} – Fin"
                        } else {
                            "Fecha inicio – Fecha fin"
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = AccentBlue,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                        )
                    },
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        selectedDayContainerColor = NavyBluePrimary,
                        selectedDayContentColor = Color.White,
                        dayInSelectionRangeContainerColor = LightBlueHeader,
                        dayInSelectionRangeContentColor = NavyBluePrimary,
                        todayDateBorderColor = NavyBluePrimary,
                        todayContentColor = NavyBluePrimary
                    )
                )
            }
        }

        // --- 7. DIÁLOGO MODAL PARA ELEGIR CUALQUIER AÑO ---
        if (showYearPickerDialog) {
            AlertDialog(
                onDismissRequest = { showYearPickerDialog = false },
                title = {
                    Text(
                        text = "Seleccionar Año",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Elige el año que deseas consultar:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val aniosDisponibles = listOf("2026", "2025", "2024", "2023", "2022", "2021", "2020")
                        LazyColumn(modifier = Modifier.height(230.dp)) {
                            items(aniosDisponibles) { anio ->
                                val labelAnio = "Año $anio"
                                val isSelected = selectedRango == labelAnio
                                Surface(
                                    onClick = {
                                        selectedRango = labelAnio
                                        customDateStartMillis = null
                                        customDateEndMillis = null
                                        showYearPickerDialog = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) LightBlueHeader else Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AccentBlue else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = labelAnio,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) NavyBluePrimary else Color(0xFF1E293B)
                                            )
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showYearPickerDialog = false }) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

// Selector desplegable reutilizable para filtros
@Composable
private fun FilterDropdownSelector(
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
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = NavyBluePrimary,
                letterSpacing = 0.6.sp
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onOpenChange(!isOpen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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

// Tarjeta individual para un registro de mantenimiento
@Composable
private fun MaintenanceCardItem(
    item: MantenimientoItem,
    currencyFormatter: NumberFormat,
    onVerEvidencia: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: Placa, Botón Evidencia y Tipo (Preventivo/Correctivo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Placa del vehículo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = NavyBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.placa,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }

                // Botón Evidencia (abre las fotos de factura/trabajo)
                OutlinedButton(
                    onClick = onVerEvidencia,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBluePrimary.copy(alpha = 0.5f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "EVIDENCIA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                // Badge Tipo: PREVENTIVO o CORRECTIVO
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(item.tipo.bgBadge)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.tipo.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = item.tipo.textBadge,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Taller y Descripción
            Text(
                text = item.taller,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            )
            Text(
                text = item.descripcion,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Fila inferior: Fecha y Costo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.fecha,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                // Costo en Colones (₡)
                Text(
                    text = "₡${currencyFormatter.format(item.costo)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyBluePrimary
                    )
                )
            }
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MaintenanceReportScreenPreview() {
    TransAndinaAppTheme {
        MaintenanceReportScreen()
    }
}
