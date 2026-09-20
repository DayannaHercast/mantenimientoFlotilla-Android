package com.example.transandina_app

import com.example.transandina_app.data.model.MecanicoRegistroState
import com.example.transandina_app.screens.mecanico.MecanicoHomeScreen
import com.example.transandina_app.screens.mecanico.RegistroIntervencionMecanicoScreen
import com.example.transandina_app.screens.mecanico.DetalleMantenimientoMecanicoScreen
import com.example.transandina_app.screens.mecanico.ConfirmarRegistroMantenimientoMecanicoScreen
import com.example.transandina_app.screens.mecanico.HistorialIntervencionesMecanicoScreen
import androidx.activity.compose.BackHandler

import android.os.Bundle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.transandina_app.data.database.ConductorSqlClient
import com.example.transandina_app.data.model.VehiculoConductor
import com.example.transandina_app.data.repository.ConductorCatalogoRepository
import com.example.transandina_app.screens.admin.KilometrajeRegistroScreen
import com.example.transandina_app.screens.admin.KilometrajeGraficoScreen
import com.example.transandina_app.screens.admin.RegistroMantenimientoScreen
import com.example.transandina_app.screens.admin.HistorialVehiculoScreen
import com.example.transandina_app.screens.admin.NotificacionesVehiculoScreen
import com.example.transandina_app.screens.admin.ConfirmarRegistroKilometrajeScreen
import com.example.transandina_app.screens.admin.ConfirmarRegistroMantenimientoScreen
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
import com.example.transandina_app.screens.conductor.ConductorHomeScreen
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
                var fichaTecnicaSeleccionada by remember { mutableStateOf(FichaTecnicaVehiculo()) }
                var infoGeneralTemporal by remember { mutableStateOf<VehiculoInfoGeneral?>(null) }

                val conductorCatalogoRepository = remember { ConductorCatalogoRepository() }
                var placaConductor by remember { mutableStateOf("") }
                var vehiculosConductor by remember { mutableStateOf<List<VehiculoConductor>>(emptyList()) }
                var mostrarSelectorGrafica by remember { mutableStateOf(false) }
                var cargandoVehiculosGrafica by remember { mutableStateOf(false) }

                var mecanicoRegistro by remember { mutableStateOf(MecanicoRegistroState()) }
                BackHandler(enabled = currentScreen in setOf(
                    "mecanico_registro", "mecanico_historial", "mecanico_confirmacion"
                )) {
                    mecanicoRegistro = MecanicoRegistroState()
                    currentScreen = "mecanico_home"
                }

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
                                        } else if (usuario.esConductor) {
                                            ConductorSqlClient.iniciarSesion(usuario.idUsuario)
                                            placaConductor = ""
                                            Toast.makeText(
                                                context,
                                                "¡Bienvenido(a) ${usuario.nombreCompleto} ($rolFormateado)!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            currentScreen = "conductor_home"
                                        } else if (usuario.esMecanico) {
                                            ConductorSqlClient.iniciarSesion(usuario.idUsuario)
                                            mecanicoRegistro = MecanicoRegistroState()
                                            Toast.makeText(context, "¡Bienvenido(a) " + usuario.nombreCompleto + "!", Toast.LENGTH_SHORT).show()
                                            currentScreen = "mecanico_home"
                                        } else {
                                            // Rol sin pantalla asignada.
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

                    "conductor_home" -> {
                        ConductorHomeScreen(
                            onNavigateToUsers = {
                                currentScreen = "conductor_kilometraje"
                            },
                            onNavigateToVehicles = {
                                if (!cargandoVehiculosGrafica) {
                                    cargandoVehiculosGrafica = true
                                    val idSolicitante = usuarioLogueado?.idUsuario
                                    coroutineScope.launch {
                                        try {
                                            val result = conductorCatalogoRepository.obtenerFlotilla()
                                            if (currentScreen == "conductor_home" && usuarioLogueado?.idUsuario == idSolicitante) {
                                                result.onSuccess { vehiculos ->
                                                    when (vehiculos.size) {
                                                        0 -> Toast.makeText(context, "No tienes vehículos asignados.", Toast.LENGTH_SHORT).show()
                                                        1 -> {
                                                            placaConductor = vehiculos.first().placa
                                                            currentScreen = "conductor_grafica"
                                                        }
                                                        else -> {
                                                            vehiculosConductor = vehiculos
                                                            mostrarSelectorGrafica = true
                                                        }
                                                    }
                                                }.onFailure { error ->
                                                    Toast.makeText(context, error.localizedMessage ?: "No se pudieron cargar los vehículos.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } finally {
                                            cargandoVehiculosGrafica = false
                                        }
                                    }
                                }
                            },
                            onNavigateToRecords = {
                                currentScreen = "conductor_mantenimiento"
                            },
                            onNavigateToFleet = {
                                currentScreen = "conductor_historial"
                            },
                            onNavigateToAlerts = {
                                currentScreen = "conductor_notificaciones"
                            },
                            onLogout = {
                                ConductorSqlClient.cerrarSesion()
                                placaConductor = ""
                                vehiculosConductor = emptyList()
                                mostrarSelectorGrafica = false
                                usuarioLogueado = null
                                loginErrorMessage = null
                                currentScreen = "login"
                            }
                        )
                    }
                    "conductor_kilometraje" -> {
                        KilometrajeRegistroScreen(
                            onNavigateBack = { currentScreen = "conductor_home" },
                            onRegistroExitoso = { currentScreen = "conductor_confirmar_kilometraje" }
                        )
                    }
                    "conductor_confirmar_kilometraje" -> {
                        ConfirmarRegistroKilometrajeScreen(
                            onFinalizar = { currentScreen = "conductor_home" }
                        )
                    }
                    "conductor_grafica" -> {
                        KilometrajeGraficoScreen(
                            placa = placaConductor,
                            onNavigateBack = { currentScreen = "conductor_home" }
                        )
                    }
                    "conductor_mantenimiento" -> {
                        RegistroMantenimientoScreen(
                            onNavigateBack = { currentScreen = "conductor_home" },
                            onRegistroExitoso = { currentScreen = "conductor_confirmar_mantenimiento" }
                        )
                    }
                    "conductor_confirmar_mantenimiento" -> {
                        ConfirmarRegistroMantenimientoScreen(
                            onFinalizar = { currentScreen = "conductor_home" }
                        )
                    }
                    "conductor_historial" -> {
                        HistorialVehiculoScreen(
                            onNavigateBack = { currentScreen = "conductor_home" }
                        )
                    }
                    "conductor_notificaciones" -> {
                        NotificacionesVehiculoScreen(
                            onNavigateBack = { currentScreen = "conductor_home" }
                        )
                    }

                    "mecanico_home" -> {
                        MecanicoHomeScreen(
                            onNavigateToRegistro = {
                                mecanicoRegistro = MecanicoRegistroState()
                                currentScreen = "mecanico_registro"
                            },
                            onNavigateToHistorial = { currentScreen = "mecanico_historial" },
                            onLogout = {
                                ConductorSqlClient.cerrarSesion()
                                mecanicoRegistro = MecanicoRegistroState()
                                usuarioLogueado = null
                                loginErrorMessage = null
                                currentScreen = "login"
                            }
                        )
                    }
                    "mecanico_registro" -> {
                        RegistroIntervencionMecanicoScreen(
                            estado = mecanicoRegistro,
                            onNavigateBack = {
                                mecanicoRegistro = MecanicoRegistroState()
                                currentScreen = "mecanico_home"
                            },
                            onSiguiente = {
                                mecanicoRegistro = it
                                currentScreen = "mecanico_detalle"
                            }
                        )
                    }
                    "mecanico_detalle" -> {
                        DetalleMantenimientoMecanicoScreen(
                            estado = mecanicoRegistro,
                            onNavigateBack = {
                                mecanicoRegistro = it
                                currentScreen = "mecanico_registro"
                            },
                            onRegistroExitoso = {
                                mecanicoRegistro = MecanicoRegistroState()
                                currentScreen = "mecanico_confirmacion"
                            }
                        )
                    }
                    "mecanico_confirmacion" -> {
                        ConfirmarRegistroMantenimientoMecanicoScreen(
                            onFinalizar = { currentScreen = "mecanico_home" }
                        )
                    }
                    "mecanico_historial" -> {
                        HistorialIntervencionesMecanicoScreen(
                            onNavigateBack = { currentScreen = "mecanico_home" }
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
                            ficha = fichaTecnicaSeleccionada,
                            onReasignarConductor = {
                                currentScreen = "admin_reasignar_conductor"
                            },
                            onEditarInformacionVehicular = {
                                currentScreen = "admin_editar_vehiculo_info"
                            },
                            onVolver = {
                                currentScreen = "gestion_flotilla"
                            }
                        )
                    }

                    "admin_reasignar_conductor" -> {
                        AdminReasignarConductor(
                            conductorActual = fichaTecnicaSeleccionada.conductorActual,
                            onConfirmarReasignacion = { nuevoConductorId ->
                                // TODO: acá va el UPDATE a la base de datos cuando esté conectada
                                currentScreen = "admin_confirmar_reasignacion"
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
                            onVolver = {
                                currentScreen = "admin_editar_vehiculo_info"
                            },
                            onGuardar = { estadoDatos ->
                                // TODO: acá van los dos objetos juntos (infoGeneralTemporal + estadoDatos)
                                // para armar el UPDATE completo a la base de datos
                                currentScreen = "admin_confirmar_edicion_vehicular"
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

                if (mostrarSelectorGrafica && currentScreen == "conductor_home") {
                    AlertDialog(
                        onDismissRequest = { mostrarSelectorGrafica = false },
                        title = { Text("Selecciona el vehículo") },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                vehiculosConductor.forEach { vehiculo ->
                                    TextButton(onClick = {
                                        placaConductor = vehiculo.placa
                                        mostrarSelectorGrafica = false
                                        currentScreen = "conductor_grafica"
                                    }) {
                                        Text(vehiculo.placa + " · " + vehiculo.nombreVehiculo)
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { mostrarSelectorGrafica = false }) { Text("Cancelar") }
                        }
                    )
                }
            }
        }
    }
}
