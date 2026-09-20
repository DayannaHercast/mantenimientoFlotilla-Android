package com.example.transandina_app.screens.mecanico

import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transandina_app.data.model.CategoriaServicioConductor
import com.example.transandina_app.data.model.MecanicoRegistroState
import com.example.transandina_app.data.repository.MecanicoRepository
import com.example.transandina_app.util.ConductorFormato
import kotlinx.coroutines.launch

private val NavyBluePrimary = Color(0xFF132A60)
private val NavyBlueDark = Color(0xFF0A1838)
private val LightBlueHeader = Color(0xFFE0F2FE)
private val MecanicoBackground = Color(0xFFF1F5F9)
private val CardBackground = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMantenimientoMecanicoScreen(
    estado: MecanicoRegistroState,
    modifier: Modifier = Modifier,
    onNavigateBack: (MecanicoRegistroState) -> Unit = {},
    onRegistroExitoso: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { MecanicoRepository() }
    val coroutineScope = rememberCoroutineScope()

    var categorias by remember { mutableStateOf<List<CategoriaServicioConductor>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf(estado.categoria ?: "") }
    var expandirCategoria by remember { mutableStateOf(false) }

    var kilometraje by remember { mutableStateOf(estado.kilometrajeActual?.toString() ?: "") }
    var taller by remember { mutableStateOf(estado.taller ?: "") }
    var costo by remember { mutableStateOf(estado.costo ?: "") }
    var descripcion by remember { mutableStateOf(estado.descripcion ?: "") }
    var archivosSeleccionados by remember { mutableStateOf(estado.comprobantesUris.map { Uri.parse(it) }) }

    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun volver() {
        onNavigateBack(estado.copy(
            kilometrajeActual = kilometraje.toIntOrNull(), categoria = categoriaSeleccionada,
            taller = taller, costo = costo, descripcion = descripcion,
            comprobantesUris = archivosSeleccionados.map { it.toString() }
        ))
    }
    BackHandler { if (!enviando) volver() }

    val selectorArchivos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris -> archivosSeleccionados = uris }

    LaunchedEffect(Unit) {
        repository.obtenerCategorias()
            .onSuccess { categorias = it }
            .onFailure { error = it.localizedMessage ?: "No se pudo cargar el catálogo de categorías." }
    }

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
                    Text("Detalle del Mantenimiento", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = NavyBluePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Paso 2 de 2 · Servicio y comprobantes",
                        style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(20.dp))

                error?.let {
                    Text(it, color = Color(0xFF991B1B), modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- FORMULARIO ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardBackground)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampoFormulario("Kilometraje actual") {
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

                            value = kilometraje,
                            onValueChange = { nuevo -> if (nuevo.all { it.isDigit() }) kilometraje = nuevo },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    CampoFormulario("Categoría") {
                        ExposedDropdownMenuBox(expanded = expandirCategoria, onExpandedChange = { expandirCategoria = it }) {
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

                                value = categoriaSeleccionada,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Selecciona una categoría") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirCategoria) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandirCategoria, onDismissRequest = { expandirCategoria = false }) {
                                categorias.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria.nombreCategoria) },
                                        onClick = { categoriaSeleccionada = categoria.nombreCategoria; expandirCategoria = false }
                                    )
                                }
                            }
                        }
                    }

                    CampoFormulario("Taller") {
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
                            value = taller, onValueChange = { taller = it }, modifier = Modifier.fillMaxWidth())
                    }

                    CampoFormulario("Costo monetario") {
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

                            value = costo,
                            onValueChange = { nuevo -> if (ConductorFormato.entradaCostoValida(nuevo)) costo = nuevo },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    CampoFormulario("Descripción") {
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

                            value = descripcion,
                            onValueChange = { descripcion = it },
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            maxLines = 5
                        )
                    }

                    CampoFormulario("Evidencias") {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = NavyBluePrimary),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            enabled = !enviando, onClick = { selectorArchivos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (archivosSeleccionados.isEmpty()) "Adjuntar comprobantes"
                                else "${archivosSeleccionados.size} archivo(s) seleccionado(s)"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

            }

            // --- BOTONES: REGRESAR / REGISTRAR ---
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = NavyBluePrimary),
                        border = BorderStroke(1.5.dp, NavyBluePrimary),
                        onClick = { volver() }, enabled = !enviando) { Text("Regresar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                Button(
                        modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                        shape = RoundedCornerShape(16.dp),

                    enabled = !enviando && kilometraje.isNotBlank() && categoriaSeleccionada.isNotBlank()
                        && categorias.any { it.nombreCategoria == categoriaSeleccionada }
                        && taller.isNotBlank() && costo.isNotBlank() && descripcion.isNotBlank(),
                    onClick = {
                        val montoCosto = ConductorFormato.costo(costo)
                        val km = kilometraje.toIntOrNull()
                        val fechaTexto = estado.fechaMillis?.let { ConductorFormato.fechaApi(it) }
                        if (montoCosto == null || km == null || fechaTexto == null
                            || estado.placa == null || estado.tipoServicio == null
                        ) {
                            error = "Revisa los datos ingresados."
                            return@Button
                        }
                        enviando = true
                        error = null
                        coroutineScope.launch {
                            repository.registrarMantenimiento(
                                placa = estado.placa,
                                kilometrajeActual = km,
                                tipoServicio = estado.tipoServicio,
                                categoria = categoriaSeleccionada,
                                fecha = fechaTexto,
                                taller = taller,
                                costo = montoCosto,
                                descripcion = descripcion,
                                context = context,
                                comprobantesUris = archivosSeleccionados.map { it.toString() }
                            ).onSuccess {
                                enviando = false
                                onRegistroExitoso()
                            }.onFailure {
                                enviando = false
                                error = it.localizedMessage ?: "No se pudo registrar el mantenimiento."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary, contentColor = Color.White)
                ) { Text(if (enviando) "Registrando..." else "Registrar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            }

        }
    }
}

@Composable
private fun CampoFormulario(etiqueta: String, contenido: @Composable () -> Unit) {
    Column {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelLarge.copy(color = NavyBluePrimary, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        contenido()
    }
}
