package com.example.transandina_app.screens.mecanico

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.transandina_app.data.database.ConductorSqlClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.model.IntervencionMecanico
import com.example.transandina_app.data.model.ResumenMecanico
import com.example.transandina_app.data.repository.MecanicoRepository
import kotlinx.coroutines.launch

private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val MecanicoBackground = Color(0xFFF1F5F9)
private val TableHeaderBackground = Color(0xFFE2E8F0)

@Composable
fun HistorialIntervencionesMecanicoScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val repository = remember { MecanicoRepository() }
    val coroutineScope = rememberCoroutineScope()

    val usuarioActual by ConductorSqlClient.usuarioActual.collectAsState()
    val identificador = usuarioActual?.toString().orEmpty()
    var resumen by remember { mutableStateOf<ResumenMecanico?>(null) }
    var intervenciones by remember { mutableStateOf<List<IntervencionMecanico>>(emptyList()) }
    var servicioFiltro by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun consultar() {
        if (cargando) return
        if (usuarioActual == null) {
            error = "Inicia sesión para consultar tu historial."
            return
        }
        cargando = true
        error = null
        resumen = null
        intervenciones = emptyList()
        val filtro = servicioFiltro.ifBlank { null }
        coroutineScope.launch {
            repository.obtenerResumen()
                .onSuccess { resumen = it }
                .onFailure { error = it.localizedMessage ?: "No se encontró el identificador." }
            repository.obtenerHistorial(servicio = filtro)
                .onSuccess { intervenciones = it }
                .onFailure { error = it.localizedMessage ?: "No se pudo cargar el historial." }
            cargando = false
        }
    }

    LaunchedEffect(usuarioActual) { consultar() }

    Surface(modifier = modifier.fillMaxSize(), color = MecanicoBackground, contentColor = Color(0xFF1E293B)) {
        Column(modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
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

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text("Historial de Intervenciones", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Consulta tus mantenimientos registrados",
                        style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(20.dp))

                // --- BUSQUEDA POR IDENTIFICADOR ---
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text(
                        "Identificador de tu sesión",
                        style = MaterialTheme.typography.titleSmall.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B),
                                focusedPlaceholderColor = Color(0xFF64748B),
                                unfocusedPlaceholderColor = Color(0xFF64748B),
                                focusedTrailingIconColor = NavyBluePrimary,
                                unfocusedTrailingIconColor = NavyBluePrimary,
                                cursorColor = Color(0xFF0284C7)
                            ),
                            singleLine = true,

                            value = identificador,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Identificador") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(enabled = !cargando, onClick = { consultar() }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = NavyBluePrimary)
                        }
                    }
                }

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = Color(0xFF991B1B), modifier = Modifier.padding(horizontal = 20.dp))
                }

                // --- FICHA DEL USUARIO ---
                resumen?.let { datos ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Text("Nombre del usuario", style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
                        Text(
                            datos.nombreUsuario,
                            style = MaterialTheme.typography.labelLarge.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ID", style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
                        Text(
                            datos.id.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Activo desde", style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
                        Text(
                            datos.activoDesde,
                            style = MaterialTheme.typography.labelLarge.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- FILTRO DE BUSQUEDA POR SERVICIO ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B),
                                focusedPlaceholderColor = Color(0xFF64748B),
                                unfocusedPlaceholderColor = Color(0xFF64748B),
                                focusedTrailingIconColor = NavyBluePrimary,
                                unfocusedTrailingIconColor = NavyBluePrimary,
                                cursorColor = Color(0xFF0284C7)
                            ),
                            singleLine = true,

                        value = servicioFiltro,
                        onValueChange = { servicioFiltro = it },
                        placeholder = { Text("Filtrar por servicio") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                            modifier = Modifier.heightIn(min = 50.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !cargando, onClick = { consultar() }, colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary, contentColor = Color.White)) {
                        Text("Buscar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- TABLA: FECHA / SERVICIO / TALLER / COSTO ---
                if (cargando) {
                    CircularProgressIndicator(color = NavyBluePrimary, modifier = Modifier.padding(horizontal = 20.dp))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp)).background(Color.White)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Row(modifier = Modifier.background(TableHeaderBackground).padding(horizontal = 12.dp, vertical = 14.dp)) {
                            listOf("Fecha", "Placa", "Servicio", "Taller", "Costo").forEach { encabezado ->
                                Text(
                                    encabezado,
                                    modifier = Modifier.width(120.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary
                                )
                            }
                        }
                        intervenciones.forEach { fila ->
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
                                Text(fila.fecha, modifier = Modifier.width(120.dp))
                                Text(fila.placa, modifier = Modifier.width(120.dp))
                                Text(fila.servicio, modifier = Modifier.width(120.dp))
                                Text(fila.taller, modifier = Modifier.width(120.dp))
                                Text("₡${fila.costo}", modifier = Modifier.width(120.dp))
                            }
                        }
                        if (intervenciones.isEmpty() && resumen != null) {
                            Text("No hay intervenciones registradas.", modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

            }

            // --- BOTON REGRESAR ---
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                OutlinedButton(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = NavyBluePrimary),
                        border = BorderStroke(1.5.dp, NavyBluePrimary),
                        onClick = onNavigateBack) { Text("Regresar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
