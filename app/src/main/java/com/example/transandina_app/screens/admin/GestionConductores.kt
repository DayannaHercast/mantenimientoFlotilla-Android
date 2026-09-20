package com.example.transandina_app.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.repository.UsuarioRepository
import com.example.transandina_app.ui.theme.TransAndinaAppTheme
import kotlinx.coroutines.launch

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Estados posibles de un conductor
enum class EstadoConductor(val etiqueta: String) {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    SUSPENDIDO("Suspendido")
}

// Colores semánticos para el badge de estado
private fun getEstadoConductorColors(estado: EstadoConductor): Pair<Color, Color> {
    return when (estado) {
        EstadoConductor.ACTIVO -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))       // Verde
        EstadoConductor.SUSPENDIDO -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))   // Ámbar
        EstadoConductor.INACTIVO -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))     // Rojo
    }
}

// Modelo simple de conductor
data class Conductor(
    val id: Int,
    val nombreCompleto: String,
    val estado: EstadoConductor
)

// Lista de ejemplo para @Preview
private val conductoresDePrueba = listOf(
    Conductor(1, "Juan Pérez López", EstadoConductor.ACTIVO),
    Conductor(2, "Estefannía Portuguez Víquez", EstadoConductor.ACTIVO),
    Conductor(3, "Francisco Solano Molina", EstadoConductor.SUSPENDIDO),
    Conductor(4, "Leticia Rivera Fernández", EstadoConductor.INACTIVO)
)

@Composable
fun GestionConductores(
    modifier: Modifier = Modifier,
    conductoresIniciales: List<Conductor> = emptyList(),
    usuarioRepository: UsuarioRepository = remember { UsuarioRepository() },
    onEstadoChange: (conductorId: Int, nuevoEstado: EstadoConductor) -> Unit = { _, _ -> },
    onVolver: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var conductoresList by remember { mutableStateOf(conductoresIniciales) }
    var isLoading by remember { mutableStateOf(conductoresIniciales.isEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun cargarConductores() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val res = usuarioRepository.obtenerConductores()
            res.onSuccess { lista ->
                conductoresList = lista
                isLoading = false
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Error al conectar con la base de datos."
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (conductoresIniciales.isEmpty()) {
            cargarConductores()
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

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. TÍTULO Y SUBTÍTULO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Conductores",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Personal asignable a vehículos de la flota y estado operativo",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. LISTA DE CONDUCTORES O ESTADO DE CARGA ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NavyBluePrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Consultando base de datos...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                        )
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFB91C1C)),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { cargarConductores() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    conductoresList.forEach { conductor ->
                        ConductorRow(
                            conductor = conductor,
                            onEstadoSeleccionado = { nuevoEstado ->
                                // Actualizar en base de datos via SP sp_ActualizarEstadoUsuario
                                coroutineScope.launch {
                                    val result = usuarioRepository.actualizarEstadoUsuario(conductor.id, nuevoEstado)
                                    result.onSuccess {
                                        // Refrescar en memoria
                                        conductoresList = conductoresList.map {
                                            if (it.id == conductor.id) it.copy(estado = nuevoEstado) else it
                                        }
                                        Toast.makeText(context, "${conductor.nombreCompleto}: ${nuevoEstado.etiqueta}", Toast.LENGTH_SHORT).show()
                                        onEstadoChange(conductor.id, nuevoEstado)
                                    }.onFailure { error ->
                                        Toast.makeText(context, "Error al guardar en BD: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // --- 4. BOTÓN REGRESAR AL MENÚ ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onVolver,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavyBluePrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Regresar al menú",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Tarjeta individual para un conductor con avatar y badge interactivo de estado
@Composable
private fun ConductorRow(
    conductor: Conductor,
    onEstadoSeleccionado: (EstadoConductor) -> Unit
) {
    var menuExpandido by remember { mutableStateOf(false) }
    val (bgBadge, textBadge) = getEstadoConductorColors(conductor.estado)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar con icono de conductor
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightBlueHeader),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = NavyBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Nombre y detalles del conductor
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conductor.nombreCompleto,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyBluePrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ID: CH-${conductor.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B)
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Badge selector de estado tipo píldora interactivo
            Box {
                Surface(
                    onClick = { menuExpandido = true },
                    shape = RoundedCornerShape(20.dp),
                    color = bgBadge,
                    border = androidx.compose.foundation.BorderStroke(1.dp, textBadge.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = conductor.estado.etiqueta,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = textBadge,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Cambiar estado",
                            tint = textBadge,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    EstadoConductor.entries.forEach { opcion ->
                        val (opBg, opText) = getEstadoConductorColors(opcion)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(opText)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opcion.etiqueta,
                                        fontWeight = if (conductor.estado == opcion) FontWeight.Bold else FontWeight.Normal,
                                        color = if (conductor.estado == opcion) opText else Color.Unspecified
                                    )
                                }
                            },
                            onClick = {
                                menuExpandido = false
                                onEstadoSeleccionado(opcion)
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GestionConductoresPreview() {
    TransAndinaAppTheme {
        GestionConductores(conductoresIniciales = conductoresDePrueba)
    }
}