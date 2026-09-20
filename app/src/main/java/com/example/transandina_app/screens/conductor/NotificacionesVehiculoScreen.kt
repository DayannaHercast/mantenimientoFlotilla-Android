package com.example.transandina_app.screens.admin

import androidx.compose.material3.AlertDialog
import kotlinx.coroutines.Job
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.model.NotificacionVehiculoItem
import com.example.transandina_app.data.model.TipoNotificacion
import com.example.transandina_app.data.repository.NotificacionRepository
import com.example.transandina_app.ui.theme.TransAndinaAppTheme
import kotlinx.coroutines.launch

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val AccentBlue = Color(0xFF0284C7)
private val BackgroundCanvas = Color(0xFFF1F5F9)

@Composable
fun NotificacionesVehiculoScreen(
    modifier: Modifier = Modifier,
    placa: String? = null,
    notificacionRepository: NotificacionRepository = remember { NotificacionRepository() },
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var notificaciones by remember { mutableStateOf<List<NotificacionVehiculoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var detalle by remember { mutableStateOf<NotificacionVehiculoItem?>(null) }
    var errorAccion by remember { mutableStateOf<String?>(null) }
    var marcando by remember { mutableStateOf<Set<String>>(emptySet()) }
    var consultaJob by remember { mutableStateOf<Job?>(null) }
    fun abrirNotificacion(item: NotificacionVehiculoItem) {
        detalle = item
        errorAccion = null
        if (!item.leida && item.id !in marcando) {
            marcando = marcando + item.id
            coroutineScope.launch {
                try {
                    notificacionRepository.marcarLeida(item.id).onSuccess {
                        notificaciones = notificaciones.map { if (it.id == item.id) it.copy(leida = true) else it }
                        if (detalle?.id == item.id) detalle = item.copy(leida = true)
                    }.onFailure { errorAccion = it.localizedMessage ?: "No se pudo marcar la notificación como leída." }
                } finally { marcando = marcando - item.id }
            }
        }
    }

    // Ejecuta sp_ObtenerNotificacionesVehiculo(@Placa) a través de NotificacionRepository
    fun cargarNotificaciones() {
        consultaJob?.cancel()
        isLoading = true
        errorMessage = null
        consultaJob = coroutineScope.launch {
            val result = notificacionRepository.obtenerNotificaciones(placa)
            result.onSuccess { lista ->
                notificaciones = lista
                isLoading = false
            }.onFailure { error ->
                errorMessage = error.localizedMessage ?: "Error al conectar con la base de datos."
                isLoading = false
            }
        }
    }

    LaunchedEffect(placa) { cargarNotificaciones() }

    // Filtros: por tipo y por estado (leída / no leída)
    val tiposOpciones = remember { listOf("Todos") + TipoNotificacion.values().map { it.label } }
    var selectedTipo by remember { mutableStateOf("Todos") }
    var isTipoMenuOpen by remember { mutableStateOf(false) }

    val estadosOpciones = listOf("Todas", "No leídas", "Leídas")
    var selectedEstado by remember { mutableStateOf("Todas") }
    var isEstadoMenuOpen by remember { mutableStateOf(false) }

    val notificacionesFiltradas = remember(notificaciones, selectedTipo, selectedEstado) {
        notificaciones.filter { item ->
            val coincideTipo = selectedTipo == "Todos" || item.tipo.label == selectedTipo
            val coincideEstado = when (selectedEstado) {
                "No leídas" -> !item.leida
                "Leídas" -> item.leida
                else -> true
            }
            coincideTipo && coincideEstado
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.headlineSmall.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CompactFilterDropdown(
                            label = if (selectedTipo == "Todos") "Filtro por tipo" else selectedTipo,
                            options = tiposOpciones,
                            isOpen = isTipoMenuOpen,
                            onOpenChange = { isTipoMenuOpen = it },
                            onSelect = { selectedTipo = it },
                            modifier = Modifier.weight(1f)
                        )
                        CompactFilterDropdown(
                            label = if (selectedEstado == "Todas") "Filtro por estado" else selectedEstado,
                            options = estadosOpciones,
                            isOpen = isEstadoMenuOpen,
                            onOpenChange = { isEstadoMenuOpen = it },
                            onSelect = { selectedEstado = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                when {
                    isLoading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = NavyBluePrimary, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("Consultando notificaciones...", style = MaterialTheme.typography.bodyMedium.copy(color = NavyBluePrimary))
                                }
                            }
                        }
                    }
                    errorMessage != null -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                            ) {
                                Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.height(36.dp).width(36.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(text = errorMessage ?: "", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF991B1B)), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { cargarNotificaciones() }, colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary), shape = RoundedCornerShape(12.dp)) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Reintentar conexión", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    notificacionesFiltradas.isEmpty() -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (notificaciones.isEmpty()) "No hay notificaciones registradas." else "No hay notificaciones para los filtros seleccionados.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        items(notificacionesFiltradas, key = { it.id }) { notificacion ->
                            NotificacionCardItem(notificacion, notificacion.id in marcando) { abrirNotificacion(notificacion) }
                        }
                    }
                }

                errorAccion?.let { mensaje -> item { Text(mensaje, color = Color(0xFFDC2626)) } }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // --- BOTÓN: REGRESAR ---
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary)
                ) {
                    Text(text = "Regresar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary))
                }
            }
        }
    }
    detalle?.let { item ->
        AlertDialog(
            onDismissRequest = { detalle = null },
            title = { Text(item.titulo) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.descripcion)
                    Text(item.placa?.let { "Placa: $it" } ?: "Notificación general")
                    Text(item.fecha)
                    errorAccion?.let { Text(it, color = Color(0xFFDC2626)) }
                }
            },
            confirmButton = { TextButton(onClick = { detalle = null }) { Text("Cerrar") } }
        )
    }
}

@Composable
private fun NotificacionCardItem(notificacion: NotificacionVehiculoItem, guardando: Boolean, onVerMas: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = NavyBluePrimary, modifier = Modifier.height(18.dp).width(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notificacion.titulo,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary),
                        modifier = Modifier.weight(1f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(notificacion.tipo.bgBadge)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = notificacion.tipo.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = notificacion.tipo.textBadge, fontSize = 10.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notificacion.descripcion + (notificacion.placa?.let { " (Placa $it)" } ?: " (General)"),
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.height(14.dp).width(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = notificacion.fecha, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                }

                OutlinedButton(
                    onClick = onVerMas,
                    enabled = !guardando,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBluePrimary.copy(alpha = 0.5f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(text = if (guardando) "Guardando..." else if (notificacion.leida) "Ver detalle" else "Ver y marcar leída", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                }
            }
        }
    }
}

@Composable
private fun CompactFilterDropdown(
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
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFBAE6FD), contentColor = NavyBluePrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = NavyBluePrimary), maxLines = 1)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Opciones de filtro", tint = NavyBluePrimary)
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
fun NotificacionesVehiculoScreenPreview() {
    TransAndinaAppTheme {
        NotificacionesVehiculoScreen()
    }
}
