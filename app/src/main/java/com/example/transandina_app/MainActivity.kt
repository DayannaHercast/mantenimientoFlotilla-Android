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

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                Toast.makeText(
                                    context,
                                    "Iniciando sesión con: $email",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                }
            }
        }
    }
}