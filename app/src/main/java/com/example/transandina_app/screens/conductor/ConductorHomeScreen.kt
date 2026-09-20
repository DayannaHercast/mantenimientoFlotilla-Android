package com.example.transandina_app.screens.conductor

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.transandina_app.data.database.ConductorSqlClient
import com.example.transandina_app.data.repository.NotificacionRepository
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
import androidx.compose.material3.ButtonDefaults
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
fun ConductorHomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToUsers: () -> Unit = {},
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToFleet: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val notificacionRepository = remember { NotificacionRepository() }
    val usuario by ConductorSqlClient.usuarioActual.collectAsState()
    val revision by NotificacionRepository.cambios.collectAsState()
    var intento by remember { mutableStateOf(0) }
    var noLeidas by remember { mutableStateOf(0) }
    var errorInicio by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(usuario, revision, intento) {
        noLeidas = 0
        errorInicio = null
        if (usuario == null) errorInicio = "Inicia sesión para cargar tus alertas."
        else notificacionRepository.obtenerNoLeidas().onSuccess { noLeidas = it }
            .onFailure { errorInicio = it.localizedMessage ?: "No se pudieron cargar las alertas." }
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
                            if (noLeidas > 0) Badge(
                                containerColor = Color(0xFFEF4444), // Rojo de alerta
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (noLeidas > 99) "99+" else noLeidas.toString(),
                                    fontWeight = FontWeight.Bold
                                )
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
                    text = "Módulo del conductor",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            errorInicio?.let { mensaje ->
                Text(
                    text = "$mensaje Toca para reintentar.", color = Color(0xFF991B1B),
                    modifier = Modifier.padding(horizontal = 20.dp).clickable { intento++ })
            }

            // --- 3. CUADRÍCULA 2x2 CON LOS 4 CUADRADOS PRINCIPALES ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FILA 1: Kilometraje y grafica
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CondcModuleCard(
                        title = "Kilometraje",
                        subtitle = "Registrar Kilometraje",
                        icon = Icons.Default.People,
                        iconTint = Color(0xFF2563EB),
                        iconBackground = Color(0xFFDBEAFE),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToUsers
                    )

                    CondcModuleCard(
                        title = "Grafico",
                        subtitle = "Ver el grafico de Kilometraje",
                        icon = Icons.Default.DirectionsCar,
                        iconTint = Color(0xFF0D9488),
                        iconBackground = Color(0xFFCCFBF1),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVehicles
                    )
                }

                // FILA 2: Mantenimiento e historial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CondcModuleCard(
                        title = "Mantenimiento",
                        subtitle = "Registrar Mantenimiento",
                        icon = Icons.Default.Build,
                        iconTint = Color(0xFFD97706),
                        iconBackground = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRecords
                    )

                    CondcModuleCard(
                        title = "Historial",
                        subtitle = "Historial vehicular",
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
                    onClick = { ConductorSqlClient.cerrarSesion(); onLogout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
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
private fun CondcModuleCard(
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
fun ConductorHomeScreenPreview() {
    TransAndinaAppTheme {
        ConductorHomeScreen()
    }
}