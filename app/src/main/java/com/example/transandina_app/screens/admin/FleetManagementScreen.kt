package com.example.transandina_app.screens.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.ui.theme.TransAndinaAppTheme

// Colores corporativos TransAndina
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val AccentBlue = Color(0xFF0284C7)
private val BackgroundCanvas = Color(0xFFF1F5F9)

// Estados de Semáforo de Mantenimiento (Requerimiento 6)
enum class EstadoSemaforo(
    val label: String,
    val color: Color
) {
    AL_DIA("Al día", Color(0xFF10B981)),      // Verde
    PROXIMO("Próximo", Color(0xFFF59E0B)),    // Naranja / Ámbar
    ATRASADO("Atrasado", Color(0xFFEF4444))   // Rojo
}

// Modelo de datos para la lista de vehículos de la flota
data class VehiculoItem(
    val id: String,
    val nombre: String, // Marca y Modelo
    val placa: String,
    val estado: EstadoSemaforo
)

@Composable
fun FleetManagementScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onVehicleDetailClick: (VehiculoItem) -> Unit = {}
) {
    // Lista de vehículos de prueba (Mock Data) basada en el prototipo
    val vehiculosIniciales = remember {
        listOf(
            VehiculoItem("1", "Toyota Hilux 2022", "DDD-123", EstadoSemaforo.AL_DIA),
            VehiculoItem("2", "Hyundai Tucson 2023", "DDD-124", EstadoSemaforo.AL_DIA),
            VehiculoItem("3", "Hyundai Tucson 2022", "DDD-125", EstadoSemaforo.PROXIMO),
            VehiculoItem("4", "Toyota Hilux 2021", "DDD-126", EstadoSemaforo.ATRASADO),
            VehiculoItem("5", "Nissan Frontier 2023", "DDD-127", EstadoSemaforo.AL_DIA),
            VehiculoItem("6", "Mitsubishi L200 2020", "DDD-128", EstadoSemaforo.ATRASADO)
        )
    }

    // Estados de búsqueda y filtro
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Filtro por estado") }
    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    val filterOptions = listOf("Todos", "Al día", "Próximo", "Atrasado")

    // Filtrado dinámico en tiempo real por búsqueda de texto y estado
    val vehiculosFiltrados = remember(searchQuery, selectedFilter, vehiculosIniciales) {
        vehiculosIniciales.filter { vehiculo ->
            val coincideTexto = vehiculo.nombre.contains(searchQuery, ignoreCase = true) ||
                    vehiculo.placa.contains(searchQuery, ignoreCase = true)

            val coincideEstado = when (selectedFilter) {
                "Al día" -> vehiculo.estado == EstadoSemaforo.AL_DIA
                "Próximo" -> vehiculo.estado == EstadoSemaforo.PROXIMO
                "Atrasado" -> vehiculo.estado == EstadoSemaforo.ATRASADO
                else -> true
            }

            coincideTexto && coincideEstado
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
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


            // --- 2. BARRA DE BÚSQUEDA Y FILTRO POR ESTADO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Campo de búsqueda: Placa o Modelo
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Placa o modelo", style = MaterialTheme.typography.bodyMedium) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = AccentBlue
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Menú desplegable: Filtro por estado (Semáforo)
                    Box {
                        OutlinedButton(
                            onClick = { isFilterMenuExpanded = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFBAE6FD),
                                contentColor = NavyBluePrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7DD3FC)),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                text = if (selectedFilter == "Todos") "Estado: Todos" else selectedFilter,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Opciones de filtro",
                                tint = NavyBluePrimary
                            )
                        }

                        DropdownMenu(
                            expanded = isFilterMenuExpanded,
                            onDismissRequest = { isFilterMenuExpanded = false }
                        ) {
                            filterOptions.forEach { opcion ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            when (opcion) {
                                                "Al día" -> Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF10B981)))
                                                "Próximo" -> Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF59E0B)))
                                                "Atrasado" -> Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFEF4444)))
                                                else -> Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color.Gray))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = opcion,
                                                fontWeight = if (selectedFilter == opcion) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedFilter = opcion
                                        isFilterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. LISTA DE TARJETAS DE VEHÍCULOS ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (vehiculosFiltrados.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se encontraron vehículos con los filtros aplicados",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(vehiculosFiltrados, key = { it.id }) { vehiculo ->
                        VehicleCardItem(
                            vehiculo = vehiculo,
                            onVerMasClick = { onVehicleDetailClick(vehiculo) }
                        )
                    }
                }

                // Espacio inferior para el botón
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- 4. BOTÓN INFERIOR: REGRESAR AL MENÚ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavyBluePrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyBluePrimary)
                ) {
                    Text(
                        text = "Regresar al menú",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }
            }
        }
    }
}

// Componente para la tarjeta de cada vehículo con indicador de semáforo
@Composable
fun VehicleCardItem(
    vehiculo: VehiculoItem,
    modifier: Modifier = Modifier,
    onVerMasClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Información del vehículo
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehiculo.nombre,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Placa: ${vehiculo.placa}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Badge con indicador de semáforo (Verde, Amarillo/Naranja, Rojo)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(vehiculo.estado.color)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = vehiculo.estado.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón "Ver más"
            OutlinedButton(
                onClick = onVerMasClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBluePrimary.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "Ver más",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
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
fun FleetManagementScreenPreview() {
    TransAndinaAppTheme {
        FleetManagementScreen()
    }
}
