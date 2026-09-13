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
        enableEdgeToEdge()
        setContent {
            TransAndinaAppTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf("login") }
                var previousScreen by remember { mutableStateOf("admin_home") }
                var selectedFicha by remember { mutableStateOf(FichaVehicular()) }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                Toast.makeText(
                                    context,
                                    "Bienvenido Administrador ($email)",
                                    Toast.LENGTH_SHORT
                                ).show()
                                currentScreen = "admin_home"
                            },
                            onNavigateToRegister = {
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
                            onRegisterSuccess = {
                                Toast.makeText(
                                    context,
                                    "¡Cuenta registrada con éxito! Volviendo a inicio...",
                                    Toast.LENGTH_LONG
                                ).show()
                                currentScreen = "login"
                            },
                            onNavigateBack = {
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
                                selectedFicha = FichaVehicular(placa = "Todos los Vehículos")
                                previousScreen = "admin_home"
                                currentScreen = "maintenance_reports"
                            },
                            onNavigateToFleet = {
                                currentScreen = "fleet_management"
                            },
                            onNavigateToAlerts = {
                                Toast.makeText(context, "Centro de Alertas y Notificaciones", Toast.LENGTH_SHORT).show()
                            },
                            onLogout = {
                                currentScreen = "login"
                            }
                        )
                    }
                    "fleet_management" -> {
                        FleetManagementScreen(
                            onNavigateBack = {
                                currentScreen = "admin_home"
                            },
                            onVehicleDetailClick = { vehiculo ->
                                val partes = vehiculo.nombre.split(" ")
                                val marca = partes.getOrElse(0) { "Vehículo" }
                                val modelo = partes.getOrElse(1) { "" }
                                val anio = partes.getOrNull(2)?.toIntOrNull() ?: 2023

                                val (marchamoEstado, rtvEstado, seguroDocEstado) = when (vehiculo.estado) {
                                    EstadoSemaforo.AL_DIA -> Triple(EstadoDocumento.AL_DIA, EstadoDocumento.AL_DIA, EstadoDocumento.AL_DIA)
                                    EstadoSemaforo.PROXIMO -> Triple(EstadoDocumento.AL_DIA, EstadoDocumento.PROXIMO, EstadoDocumento.AL_DIA)
                                    EstadoSemaforo.ATRASADO -> Triple(EstadoDocumento.VENCIDO, EstadoDocumento.VENCIDO, EstadoDocumento.PROXIMO)
                                }

                                selectedFicha = FichaVehicular(
                                    placa = vehiculo.placa,
                                    marca = marca,
                                    modelo = modelo,
                                    anio = anio,
                                    estadoSemaforo = vehiculo.estado,
                                    estadoOperativo = if (vehiculo.estado == EstadoSemaforo.ATRASADO) "En Taller / Inactivo" else "Activo",
                                    conductorActual = "Juan Pérez López",
                                    capacidad = if (marca.contains("Hilux", ignoreCase = true)) "5 personas / 1 Ton" else "5 personas",
                                    tipoVehiculo = if (marca.contains("Hilux", ignoreCase = true)) "Pesado / Pick-Up" else "Liviano / SUV",
                                    marchamoVence = if (marchamoEstado == EstadoDocumento.VENCIDO) "31/12/2025" else "31/12/2026",
                                    marchamoEstado = marchamoEstado,
                                    rtvVence = if (rtvEstado == EstadoDocumento.VENCIDO) "15/08/2026" else "15/10/2026",
                                    rtvEstado = rtvEstado,
                                    seguroEstado = if (seguroDocEstado == EstadoDocumento.PROXIMO) "Póliza por renovar" else "Póliza Activa (INS)",
                                    seguroEstadoDoc = seguroDocEstado,
                                    kilometrajeActual = "42,350 km"
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