package cl.duocuc.cimuc.data.repository

import cl.duocuc.cimuc.data.local.AppDatabase
import cl.duocuc.cimuc.data.local.relation.ActividadConArea
import cl.duocuc.cimuc.data.local.relation.ProyectoConDetalles
import cl.duocuc.cimuc.domain.model.Actividad
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Conteos
import cl.duocuc.cimuc.domain.model.Integrante
import cl.duocuc.cimuc.domain.model.Proyecto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

open class CimRepository(private val database: AppDatabase? = null) {

    open fun getProyectos(
        query: String? = null,
        areaId: Int? = null,
        anio: Int? = null,
        estado: String? = null
    ): Flow<List<Proyecto>> {
        val db = database ?: return emptyFlow()
        val sanitizedQuery = query?.trim()?.ifEmpty { null }
        return db.proyectoDao()
            .searchAndFilterWithDetalles(sanitizedQuery, areaId, anio, estado)
            .map { list -> list.map { it.toDomain() } }
    }

    open fun getProyectoById(id: Int): Flow<Proyecto?> {
        val db = database ?: return emptyFlow()
        return db.proyectoDao().getWithDetallesById(id).map { it?.toDomain() }
    }

    open fun getAreasTematicas(): Flow<List<AreaTematica>> {
        val db = database ?: return emptyFlow()
        return db.areaTematicaDao().getAll().map { list ->
            list.map { AreaTematica(id = it.idArea, nombre = it.nombre, descripcion = it.descripcion) }
        }
    }

    open fun getAniosDisponibles(): Flow<List<Int>> {
        val db = database ?: return emptyFlow()
        return db.proyectoDao().getAniosDisponibles()
    }

    open fun getActividades(): Flow<List<Actividad>> {
        val db = database ?: return emptyFlow()
        return db.actividadDao().getAllWithArea().map { list ->
            list.map { it.toDomain() }
        }
    }

    open fun getIntegrantes(): Flow<List<Integrante>> {
        val db = database ?: return emptyFlow()
        return db.integranteDao().getAll().map { list ->
            list.map {
                Integrante(
                    id = it.idIntegrante,
                    nombre = it.nombre,
                    rol = it.rol,
                    especialidad = it.especialidad,
                    organizacionId = it.idOrganizacion
                )
            }
        }
    }

    // Conteos para las tarjetas de acceso de P-01 (mockup HU-01)
    open fun getConteos(): Flow<Conteos> {
        val db = database ?: return emptyFlow()
        return combine(
            db.proyectoDao().getAll(),
            db.capacidadDao().getAll(),
            db.integranteDao().getAll(),
            db.publicacionDao().getAll(),
            db.actividadDao().getAll()
        ) { proyectos, capacidades, integrantes, publicaciones, actividades ->
            Conteos(
                proyectos = proyectos.size,
                capacidades = capacidades.size,
                integrantes = integrantes.size,
                publicaciones = publicaciones.size,
                actividades = actividades.size
            )
        }
    }

    private fun ProyectoConDetalles.toDomain(): Proyecto {
        return Proyecto(
            id = proyecto.idProyecto,
            titulo = proyecto.titulo,
            descripcion = proyecto.descripcion,
            anio = proyecto.anio,
            estado = proyecto.estado,
            areaId = proyecto.idArea,
            areaNombre = area?.nombre.orEmpty(),
            integrantes = integrantes.map {
                Integrante(
                    id = it.idIntegrante,
                    nombre = it.nombre,
                    rol = it.rol,
                    especialidad = it.especialidad,
                    organizacionId = it.idOrganizacion
                )
            },
            capacidades = capacidades.map { it.nombre },
            publicaciones = publicaciones.map { it.titulo }
        )
    }

    private fun ActividadConArea.toDomain(): Actividad {
        return Actividad(
            id = actividad.idActividad,
            titulo = actividad.titulo,
            tipo = actividad.tipo,
            fecha = actividad.fecha,
            descripcion = actividad.descripcion,
            areaId = actividad.idArea,
            areaNombre = area?.nombre.orEmpty()
        )
    }
}
