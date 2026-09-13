package com.example.transandina_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.example.transandina_app.data.model.Usuario
import com.example.transandina_app.data.repository.AuthRepository
import kotlinx.coroutines.launch
import com.example.transandina_app.screens.admin.AdminHomeScreen
import com.example.transandina_app.screens.admin.EstadoDocumento
import com.example.transandina_app.screens.admin.EstadoSemaforo
import com.example.transandina_app.screens.admin.FichaVehicular
import com.example.transandina_app.screens.admin.FleetManagementScreen
import com.example.transandina_app.screens.admin.MaintenanceReportScreen
import com.example.transandina_app.screens.admin.VehicleDetailScreen
import com.example.transandina_app.screens.auth.LoginScreen
import com.example.transandina_app.screens.auth.RegisterScreen
import com.example.transandina_app.ui.theme.TransAndinaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.transandina_app.data.db.DatabaseConfig.disableDriverAssertions()
        enableEdgeToEdge()
        setContent {
            TransAndinaAppTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf("login") }
                var previousScreen by remember { mutableStateOf("admin_home") }
                var selectedFicha by remember { mutableStateOf(FichaVehicular()) }

                val authRepository = remember { AuthRepository() }
                val coroutineScope = rememberCoroutineScope()
                var usuarioLogueado by remember { mutableStateOf<Usuario?>(null) }
                var isLoggingIn by remember { mutableStateOf(false) }
                var loginErrorMessage by remember { mutableStateOf<String?>(null) }
                var isRegistering by remember { mutableStateOf(false) }
                var registerErrorMessage by remember { mutableStateOf<String?>(null) }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            isLoading = isLoggingIn,
                            errorMessage = loginErrorMessage,
                            onLoginClick = { email, password ->
                                isLoggingIn = true
                                loginErrorMessage = null
                                coroutineScope.launch {
                                    val result = authRepository.login(email, password)
                                    result.onSuccess { usuario ->
                                        usuarioLogueado = usuario
                                        isLoggingIn = false
                                        val rolFormateado = usuario.rol.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                        if (usuario.esAdmin) {
                                            Toast.makeText(
                                                context,
                                                "¡Bienvenido(a) ${usuario.nombreCompleto} ($rolFormateado)!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            currentScreen = "admin_home"
                                        } else {
                                            // Conductor o Mecánico: solo mensaje de bienvenida sin redirigir a admin_home
                                            Toast.makeText(
                                                context,
                                                "¡Bienvenido(a) ${usuario.nombreCompleto} ($rolFormateado)!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }.onFailure { error ->
                                        isLoggingIn = false
                                        loginErrorMessage = error.localizedMessage ?: "Error al autenticar."
                                    }
                                }
                            },
                            onNavigateToRegister = {
                                loginErrorMessage = null
                                registerErrorMessage = null
                                currentScreen = "register"
                            },
                            onForgotPasswordClick = {
                                Toast.makeText(
                                    context,
                                    "Recuperación de contraseña por correo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                    "register" -> {
                        RegisterScreen(
                            isLoading = isRegistering,
                            errorMessage = registerErrorMessage,
                            onRegisterClick = { req ->
                                isRegistering = true
                                registerErrorMessage = null
                                coroutineScope.launch {
                                    val result = authRepository.registrarUsuario(req)
                                    result.onSuccess { usuarioCreado ->
                                        isRegistering = false
                                        Toast.makeText(
                                            context,
                                            "¡Usuario ${usuarioCreado.nombreCompleto} registrado exitosamente! Ya puedes iniciar sesión.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        currentScreen = "login"
                                    }.onFailure { error ->
                                        isRegistering = false
                                        registerErrorMessage = error.localizedMessage ?: "Error al registrar el usuario."
                                    }
                                }
                            },
                            onNavigateBack = {
                                registerErrorMessage = null
                                currentScreen = "login"
                            }
                        )
                    }
                    "admin_home" -> {
                        AdminHomeScreen(
                            onNavigateToUsers = {
                                Toast.makeText(context, "Módulo de Usuarios", Toast.LENGTH_SHORT).show()
                            },
                            onNavigateToVehicles = {
                                Toast.makeText(context, "Módulo de Vehículos", Toast.LENGTH_SHORT).show()
                            },
                            onNavigateToRecords = {
                                Toast.makeText(context, "Módulo de Registros", Toast.LENGTH_SHORT).show()
                            },
                            onNavigateToFleet = {
                                currentScreen = "fleet_management"
                            },
                            onNavigateToAlerts = {
                                Toast.makeText(context, "Centro de Alertas y Notificaciones", Toast.LENGTH_SHORT).show()
                            },
                            onLogout = {
                                usuarioLogueado = null
                                loginErrorMessage = null
                                currentScreen = "login"
                            }
                        )
                    }
                    "fleet_management" -> {
                        FleetManagementScreen(
                            onNavigateBack = {
                                currentScreen = "admin_home"
                            },
                            onVehicleDetailClick = { vehiculoItem ->
                                selectedFicha = vehiculoItem.vehiculoOriginal?.toFichaVehicular() ?: FichaVehicular(
                                    placa = vehiculoItem.placa,
                                    marca = vehiculoItem.nombre.substringBefore(" "),
                                    modelo = vehiculoItem.nombre.substringAfter(" "),
                                    estadoSemaforo = vehiculoItem.estado
                                )
                                currentScreen = "vehicle_detail"
                            }
                        )
                    }
                    "vehicle_detail" -> {
                        VehicleDetailScreen(
                            ficha = selectedFicha,
                            onNavigateBack = {
                                currentScreen = "fleet_management"
                            },
                            onNavigateToHistory = {
                                previousScreen = "vehicle_detail"
                                currentScreen = "maintenance_reports"
                            }
                        )
                    }
                    "maintenance_reports" -> {
                        MaintenanceReportScreen(
                            placaInicial = selectedFicha.placa,
                            onNavigateBack = {
                                currentScreen = previousScreen
                            }
                        )
                    }
                }
            }
        }
    }
}