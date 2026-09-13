package com.example.transandina_app.data.model

data class Usuario(
    val idUsuario: Int,
    val nombre: String,
    val primerApellido: String,
    val segundoApellido: String,
    val cedula: String,
    val correo: String,
    val telefono: String,
    val numLicencia: String? = null,
    val tipoLicencia: String? = null,
    val contrasena: String,
    val estado: String,
    val rol: String
) {
    val nombreCompleto: String
        get() = "$nombre $primerApellido $segundoApellido".trim()

    val esAdmin: Boolean
        get() = rol.equals("administrador", ignoreCase = true)

    val esConductor: Boolean
        get() = rol.equals("conductor", ignoreCase = true)

    val esMecanico: Boolean
        get() = rol.equals("mecanico", ignoreCase = true)
}
