package com.example.transandina_app.screens.admin

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
private val AccentBlue = Color(0xFF0284C7)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Lista de ejemplo de conductores disponibles para @Preview
private val conductoresDisponiblesDePrueba = listOf(
    Conductor(2, "Estefannía Portuguez Víquez", EstadoConductor.ACTIVO),
    Conductor(4, "Leticia Rivera Fernández", EstadoConductor.ACTIVO)
)

@Composable
fun AdminReasignarConductor(
    modifier: Modifier = Modifier,
    placa: String = "",
    conductorActual: String = "Juan Pérez López",
    conductoresDisponiblesIniciales: List<Conductor> = emptyList(),
    usuarioRepository: UsuarioRepository = remember { UsuarioRepository() },
    onConfirmarReasignacion: (nuevoConductorId: Int) -> Unit = {},
    onCancelar: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var conductoresList by remember { mutableStateOf(conductoresDisponiblesIniciales) }
    var isLoading by remember { mutableStateOf(conductoresDisponiblesIniciales.isEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var conductorSeleccionadoId by remember { mutableStateOf<Int?>(null) }

    fun cargarConductoresDisponibles() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val res = usuarioRepository.obtenerConductoresDisponibles(placa.ifBlank { null })
            res.onSuccess { lista ->
                conductoresList = lista
                isLoading = false
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Error al obtener conductores disponibles."
                isLoading = false
            }
        }
    }

    LaunchedEffect(placa) {
        if (conductoresDisponiblesIniciales.isEmpty()) {
            cargarConductoresDisponibles()
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
                    text = "Reasignar Conductor",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Modifica la asignación de chofer para este vehículo",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. TARJETA: CONDUCTOR ACTUAL ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "CONDUCTOR ACTUAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = conductorActual,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary
                                )
                            )
                            Text(
                                text = "Chofer actualmente registrado en la base de datos",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 4. AVISO INFORMATIVO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2FE))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Al reasignar, el nuevo conductor recibirá una notificación automática en su aplicación.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF0369A1),
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 5. CONDUCTORES DISPONIBLES O ESTADO DE CARGA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "CONDUCTORES DISPONIBLES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NavyBluePrimary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Cargando conductores disponibles...",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                            )
                        }
                    }
                } else if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage ?: "Error",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB91C1C)),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { cargarConductoresDisponibles() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                } else if (conductoresList.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "No hay otros conductores activos disponibles en este momento.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        conductoresList.forEach { conductor ->
                            val seleccionado = conductor.id == conductorSeleccionadoId
                            ConductorDisponibleCard(
                                conductor = conductor,
                                seleccionado = seleccionado,
                                onSelect = { conductorSeleccionadoId = conductor.id }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // --- 6. BOTONES DE ACCIÓN: CANCELAR Y CONFIRMAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelar,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }

                Button(
                    onClick = {
                        conductorSeleccionadoId?.let { onConfirmarReasignacion(it) }
                    },
                    enabled = conductorSeleccionadoId != null,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyBluePrimary,
                        disabledContainerColor = NavyBluePrimary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Confirmar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Tarjeta individual para cada conductor disponible con selección interactiva
@Composable
private fun ConductorDisponibleCard(
    conductor: Conductor,
    seleccionado: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) Color(0xFFF0FDF4) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (seleccionado) Color(0xFF10B981) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (seleccionado) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (seleccionado) Color(0xFFD1FAE5) else LightBlueHeader),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (seleccionado) Color(0xFF047857) else NavyBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = conductor.nombreCompleto,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                    Text(
                        text = "Disponible para asignación",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            if (seleccionado) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFD1FAE5))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Elegido",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onSelect,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Seleccionar",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = NavyBluePrimary
                        )
                    )
                }
            }
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminReasignarConductorPreview() {
    TransAndinaAppTheme {
        AdminReasignarConductor(conductoresDisponiblesIniciales = conductoresDisponiblesDePrueba)
    }
}