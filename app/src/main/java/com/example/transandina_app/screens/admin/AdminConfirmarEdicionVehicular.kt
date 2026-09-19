package com.example.transandina_app.screens.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
private val BackgroundCanvas = Color(0xFFCDEFFB)

@Composable
fun AdminConfirmarEdicionVehicular(
    modifier: Modifier = Modifier,
    mensaje: String = "La información del vehículo ha sido editada con éxito.",
    onFinalizarYVolver: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundCanvas
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            // --- 2. CONTENIDO CENTRAL: MENSAJE DE ÉXITO ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Edición Exitosa!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NavyBluePrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedButton(
                    onClick = onFinalizarYVolver,
                    shape = RoundedCornerShape(20.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = NavyBluePrimary
                    )
                ) {
                    Text(
                        text = "Finalizar y volver",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
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
fun AdminConfirmarEdicionVehicularPreview() {
    TransAndinaAppTheme {
        AdminConfirmarEdicionVehicular()
    }
}