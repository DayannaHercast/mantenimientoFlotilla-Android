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
import com.example.transandina_app.screens.admin.AdminGestionUsuarios
import com.example.transandina_app.screens.admin.GestionConductores
import com.example.transandina_app.screens.admin.GestionMecanicos
import com.example.transandina_app.screens.admin.AdminGestionAsignarVehiculo
import com.example.transandina_app.screens.admin.AdminFichaTecnicaVehiculo
import com.example.transandina_app.screens.admin.AdminReasignarConductor
import com.example.transandina_app.screens.admin.AdminConfirmarReasignacion
import com.example.transandina_app.screens.admin.AdminEditarVehiculoInfo
import com.example.transandina_app.screens.admin.AdminEditarVehiculoEstado
import com.example.transandina_app.screens.admin.VehiculoInfoGeneral
import com.example.transandina_app.screens.admin.AdminConfirmarEdicionVehicular
import com.example.transandina_app.screens.admin.FichaTecnicaVehiculo
import com.example.transandina_app.data.repository.VehiculoRepository
import com.example.transandina_app.screens.admin.Conductor
import com.example.transandina_app.screens.admin.EstadoConductor
import com.example.transandina_app.screens.admin.EstadoVehiculo

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
                val vehiculoRepository = remember { VehiculoRepository() }
                val coroutineScope = rememberCoroutineScope()
                var usuarioLogueado by remember { mutableStateOf<Usuario?>(null) }
                var isLoggingIn by remember { mutableStateOf(false) }
                var loginErrorMessage by remember { mutableStateOf<String?>(null) }
                var isRegistering by remember { mutableStateOf(false) }
                var registerErrorMessage by remember { mutableStateOf<String?>(null) }
                var fichaTecnicaSeleccionada by remember { mutableStateOf(FichaTecnicaVehiculo()) }
                var infoGeneralTemporal by remember { mutableStateOf<VehiculoInfoGeneral?>(null) }

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
                                currentScreen = "admin_gestion_usuarios"
                            },
                            onNavigateToVehicles = {
                                currentScreen = "gestion_flotilla"
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

                    "admin_gestion_usuarios" -> {
                        AdminGestionUsuarios(
                            onNavigateToConductores = {
                                currentScreen = "gestion_conductores"
                            },
                            onNavigateToMecanicos = {
                                currentScreen = "gestion_mecanicos"
                            },
                            onVolver = {
                                currentScreen = "admin_home"
                            }
                        )
                    }

                    "gestion_conductores" -> {
                        GestionConductores(
                            onEstadoChange = { conductorId, nuevoEstado ->
                                //TODO: acá va el UPDATE a la base de datos cuando esté conectada
                                Toast.makeText(context, "Conductor $conductorId → ${nuevoEstado.etiqueta}", Toast.LENGTH_SHORT).show()
                            },
                            onVolver = {
                                currentScreen = "admin_gestion_usuarios"
                            }
                        )
                    }

                    "gestion_mecanicos" -> {
                        GestionMecanicos(
                            onEstadoChange = { mecanicoId, nuevoEstado ->
                                // TODO: acá va el UPDATE a la base de datos cuando esté conectada
                                Toast.makeText(context, "Mecánico $mecanicoId → ${nuevoEstado.etiqueta}", Toast.LENGTH_SHORT).show()
                            },
                            onVolver = {
                                currentScreen = "admin_gestion_usuarios"
                            }
                        )
                    }

                    "gestion_flotilla" -> {
                        AdminGestionAsignarVehiculo(
                            onVerFichaVehicular = { vehiculo ->
                                fichaTecnicaSeleccionada = FichaTecnicaVehiculo(
                                    placa = vehiculo.placa,
                                    marca = vehiculo.marca,
                                    modelo = vehiculo.modelo,
                                    estado = vehiculo.estado.etiqueta,
                                    conductorActual = vehiculo.conductorActual
                                    // año, capacidad, vencimiento y kilometraje quedan con los
                                    // valores por defecto hasta que la base de datos los traiga
                                )
                                currentScreen = "admin_ficha_tecnica_vehiculo"
                            },
                            onVolver = {
                                currentScreen = "admin_home"
                            }
                        )
                    }

                    "admin_ficha_tecnica_vehiculo" -> {
                        AdminFichaTecnicaVehiculo(
                            placa = fichaTecnicaSeleccionada.placa,
                            ficha = fichaTecnicaSeleccionada,
                            onReasignarConductor = { f ->
                                fichaTecnicaSeleccionada = f
                                currentScreen = "admin_reasignar_conductor"
                            },
                            onEditarInformacionVehicular = { f ->
                                fichaTecnicaSeleccionada = f
                                currentScreen = "admin_editar_vehiculo_info"
                            },
                            onVolver = {
                                currentScreen = "gestion_flotilla"
                            }
                        )
                    }

                    "admin_reasignar_conductor" -> {
                        AdminReasignarConductor(
                            placa = fichaTecnicaSeleccionada.placa,
                            conductorActual = fichaTecnicaSeleccionada.conductorActual,
                            onConfirmarReasignacion = { nuevoConductorId ->
                                coroutineScope.launch {
                                    val res = vehiculoRepository.reasignarConductor(
                                        placa = fichaTecnicaSeleccionada.placa,
                                        idNuevoConductor = nuevoConductorId,
                                        idAsignador = usuarioLogueado?.idUsuario ?: 1
                                    )
                                    res.onSuccess {
                                        Toast.makeText(context, "Conductor reasignado exitosamente", Toast.LENGTH_SHORT).show()
                                        currentScreen = "admin_confirmar_reasignacion"
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onCancelar = {
                                currentScreen = "admin_ficha_tecnica_vehiculo"
                            }
                        )
                    }

                    "admin_confirmar_reasignacion" -> {
                        AdminConfirmarReasignacion(
                            onFinalizarYVolver = {
                                currentScreen = "admin_ficha_tecnica_vehiculo"
                            }
                        )
                    }

                    "admin_editar_vehiculo_info" -> {
                        AdminEditarVehiculoInfo(
                            placaInicial = fichaTecnicaSeleccionada.placa,
                            marcaInicial = fichaTecnicaSeleccionada.marca,
                            modeloInicial = fichaTecnicaSeleccionada.modelo,
                            anioInicial = fichaTecnicaSeleccionada.anio,
                            tipoVehiculoInicial = fichaTecnicaSeleccionada.tipoVehiculo,
                            capacidadInicial = fichaTecnicaSeleccionada.capacidad,
                            conductorAsignadoInicial = if (fichaTecnicaSeleccionada.conductorActual.isNotBlank() && !fichaTecnicaSeleccionada.conductorActual.equals("Sin asignar", ignoreCase = true)) {
                                Conductor(0, fichaTecnicaSeleccionada.conductorActual, EstadoConductor.ACTIVO)
                            } else null,
                            onVolver = {
                                currentScreen = "admin_ficha_tecnica_vehiculo"
                            },
                            onContinuar = { datos ->
                                infoGeneralTemporal = datos
                                currentScreen = "admin_editar_vehiculo_estado"
                            }
                        )
                    }
                    "admin_editar_vehiculo_estado" -> {
                        AdminEditarVehiculoEstado(
                            kilometrajeInicial = fichaTecnicaSeleccionada.kilometrajeActual,
                            revisionTecnicaInicial = fichaTecnicaSeleccionada.vencimientoRtv,
                            marchamoInicial = fichaTecnicaSeleccionada.vencimientoMarchamo,
                            seguroInicial = fichaTecnicaSeleccionada.vencimientoSeguro,
                            estadoInicial = if (fichaTecnicaSeleccionada.estado.equals("Inactivo", ignoreCase = true)) EstadoVehiculo.INACTIVO else EstadoVehiculo.ACTIVO,
                            onVolver = {
                                currentScreen = "admin_editar_vehiculo_info"
                            },
                            onGuardar = { estadoDatos ->
                                val info = infoGeneralTemporal
                                if (info != null) {
                                    coroutineScope.launch {
                                        val res = vehiculoRepository.actualizarVehiculo(
                                            placa = fichaTecnicaSeleccionada.placa,
                                            infoGeneral = info,
                                            estadoInicial = estadoDatos,
                                            idAsignador = usuarioLogueado?.idUsuario ?: 1
                                        )
                                        res.onSuccess {
                                            Toast.makeText(context, "Vehículo actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                            currentScreen = "admin_confirmar_edicion_vehicular"
                                        }.onFailure { err ->
                                            Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    currentScreen = "admin_confirmar_edicion_vehicular"
                                }
                            },
                        )
                    }

                    "admin_confirmar_edicion_vehicular" -> {
                        AdminConfirmarEdicionVehicular(
                            onFinalizarYVolver = {
                                currentScreen = "admin_ficha_tecnica_vehiculo"
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