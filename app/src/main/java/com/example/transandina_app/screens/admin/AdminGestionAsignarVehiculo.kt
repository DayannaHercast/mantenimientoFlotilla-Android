package com.example.transandina_app.screens.admin


import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
// TODO: mover a com.example.transandina_app.ui.theme.Color.kt y compartir entre todas las pantallas
private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val CardDetailBackground = Color(0xFFD9D9D9)
private val BackgroundCanvas = Color(0xFFCDEFFB)

// Estados posibles de un vehículo dentro de la flotilla.
enum class EstadoVehiculo(val etiqueta: String) {
    ACTIVO("Activo"),
    INACTIVO("Inactivo")
}

// Opciones del filtro superior. "Todos" no es un estado real del vehículo,
// es una opción exclusiva del filtro para no aplicar ningún filtro de estado.
enum class FiltroEstadoVehiculo(val etiqueta: String) {
    TODOS("Todos"),
    ACTIVOS("Activos"),
    INACTIVOS("Inactivos")
}

// Modelo simple de vehículo para esta pantalla.
// id sirve para identificar cuál registro pedir al tocar "Ver Ficha Vehicular".
data class VehiculoFlota(
    val id: Int,
    val placa: String,
    val marca: String,
    val modelo: String,
    val estado: EstadoVehiculo,
    val conductorActual: String
)

// Lista de ejemplo, solo para @Preview y para probar la pantalla antes de conectar la base de datos.
private val vehiculosDePrueba = listOf(
    VehiculoFlota(1, "JPL-073", "Toyota", "HiAce", EstadoVehiculo.ACTIVO, "Juan Pérez López"),
    VehiculoFlota(2, "MNB-214", "Hyundai", "H1", EstadoVehiculo.INACTIVO, "Sin asignar"),
    VehiculoFlota(3, "QRS-559", "Toyota", "Coaster", EstadoVehiculo.ACTIVO, "Estefannía Portuguez Víquez")
)

@Composable
fun AdminGestionAsignarVehiculo(
    modifier: Modifier = Modifier,
    vehiculos: List<VehiculoFlota> = vehiculosDePrueba,
    onVerFichaVehicular: (VehiculoFlota) -> Unit = {},
    onVolver: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf(FiltroEstadoVehiculo.TODOS) }
    var menuFiltroExpandido by remember { mutableStateOf(false) }

    // Filtra por texto (placa o modelo) y por el estado elegido en el filtro.
    val vehiculosFiltrados = vehiculos.filter { vehiculo ->
        val coincideTexto = textoBusqueda.isBlank() ||
                vehiculo.placa.contains(textoBusqueda, ignoreCase = true) ||
                vehiculo.modelo.contains(textoBusqueda, ignoreCase = true)

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
            // --- 1. ENCABEZADO CON MARCA (mismo estilo que el resto de pantallas admin) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyBlueDark, NavyBluePrimary)
                        )
                    )
                    .padding(top = 44.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Gestión de Mantenimiento de Flotillas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TransAndina",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = LightBlueHeader,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 2. TÍTULO ---
            Text(
                text = "Gestión de Flotilla",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. BARRA DE BÚSQUEDA POR PLACA O MODELO ---
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Búsqueda por placa o modelo") },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. FILTRO POR ESTADO (Todos / Activos / Inactivos) ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .wrapContentWidth()
                            .clickable { menuFiltroExpandido = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(NavyBluePrimary)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estado",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Filtrar por estado",
                                tint = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Todos, Activos, Inactivos",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NavyBluePrimary
                                )
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = menuFiltroExpandido,
                        onDismissRequest = { menuFiltroExpandido = false }
                    ) {
                        FiltroEstadoVehiculo.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.etiqueta) },
                                onClick = {
                                    menuFiltroExpandido = false
                                    filtroSeleccionado = opcion
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. LISTA DE VEHÍCULOS FILTRADOS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (vehiculosFiltrados.isEmpty()) {
                    Text(
                        text = "No se encontraron vehículos con ese criterio.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    vehiculosFiltrados.forEach { vehiculo ->
                        VehiculoFlotaCard(
                            vehiculo = vehiculo,
                            filtroSeleccionado = filtroSeleccionado,
                            onVerFichaVehicular = { onVerFichaVehicular(vehiculo) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 6. BOTÓN VOLVER ---
            OutlinedButton(
                onClick = onVolver,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NavyBluePrimary
                ),
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Text(
                    text = "Volver",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// Tarjeta con los datos de un vehículo y el botón para ver su ficha completa.
@Composable
private fun VehiculoFlotaCard(
    vehiculo: VehiculoFlota,
    filtroSeleccionado: FiltroEstadoVehiculo,
    onVerFichaVehicular: () -> Unit
) {
    Column {
        // Encabezado navy con el nombre del filtro activo (Todos / Activos / Inactivos)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(NavyBluePrimary)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = filtroSeleccionado.etiqueta,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Detalle del vehículo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(CardDetailBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DatoVehiculo(etiqueta = "Placa", valor = vehiculo.placa)
            DatoVehiculo(etiqueta = "Marca", valor = vehiculo.marca)
            DatoVehiculo(etiqueta = "Modelo", valor = vehiculo.modelo)
            DatoVehiculo(etiqueta = "Estado", valor = vehiculo.estado.etiqueta)
            DatoVehiculo(etiqueta = "Conductor actual", valor = vehiculo.conductorActual)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVerFichaVehicular,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Text(
                text = "Ver Ficha\nVehicular",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DatoVehiculo(etiqueta: String, valor: String) {
    Text(
        text = "$etiqueta: $valor",
        style = MaterialTheme.typography.bodyMedium.copy(
            color = NavyBluePrimary,
            fontWeight = FontWeight.Medium
        )
    )
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminGestionAsignarVehiculoPreview() {
    TransAndinaAppTheme {
        AdminGestionAsignarVehiculo()
    }
}