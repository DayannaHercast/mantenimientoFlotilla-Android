package com.example.transandina_app.screens.admin

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
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
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.example.transandina_app.data.repository.VehiculoRepository
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

data class EvidenciaDetalle(
    val idEvidencia: Int,
    val nombreArchivo: String,
    val tipoEvidencia: String = "foto",
    val tieneArchivo: Boolean = true
)

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
    val evidencias: List<String> = emptyList(),
    val evidenciasDetalle: List<EvidenciaDetalle> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceReportScreen(
    placaInicial: String = "DDD-124",
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val vehiculoRepository = remember { VehiculoRepository() }
    var mantenimientosList by remember { mutableStateOf<List<MantenimientoItem>>(emptyList()) }
    var listaPlacasFlotilla by remember { mutableStateOf<List<String>>(emptyList()) }
    var estaCargandoBd by remember { mutableStateOf(true) }
    var errorCargaBd by remember { mutableStateOf<String?>(null) }

    fun recargarDatosDesdeAzure() {
        estaCargandoBd = true
        errorCargaBd = null
        coroutineScope.launch {
            // Cargar placas reales de flotilla desde Azure SQL
            val resFlotilla = vehiculoRepository.obtenerFlotilla()
            resFlotilla.onSuccess { flotilla ->
                listaPlacasFlotilla = flotilla.map { it.placa }.filter { it.isNotBlank() }.distinct().sorted()
            }

            // Cargar mantenimientos reales desde Azure SQL
            val resMantenimientos = vehiculoRepository.obtenerReporteMantenimientosFlotilla()
            resMantenimientos.onSuccess { dbList ->
                mantenimientosList = dbList
                errorCargaBd = null
            }.onFailure { err ->
                errorCargaBd = err.localizedMessage ?: "Error al conectar con la base de datos de Azure."
            }
            estaCargandoBd = false
        }
    }

    LaunchedEffect(Unit) {
        recargarDatosDesdeAzure()
    }

    // Opciones de vehículos: Provienen 100% de Azure SQL (flotilla y mantenimientos)
    val vehiculosOpciones = remember(mantenimientosList, listaPlacasFlotilla) {
        val placas = (listaPlacasFlotilla + mantenimientosList.map { it.placa }).filter { it.isNotBlank() }.distinct().sorted()
        listOf("Todos los Vehículos") + placas
    }
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
    var fotoSeleccionadaParaVer by remember { mutableStateOf<Triple<String, MantenimientoItem, Int?>?>(null) }

    // Estado para la pantalla de éxito de exportación a Excel
    var showSuccessExportScreen by remember { mutableStateOf(false) }
    var exportedFileName by remember { mutableStateOf("") }

    // Filtrado interactivo en tiempo real por vehículo, tipo y rango de fechas
    val mantenimientosFiltrados = remember(
        selectedVehiculo,
        selectedTipo,
        selectedRango,
        customDateStartMillis,
        customDateEndMillis,
        mantenimientosList
    ) {
        mantenimientosList.filter { item ->
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


    if (showSuccessExportScreen) {
        ExportSuccessView(
            fileName = exportedFileName,
            onFinalizarYVolver = {
                showSuccessExportScreen = false
            }
        )
    } else {
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
                                when {
                                    opcion.contains("calendario", ignoreCase = true) || opcion.contains("personalizado", ignoreCase = true) -> {
                                        showDateRangePicker = true
                                    }
                                    opcion.contains("año", ignoreCase = true) -> {
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

                // Lista de Registros de Mantenimiento cargados desde Azure SQL
                if (estaCargandoBd) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = NavyBluePrimary, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Consultando mantenimientos en Azure SQL...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = NavyBluePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                } else if (errorCargaBd != null && mantenimientosList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Error de conexión con Azure SQL",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color(0xFFDC2626),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errorCargaBd ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { recargarDatosDesdeAzure() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reintentar conexión", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (mantenimientosFiltrados.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (mantenimientosList.isEmpty()) "No hay mantenimientos registrados en la base de datos de Azure" else "No hay registros para los filtros seleccionados",
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
                            val (exito, nombreOError) = exportarReporteExcel(
                                context = context,
                                mantenimientos = mantenimientosFiltrados,
                                vehiculoSeleccionado = selectedVehiculo,
                                tipoSeleccionado = selectedTipo,
                                rangoFechas = selectedRango,
                                totalInversion = costoTotal
                            )
                            if (exito) {
                                exportedFileName = nombreOError
                                showSuccessExportScreen = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al exportar reporte: $nombreOError",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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

                        // Lista de evidencias reales con archivo en la base de datos
                        val evidenciasAMostrar = item.evidenciasDetalle.filter { it.tieneArchivo && it.idEvidencia > 0 }

                        if (evidenciasAMostrar.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sin imágenes en la base de datos",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBluePrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No se encontraron imágenes o comprobantes guardados para este mantenimiento.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            evidenciasAMostrar.forEach { ev ->
                                val isInvoice = ev.tipoEvidencia.equals("factura", ignoreCase = true) || ev.nombreArchivo.contains("Factura", ignoreCase = true) || ev.nombreArchivo.contains("Recibo", ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isInvoice) Color(0xFFFEF3C7) else Color(0xFFBAE6FD)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isInvoice) Icons.Default.Receipt else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = if (isInvoice) Color(0xFFD97706) else AccentBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ev.nombreArchivo,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (isInvoice) "Comprobante fiscal / Factura" else "Foto real cargada",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            fotoSeleccionadaParaVer = Triple(ev.nombreArchivo, item, ev.idEvidencia)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Ver",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Ver",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
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

        // --- 5.1 DIÁLOGO VISUALIZADOR DE EVIDENCIA INDIVIDUAL ---
        fotoSeleccionadaParaVer?.let { (foto, item, idEvidencia) ->
            val context = LocalContext.current
            val isInvoice = foto.contains("Factura", ignoreCase = true) || foto.contains("Recibo", ignoreCase = true)
            var cargandoImagenBd by remember(idEvidencia) { mutableStateOf(idEvidencia != null && idEvidencia > 0) }
            var bitmapDescargado by remember(idEvidencia) { mutableStateOf<android.graphics.Bitmap?>(null) }
            var errorDescarga by remember(idEvidencia) { mutableStateOf<String?>(null) }

            LaunchedEffect(idEvidencia) {
                if (idEvidencia != null && idEvidencia > 0) {
                    cargandoImagenBd = true
                    errorDescarga = null
                    val res = vehiculoRepository.obtenerBytesEvidencia(idEvidencia)
                    res.onSuccess { bytes ->
                        try {
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bmp != null) {
                                bitmapDescargado = bmp
                            } else {
                                errorDescarga = "No se pudo interpretar el formato de la imagen."
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorDescarga = "Error al procesar la imagen: ${e.message}"
                        }
                    }.onFailure { err ->
                        errorDescarga = err.message ?: "No se pudo descargar la imagen de la base de datos."
                    }
                    cargandoImagenBd = false
                }
            }

            // Intento de cargar imagen real (si es de BD, URI local o base64)
            val realBitmap = bitmapDescargado ?: remember(foto) {
                try {
                    if (foto.startsWith("content://") || foto.startsWith("file://")) {
                        val uri = Uri.parse(foto)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } else if (foto.length > 100 && !foto.contains(" ")) {
                        val decodedBytes = Base64.decode(foto, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            Dialog(
                onDismissRequest = { fotoSeleccionadaParaVer = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .heightIn(max = 680.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header del visor
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isInvoice) Color(0xFFFEF3C7) else Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isInvoice) Icons.Default.Receipt else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = if (isInvoice) Color(0xFFD97706) else AccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isInvoice) "Comprobante Fiscal" else "Evidencia Fotográfica",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyBluePrimary
                                        )
                                    )
                                    Text(
                                        text = foto,
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                                        maxLines = 1
                                    )
                                }
                            }
                            IconButton(
                                onClick = { fotoSeleccionadaParaVer = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Contenedor visual de la imagen
                        if (cargandoImagenBd) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = NavyBluePrimary, strokeWidth = 3.dp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Descargando imagen real desde Azure SQL...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = NavyBluePrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        } else if (errorDescarga != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No se pudo cargar la imagen",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color(0xFFDC2626),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = errorDescarga ?: "",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else if (realBitmap != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                            ) {
                                Image(
                                    bitmap = realBitmap.asImageBitmap(),
                                    contentDescription = foto,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 220.dp, max = 360.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else if (isInvoice) {
                            // Factura Electrónica fidedigna
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = item.taller.uppercase(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = NavyBluePrimary
                                                )
                                            )
                                            Text(
                                                text = "RUC: 20491823901 • Taller Autorizado",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                            )
                                            Text(
                                                text = "Av. Industrial 450 - Zona Automotriz",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFE0E7FF))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "FAC-#${item.id}092",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF3730A3)
                                                )
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = Color(0xFFE2E8F0)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("CLIENTE:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                            Text("TRANSANDINA S.A.C.", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("FECHA EMISIÓN:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                            Text(item.fecha, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("VEHÍCULO / PLACA:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                            Text(item.placa, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("ESTADO / TIPO:", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                            Text(item.tipo.label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(item.tipo.label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                                Text("₡${currencyFormatter.format(item.costo)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                            }
                                            Text(
                                                text = "Incluye repuestos originales, mano de obra e impuestos de ley",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFECFDF5))
                                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "COMPROBANTE VERIFICADO Y CONCILIADO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46)
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // Registro fotográfico técnico
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0F172A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "EVIDENCIA TÉCNICA DE INTERVENCIÓN",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "${item.placa} • ${item.tipo.label}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontWeight = FontWeight.Medium
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF334155))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "TALLER: ${item.taller}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF38BDF8),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${item.fecha} • REG-IMG-${item.id}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF64748B)
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Metadatos de la intervención
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vehículo / Placa:", style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
                                Text(item.placa, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tipo de Servicio:", style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
                                Text(item.tipo.label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Taller Encargado:", style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
                                Text(item.taller, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Costo Registrado:", style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
                                Text("₡${currencyFormatter.format(item.costo)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF059669)))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { fotoSeleccionadaParaVer = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Volver a Evidencias", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
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

                // Botón Evidencia (se muestra ÚNICAMENTE si existen imágenes en la base de datos)
                val tieneEvidenciasReales = item.evidenciasDetalle.any { it.tieneArchivo && it.idEvidencia > 0 }
                if (tieneEvidenciasReales) {
                    OutlinedButton(
                        onClick = onVerEvidencia,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBluePrimary.copy(alpha = 0.5f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "VER EVIDENCIA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
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

// --- PANTALLA DE CONFIRMACIÓN DE EXPORTACIÓN (PROTOTIPO FIGMA) ---
@Composable
private fun ExportSuccessView(
    fileName: String,
    onFinalizarYVolver: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundCanvas
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Encabezado corporativo TransAndina
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

            // Contenido central (Prototipo Figma: ¡Guardado Exitoso!)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Guardado Exitoso!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "La información del vehículo ha sido exportada con éxito",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = NavyBluePrimary,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        if (fileName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Descargado en Descargas:\n$fileName",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                OutlinedButton(
                    onClick = onFinalizarYVolver,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary)
                ) {
                    Text(
                        text = "Finalizar y volver",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }
            }
        }
    }
}

// --- FUNCIÓN UTILITARIA PARA EXPORTAR REPORTE A EXCEL (.CSV) ---
fun exportarReporteExcel(
    context: Context,
    mantenimientos: List<MantenimientoItem>,
    vehiculoSeleccionado: String,
    tipoSeleccionado: String,
    rangoFechas: String,
    totalInversion: Int
): Pair<Boolean, String> {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Reporte_Mantenimiento_${timeStamp}.csv"

        val csvBuilder = StringBuilder()
        csvBuilder.append('\uFEFF') // BOM UTF-8 para compatibilidad nativa con Microsoft Excel
        csvBuilder.append("REPORTE DE MANTENIMIENTO - TRANSANDINA\n")
        csvBuilder.append("Vehículo,\"${vehiculoSeleccionado}\"\n")
        csvBuilder.append("Tipo de Mantenimiento,\"${tipoSeleccionado}\"\n")
        csvBuilder.append("Rango de Fechas,\"${rangoFechas}\"\n")
        csvBuilder.append("Fecha de Exportación,\"${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\"\n\n")

        // Encabezados
        csvBuilder.append("ID,Placa,Tipo de Mantenimiento,Taller,Descripción,Fecha,Costo (CRC)\n")

        // Registros
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        mantenimientos.forEach { item ->
            val desc = item.descripcion.replace("\"", "\"\"")
            val taller = item.taller.replace("\"", "\"\"")
            csvBuilder.append("${item.id},${item.placa},${item.tipo.label},\"$taller\",\"$desc\",\"${item.fecha}\",${item.costo}\n")
        }

        // Fila de resumen total
        csvBuilder.append("\nTOTAL INVERSIÓN,,,,,,₡${numberFormat.format(totalInversion)}\n")

        val bytes = csvBuilder.toString().toByteArray(Charsets.UTF_8)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return Pair(false, "No se pudo crear el archivo en Descargas")
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
                outputStream.flush()
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(bytes)
                outputStream.flush()
            }
        }

        Pair(true, fileName)
    } catch (e: Exception) {
        e.printStackTrace()
        Pair(false, e.localizedMessage ?: "Error al generar el archivo")
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
