package com.example.transandina_app.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.transandina_app.data.repository.RegistroUsuarioRequest
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRegisterClick: (RegistroUsuarioRequest) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Campos del formulario
    var nombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    var segundoApellido by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var localValidationError by remember { mutableStateOf<String?>(null) }

    // Roles: Conductor, Mecánico o Administrador
    val roles = listOf("Conductor", "Mecánico", "Administrador")
    var selectedRole by remember { mutableStateOf(roles[0]) }
    var isRoleMenuExpanded by remember { mutableStateOf(false) }
    var numeroLicencia by remember { mutableStateOf("") }

    // Tipos de licencia para conductores
    val tiposLicencia = listOf(
        "B1" to "B1 - Vehículo Liviano",
        "B2" to "B2 - Camión Liviano (hasta 8 ton)",
        "B3" to "B3 - Camión Pesado (+8 ton)",
        "B4" to "B4 - Articulado / Trailer",
        "C1" to "C1 - Taxi",
        "C2" to "C2 - Autobús",
        "A1" to "A1 - Motocicleta (hasta 125cc)",
        "A2" to "A2 - Motocicleta (hasta 500cc)",
        "A3" to "A3 - Motocicleta (sin límite)",
        "D1" to "D1 - Tractor",
        "D2" to "D2 - Maquinaria Pesada",
        "E1" to "E1 - Universal / Especial"
    )
    var selectedTipoLicencia by remember { mutableStateOf("B1") }
    var isTipoLicenciaMenuExpanded by remember { mutableStateOf(false) }

    // Campos de Contraseña
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
        ) {
            // --- 1. ENCABEZADO CORPORATIVO TRANSANDINA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NavyBlueDark, NavyBluePrimary)
                        )
                    )
                    .padding(vertical = 26.dp, horizontal = 20.dp),
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
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. TARJETA PRINCIPAL DEL FORMULARIO ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar ilustrativo
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(LightBlueHeader)
                            .border(2.dp, AccentBlue.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar de Usuario",
                            modifier = Modifier.size(44.dp),
                            tint = AccentBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Registro de Cuenta",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NavyBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Ingresa tus datos y selecciona tu rol",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- SECCIÓN: SELECCIÓN DE ROL (DROPDOWN) ---
                    ExposedDropdownMenuBox(
                        expanded = isRoleMenuExpanded,
                        onExpandedChange = { isRoleMenuExpanded = !isRoleMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rol en el sistema *") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = AccentBlue
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoleMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(14.dp),
                            colors = outlinedFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = isRoleMenuExpanded,
                            onDismissRequest = { isRoleMenuExpanded = false }
                        ) {
                            roles.forEach { rol ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = rol,
                                            fontWeight = if (selectedRole == rol) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedRole == rol) NavyBluePrimary else Color.Unspecified
                                        )
                                    },
                                    onClick = {
                                        selectedRole = rol
                                        isRoleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Campo Condicional: Licencia de conducir (Solo visible si es Conductor)
                    AnimatedVisibility(
                        visible = selectedRole == "Conductor",
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(modifier = Modifier.height(2.dp))

                            // Número de Licencia
                            OutlinedTextField(
                                value = numeroLicencia,
                                onValueChange = { numeroLicencia = it },
                                label = { Text("Número de Licencia de Conducir *") },
                                placeholder = { Text("Ej: 1-1234-5678") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = AccentBlue
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = outlinedFieldColors()
                            )

                            // Selector de Tipo de Licencia (Dropdown)
                            ExposedDropdownMenuBox(
                                expanded = isTipoLicenciaMenuExpanded,
                                onExpandedChange = { isTipoLicenciaMenuExpanded = !isTipoLicenciaMenuExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val labelSeleccionado = tiposLicencia.find { it.first == selectedTipoLicencia }?.second ?: selectedTipoLicencia
                                OutlinedTextField(
                                    value = labelSeleccionado,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo de Licencia *") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = AccentBlue
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTipoLicenciaMenuExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = outlinedFieldColors(),
                                    supportingText = {
                                        Text("Selecciona la categoría autorizada")
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = isTipoLicenciaMenuExpanded,
                                    onDismissRequest = { isTipoLicenciaMenuExpanded = false }
                                ) {
                                    tiposLicencia.forEach { (codigo, descripcion) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = descripcion,
                                                    fontWeight = if (selectedTipoLicencia == codigo) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selectedTipoLicencia == codigo) NavyBluePrimary else Color.Unspecified
                                                )
                                            },
                                            onClick = {
                                                selectedTipoLicencia = codigo
                                                isTipoLicenciaMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo: Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campos: Apellidos (Lado a lado para mejor distribución)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = primerApellido,
                            onValueChange = { primerApellido = it },
                            label = { Text("1° Apellido") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = outlinedFieldColors()
                        )
                        OutlinedTextField(
                            value = segundoApellido,
                            onValueChange = { segundoApellido = it },
                            label = { Text("2° Apellido") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = outlinedFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo: Cédula
                    OutlinedTextField(
                        value = cedula,
                        onValueChange = { cedula = it },
                        label = { Text("Cédula / Identificación") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo: Correo Electrónico
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo: Teléfono
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AccentBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo: Contraseña
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentBlue) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = AccentBlue
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo: Confirmar Contraseña
                    val passwordsMatch = contrasena.isEmpty() || confirmarContrasena.isEmpty() || contrasena == confirmarContrasena
                    OutlinedTextField(
                        value = confirmarContrasena,
                        onValueChange = { confirmarContrasena = it },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentBlue) },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (isConfirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = AccentBlue
                                )
                            }
                        },
                        isError = !passwordsMatch,
                        supportingText = {
                            if (!passwordsMatch) {
                                Text(
                                    text = "Las contraseñas no coinciden",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = outlinedFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mensaje de Error (Validación o Base de Datos)
                    val displayError = errorMessage ?: localValidationError
                    if (!displayError.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayError,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF991B1B),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // --- BOTÓN PRINCIPAL: GUARDAR ---
                    Button(
                        onClick = {
                            val n = nombre.trim()
                            val p1 = primerApellido.trim()
                            val p2 = segundoApellido.trim()
                            val ced = cedula.trim().filter { it.isDigit() }
                            val email = correo.trim()
                            val tel = telefono.trim().filter { it.isDigit() }
                            val pass = contrasena.trim()
                            val passConf = confirmarContrasena.trim()
                            val lic = numeroLicencia.trim().filter { it.isDigit() }

                            if (n.isEmpty() || p1.isEmpty() || p2.isEmpty() || ced.isEmpty() || email.isEmpty() || tel.isEmpty() || pass.isEmpty()) {
                                localValidationError = "Por favor completa todos los campos obligatorios."
                                return@Button
                            }

                            if (!email.contains("@") || !email.contains(".")) {
                                localValidationError = "Por favor ingresa un correo electrónico válido."
                                return@Button
                            }

                            if (selectedRole == "Conductor") {
                                if (lic.isEmpty()) {
                                    localValidationError = "El número de licencia es obligatorio para conductores."
                                    return@Button
                                }
                                if (selectedTipoLicencia.isBlank()) {
                                    localValidationError = "El tipo de licencia es obligatorio para conductores."
                                    return@Button
                                }
                            }

                            if (pass != passConf) {
                                localValidationError = "Las contraseñas no coinciden."
                                return@Button
                            }

                            if (pass.length < 4) {
                                localValidationError = "La contraseña debe tener al menos 4 caracteres."
                                return@Button
                            }

                            localValidationError = null
                            onRegisterClick(
                                RegistroUsuarioRequest(
                                    nombre = n,
                                    primerApellido = p1,
                                    segundoApellido = p2,
                                    cedula = ced,
                                    correo = email,
                                    telefono = tel,
                                    rol = selectedRole,
                                    numLicencia = if (selectedRole == "Conductor") lic else null,
                                    tipoLicencia = if (selectedRole == "Conductor") selectedTipoLicencia else null,
                                    contrasena = pass
                                )
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBluePrimary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Guardar Cuenta",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- BOTÓN SECUNDARIO: VOLVER A INICIO ---
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBluePrimary)
                    ) {
                        Text(
                            text = "Volver a inicio",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Colores unificados para los campos de texto
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    focusedLabelColor = AccentBlue,
    cursorColor = AccentBlue,
    focusedContainerColor = Color(0xFFF8FAFC),
    unfocusedContainerColor = Color(0xFFF8FAFC)
)

// --- VISTA PREVIA PARA ANDROID STUDIO ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    TransAndinaAppTheme {
        RegisterScreen()
    }
}
