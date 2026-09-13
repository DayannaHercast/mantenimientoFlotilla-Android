package com.example.transandina_app.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToUsers: () -> Unit = {},
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToFleet: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onLogout: () -> Unit = {}
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
            // --- 1. ENCABEZADO CON MARCA E ICONO DE NOTIFICACIONES ---
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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

                    // Icono de Notificaciones con Badge de alertas
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Color(0xFFEF4444), // Rojo de alerta
                                contentColor = Color.White
                            ) {
                                Text(text = "3", fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNavigateToAlerts,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Centro de Alertas",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 2. BANNER DE BIENVENIDA Y RESUMEN DEL ROL ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Menú Principal",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = NavyBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Módulo de Administración y Control",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. CUADRÍCULA 2x2 CON LOS 4 CUADRADOS PRINCIPALES ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FILA 1: Usuarios y Vehículos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminModuleCard(
                        title = "Usuarios",
                        subtitle = "Conductores y Mecánicos",
                        icon = Icons.Default.People,
                        iconTint = Color(0xFF2563EB),
                        iconBackground = Color(0xFFDBEAFE),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToUsers
                    )

                    AdminModuleCard(
                        title = "Vehículos",
                        subtitle = "Registro e Inventario",
                        icon = Icons.Default.DirectionsCar,
                        iconTint = Color(0xFF0D9488),
                        iconBackground = Color(0xFFCCFBF1),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVehicles
                    )
                }

                // FILA 2: Registros y Flotilla
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminModuleCard(
                        title = "Registros",
                        subtitle = "Mantenimientos y Fotos",
                        icon = Icons.Default.Build,
                        iconTint = Color(0xFFD97706),
                        iconBackground = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRecords
                    )

                    AdminModuleCard(
                        title = "Flotilla",
                        subtitle = "Semáforos y Reportes",
                        icon = Icons.Default.Assessment,
                        iconTint = Color(0xFF7C3AED),
                        iconBackground = Color(0xFFEDE9FE),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFleet
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // --- 4. BOTÓN: VOLVER A INICIO / CERRAR SESIÓN ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = NavyBluePrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = NavyBluePrimary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Volver a inicio",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyBluePrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// Componente reutilizable para cada uno de los 4 cuadrados del menú
@Composable
private fun AdminModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Icono con círculo de color suave
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Textos descriptivos
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyBluePrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                )
            }
        }
    }
}

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminHomeScreenPreview() {
    TransAndinaAppTheme {
        AdminHomeScreen()
    }
}
