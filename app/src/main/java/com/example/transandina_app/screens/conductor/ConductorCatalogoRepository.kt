package com.example.transandina_app.data.repository

import com.example.transandina_app.data.database.*
import com.example.transandina_app.data.model.*

class ConductorCatalogoRepository {
    suspend fun obtenerFlotilla(): Result<List<VehiculoConductor>> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            val filas = mutableListOf<VehiculoConductor>()
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerVehiculosConductor @IdConductor = ?").use { sp ->
                sp.setInt(1, usuario)
                sp.leerResultados { _, rs ->
                    while (rs.next()) filas.add(VehiculoConductor(rs.getString("placa"), rs.getString("nombreVehiculo"), rs.getInt("kilometrajeActual")))
                }
            }
            filas
        }
    }

    suspend fun obtenerCategorias(): Result<List<CategoriaServicioConductor>> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, _ ->
            val filas = mutableListOf<CategoriaServicioConductor>()
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerCategoriasServicio").use { sp ->
                sp.leerResultados { _, rs ->
                    while (rs.next()) filas.add(CategoriaServicioConductor(rs.getInt("idCategoriaServicio"), rs.getString("nombreCategoria"), rs.getString("descripcion")))
                }
            }
            filas
        }
    }
}
