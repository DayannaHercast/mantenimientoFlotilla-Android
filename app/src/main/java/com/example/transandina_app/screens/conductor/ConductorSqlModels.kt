package com.example.transandina_app.data.model

data class VehiculoConductor(val placa: String, val nombreVehiculo: String, val kilometrajeActual: Int)
data class CategoriaServicioConductor(val idCategoriaServicio: Int, val nombreCategoria: String, val descripcion: String?)
enum class TipoServicioConductor(val label: String) {
    PREVENTIVO("Preventivo"), CORRECTIVO("Correctivo")
}
