package com.example.transandina_app.data.repository

import com.example.transandina_app.data.database.*
import com.example.transandina_app.data.model.*
import java.math.BigDecimal
import java.sql.Types
import java.sql.Date
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reutiliza la sesion, Statement y manejo de errores del conductor. */
class MecanicoRepository {

    /** Pantalla "Registro Mantenimientos": lista de vehiculos para el selector. */
    suspend fun obtenerVehiculosActivos(): Result<List<VehiculoMecanico>> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            val filas = mutableListOf<VehiculoMecanico>()
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerVehiculosActivosMecanico @IdMecanico = ?").use { sp ->
                sp.setInt(1, usuario)
                sp.leerResultados { _, rs ->
                    while (rs.next()) filas.add(
                        VehiculoMecanico(
                            rs.getString("placa"),
                            rs.getString("nombreVehiculo"),
                            rs.getInt("kilometrajeActual")
                        )
                    )
                }
            }
            filas
        }
    }

    /** Pantalla "Registro de mantenimiento": catalogo de categorias (compartido con el conductor). */
    suspend fun obtenerCategorias(): Result<List<CategoriaServicioConductor>> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, _ ->
            val filas = mutableListOf<CategoriaServicioConductor>()
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerCategoriasServicio").use { sp ->
                sp.leerResultados { _, rs ->
                    while (rs.next()) filas.add(
                        CategoriaServicioConductor(
                            rs.getInt("idCategoriaServicio"),
                            rs.getString("nombreCategoria"),
                            rs.getString("descripcion")
                        )
                    )
                }
            }
            filas
        }
    }

    /** Registra la intervencion del paso 2 y la vincula al mecanico autenticado. */
    suspend fun registrarMantenimiento(
        placa: String,
        kilometrajeActual: Int,
        tipoServicio: String,
        categoria: String,
        fecha: String,
        taller: String,
        costo: BigDecimal,
        descripcion: String,
        context: Context,
        comprobantesUris: List<String> = emptyList()
    ): Result<Unit> = resultadoSqlConductor {
        require(placa.isNotBlank() && kilometrajeActual >= 0) { "Revisa la placa y el kilometraje." }
        require(costo.signum() >= 0 && costo.scale() <= 2 && costo <= BigDecimal("9999999999.99")) {
            "El costo debe ser positivo o cero, con máximo dos decimales."
        }
        require(taller.trim().length in 1..150 && descripcion.isNotBlank() && categoria.isNotBlank()) {
            "Revisa el taller, la descripción y la categoría."
        }
        val fechaSql = Date.valueOf(fecha)
        val archivos = withContext(Dispatchers.IO) {
            MantenimientoRegistroRepository().prepararArchivos(context.applicationContext, comprobantesUris)
        }
        ConductorSqlClient.ejecutar { conexion, usuario ->
            conexion.prepararConsultaConductor(
                "EXEC dbo.sp_RegistrarMantenimientoMecanico " +
                    "@Placa = ?, @KilometrajeActual = ?, @TipoServicio = ?, @Categoria = ?, " +
                    "@Fecha = ?, @Taller = ?, @Costo = ?, @Descripcion = ?, @IdMecanico = ?, @ArchivosJson = ?"
            ).use { sp ->
                sp.setString(1, placa.trim())
                sp.setInt(2, kilometrajeActual)
                sp.setString(3, tipoServicio)
                sp.setString(4, categoria)
                sp.setDate(5, fechaSql)
                sp.setString(6, taller.trim())
                sp.setBigDecimal(7, costo)
                sp.setString(8, descripcion.trim())
                sp.setInt(9, usuario)
                sp.setString(10, archivos)
                var id: Int? = null
                var cantidad: Int? = null
                val resultados = sp.leerResultados(timeoutSegundos = 60) { indice, rs ->
                    check(indice == 0 && rs.next()) { "No se recibió confirmación del registro." }
                    id = rs.getInt("idMantenimiento")
                    cantidad = rs.getInt("cantidadComprobantes")
                    check(!rs.next()) { "Se recibió una confirmación inesperada." }
                }
                check(resultados == 1 && (id ?: 0) > 0 && cantidad == comprobantesUris.size) {
                    "No se recibió confirmación completa. Verifica el historial antes de repetir el envío."
                }
            }
        }
    }

    /** Pantalla "Historial": ficha del mecanico autenticado. */
    suspend fun obtenerResumen(): Result<ResumenMecanico?> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            var resumen: ResumenMecanico? = null
            conexion.prepararConsultaConductor("EXEC dbo.sp_ObtenerResumenMecanico @IdMecanico = ?").use { sp ->
                sp.setInt(1, usuario)
                sp.leerResultados { _, rs ->
                    if (rs.next()) resumen = ResumenMecanico(
                        rs.getInt("id"),
                        rs.getString("nombreUsuario"),
                        rs.getString("activoDesde")
                    )
                }
            }
            resumen
        }
    }

    /** Pantalla "Historial": tabla de intervenciones, con filtros opcionales. */
    suspend fun obtenerHistorial(
        servicio: String? = null,
        desde: String? = null,
        hasta: String? = null
    ): Result<List<IntervencionMecanico>> = resultadoSqlConductor {
        ConductorSqlClient.ejecutar { conexion, usuario ->
            val filas = mutableListOf<IntervencionMecanico>()
            conexion.prepararConsultaConductor(
                "EXEC dbo.sp_ObtenerHistorialMecanico @IdMecanico = ?, @Servicio = ?, @Desde = ?, @Hasta = ?, @DesfaseUtcMinutos = ?"
            ).use { sp ->
                sp.setInt(1, usuario)
                if (servicio.isNullOrBlank()) sp.setNull(2, Types.NVARCHAR) else sp.setString(2, servicio)
                if (desde.isNullOrBlank()) sp.setNull(3, Types.DATE) else sp.setDate(3, Date.valueOf(desde))
                if (hasta.isNullOrBlank()) sp.setNull(4, Types.DATE) else sp.setDate(4, Date.valueOf(hasta))
                sp.setInt(5, ConductorSqlClient.desfaseUtcMinutos)
                sp.leerResultados { _, rs ->
                    while (rs.next()) filas.add(
                        IntervencionMecanico(
                            rs.getString("id"),
                            rs.getString("fecha"),
                            rs.getLong("fechaMillis"),
                            rs.getString("placa"),
                            rs.getString("servicio"),
                            rs.getString("taller"),
                            rs.getBigDecimal("costo")
                        )
                    )
                }
            }
            filas
        }
    }
}
