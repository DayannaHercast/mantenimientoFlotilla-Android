package com.example.transandina_app.screens.admin


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// Datos completos de la ficha técnica de un vehículo para esta pantalla.
// Los valores por defecto ("AAAA", "DD/MM/AAAA", "------ KM") son placeholders
// para cuando un dato todavía no está disponible desde la base de datos.
data class FichaTecnicaVehiculo(
    val placa: String = "JPL-073",
    val marca: String = "Toyota",
    val modelo: String = "HiAce",
    val estado: String = "Activo",
    val conductorActual: String = "Juan Pérez López",
    val anio: String = "AAAA",
    val capacidad: String = "5 personas",
    val vencimientoDocumentos: String = "DD/MM/AAAA",
    val kilometraje: String = "------ KM"
)

@Composable
fun AdminFichaTecnicaVehiculo(
    modifier: Modifier = Modifier,
    ficha: FichaTecnicaVehiculo = FichaTecnicaVehiculo(),
    onReasignarConductor: () -> Unit = {},
    onEditarInformacionVehicular: () -> Unit = {},
    onVolver: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

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
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyBlueDark, NavyBluePrimary)
                        )
                    )
                    .padding(top = 44.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
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
                        style = MaterialTheme.typography.bodySmall.copy(
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
                text = "Ficha Vehicular",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = NavyBluePrimary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. TARJETA "INFORMACIÓN" CON LOS DATOS DE LA FICHA ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Barra superior navy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyBluePrimary)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Información",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Detalle de la ficha
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardDetailBackground)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DatoFicha(etiqueta = "Placa", valor = ficha.placa)
                        DatoFicha(etiqueta = "Marca", valor = ficha.marca)
                        DatoFicha(etiqueta = "Modelo", valor = ficha.modelo)
                        DatoFicha(etiqueta = "Estado", valor = ficha.estado)
                        DatoFicha(etiqueta = "Conductor actual", valor = ficha.conductorActual)
                        DatoFicha(etiqueta = "Año", valor = ficha.anio)
                        DatoFicha(etiqueta = "Capacidad", valor = ficha.capacidad)
                        DatoFicha(etiqueta = "Vencimiento de documentos", valor = ficha.vencimientoDocumentos)
                        DatoFicha(etiqueta = "Kilometraje", valor = ficha.kilometraje)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. BOTÓN: REASIGNAR CONDUCTOR ---
            Button(
                onClick = onReasignarConductor,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(64.dp)
            ) {
                Text(
                    text = "Reasignar\nconductor",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 5. BOTÓN: EDITAR INFORMACIÓN VEHICULAR ---
            Button(
                onClick = onEditarInformacionVehicular,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "Editar información vehicular",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

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

@Composable
private fun DatoFicha(etiqueta: String, valor: String) {
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
fun AdminFichaTecnicaVehiculoPreview() {
    TransAndinaAppTheme {
        AdminFichaTecnicaVehiculo()
    }
}