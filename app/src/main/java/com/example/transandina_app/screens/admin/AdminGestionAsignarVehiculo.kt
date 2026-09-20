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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import com.example.transandina_app.data.repository.VehiculoRepository
import com.example.transandina_app.ui.theme.TransAndinaAppTheme
import kotlinx.coroutines.launch

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Estados posibles de un vehículo dentro de la flotilla
enum class EstadoVehiculo(val etiqueta: String) {
    ACTIVO("Activo"),
    INACTIVO("Inactivo")
}

// Opciones del filtro superior
enum class FiltroEstadoVehiculo(val etiqueta: String) {
    TODOS("Todos los estados"),
    ACTIVOS("Solo Activos"),
    INACTIVOS("Solo Inactivos")
}

// Modelo simple de vehículo para esta pantalla
data class VehiculoFlota(
    val id: Int,
    val placa: String,
    val marca: String,
    val modelo: String,
    val estado: EstadoVehiculo,
    val conductorActual: String
)

// Lista de ejemplo para @Preview y pruebas
private val vehiculosDePrueba = listOf(
    VehiculoFlota(1, "DDD-123", "Toyota", "Hilux", EstadoVehiculo.ACTIVO, "Juan Pérez López"),
    VehiculoFlota(2, "DDD-124", "Freightliner", "Cascadia", EstadoVehiculo.ACTIVO, "Estefannía Portuguez Víquez"),
    VehiculoFlota(3, "DDD-128", "Mercedes-Benz", "Actros", EstadoVehiculo.INACTIVO, "Sin asignar")
)

@Composable
fun AdminGestionAsignarVehiculo(
    modifier: Modifier = Modifier,
    vehiculosIniciales: List<VehiculoFlota> = emptyList(),
    vehiculoRepository: VehiculoRepository = remember { VehiculoRepository() },
    onVerFichaVehicular: (VehiculoFlota) -> Unit = {},
    onVolver: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var vehiculosList by remember { mutableStateOf(vehiculosIniciales) }
    var isLoading by remember { mutableStateOf(vehiculosIniciales.isEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var textoBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf(FiltroEstadoVehiculo.TODOS) }
    var menuFiltroExpandido by remember { mutableStateOf(false) }

    fun cargarFlotilla() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val res = vehiculoRepository.obtenerFlotillaAsignacion()
            res.onSuccess { lista ->
                vehiculosList = lista
                isLoading = false
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Error al conectar con la base de datos."
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (vehiculosIniciales.isEmpty()) {
            cargarFlotilla()
        }
    }

    // Filtra por texto (placa o modelo) y por estado
    val vehiculosFiltrados = vehiculosList.filter { vehiculo ->
        val coincideTexto = textoBusqueda.isBlank() ||
                vehiculo.placa.contains(textoBusqueda, ignoreCase = true) ||
                vehiculo.modelo.contains(textoBusqueda, ignoreCase = true) ||
                vehiculo.marca.contains(textoBusqueda, ignoreCase = true)

        val coincideEstado = when (filtroSeleccionado) {
            FiltroEstadoVehiculo.TODOS -> true
            FiltroEstadoVehiculo.ACTIVOS -> vehiculo.estado == EstadoVehiculo.ACTIVO
            FiltroEstadoVehiculo.INACTIVOS -> vehiculo.estado == EstadoVehiculo.INACTIVO
        }

        coincideTexto && coincideEstado
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
                    text = "Gestión de Flotilla",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Consulta, asignación y estado técnico de unidades de la base de datos",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. CONTROLES: BÚSQUEDA Y FILTRO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Barra de búsqueda moderna
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = {
                        Text("Buscar por placa, modelo o marca...", color = Color(0xFF94A3B8))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = NavyBluePrimary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = NavyBluePrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de filtro por estado
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { menuFiltroExpandido = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = NavyBluePrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = NavyBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Filtro: ${filtroSeleccionado.etiqueta}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = NavyBluePrimary,
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
                        expanded = menuFiltroExpandido,
                        onDismissRequest = { menuFiltroExpandido = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        FiltroEstadoVehiculo.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = opcion.etiqueta,
                                        fontWeight = if (filtroSeleccionado == opcion) FontWeight.Bold else FontWeight.Normal,
                                        color = if (filtroSeleccionado == opcion) NavyBluePrimary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    menuFiltroExpandido = false
                                    filtroSeleccionado = opcion
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 4. LISTA DE TARJETAS DE VEHÍCULOS O ESTADO DE CARGA ---
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
                            text = "Cargando vehículos desde la base de datos...",
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
                        OutlinedButton(onClick = { cargarFlotilla() }) {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (vehiculosFiltrados.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron vehículos que coincidan con la búsqueda.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        vehiculosFiltrados.forEach { vehiculo ->
                            VehiculoFlotaCard(
                                vehiculo = vehiculo,
                                onVerFichaVehicular = { onVerFichaVehicular(vehiculo) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 5. BOTÓN VOLVER ---
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
                        .fillMaxWidth(0.6f)
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
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Tarjeta unificada y moderna para cada vehículo de la flota
@Composable
private fun VehiculoFlotaCard(
    vehiculo: VehiculoFlota,
    onVerFichaVehicular: () -> Unit
) {
    val isActivo = vehiculo.estado == EstadoVehiculo.ACTIVO
    val bgBadge = if (isActivo) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
    val textBadge = if (isActivo) Color(0xFF047857) else Color(0xFFB91C1C)

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
            // Fila superior: Icono, Placa y Badge de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LightBlueHeader),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = NavyBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${vehiculo.marca} ${vehiculo.modelo}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyBluePrimary
                            )
                        )
                        Text(
                            text = "Placa: ${vehiculo.placa}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Badge de estado
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgBadge)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vehiculo.estado.etiqueta,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textBadge,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila conductor asignado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Conductor actual: ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B)
                    )
                )
                Text(
                    text = vehiculo.conductorActual,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyBluePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón integrado "Ver Ficha Vehicular"
            Button(
                onClick = onVerFichaVehicular,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = "Ver Ficha Vehicular",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminGestionAsignarVehiculoPreview() {
    TransAndinaAppTheme {
        AdminGestionAsignarVehiculo(vehiculosIniciales = vehiculosDePrueba)
    }
}