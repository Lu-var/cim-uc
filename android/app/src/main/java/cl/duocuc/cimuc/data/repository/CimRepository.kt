package cl.duocuc.cimuc.data.repository

import cl.duocuc.cimuc.data.local.AppDatabase
import cl.duocuc.cimuc.data.local.entity.CapacidadEntity
import cl.duocuc.cimuc.data.local.entity.PublicacionEntity
import cl.duocuc.cimuc.data.local.relation.ActividadConArea
import cl.duocuc.cimuc.data.local.relation.ProyectoConDetalles
import cl.duocuc.cimuc.domain.model.Actividad
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Contenido
import cl.duocuc.cimuc.domain.model.Conteos
import cl.duocuc.cimuc.domain.model.Integrante
import cl.duocuc.cimuc.domain.model.Proyecto
import cl.duocuc.cimuc.domain.model.TipoContenido
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

    // Contenidos de P-02 y P-03: los 4 tipos navegables en un solo modelo (HU-01)
    open fun getContenidos(): Flow<List<Contenido>> {
        val db = database ?: return emptyFlow()
        val areas = getAreasTematicas()
        val organizaciones = db.organizacionDao().getAll()

        val capacidades = combine(db.capacidadDao().getAll(), areas) { lista, areas ->
            lista.map { it.toContenido(areas) }
        }
        val publicaciones = combine(db.publicacionDao().getAll(), areas) { lista, areas ->
            lista.map { it.toContenido(areas) }
        }
        val integrantes = combine(getIntegrantes(), organizaciones) { lista, orgs ->
            lista.map { integrante ->
                val organizacion = orgs.firstOrNull { it.idOrganizacion == integrante.organizacionId }
                integrante.toContenido(organizacion?.nombre)
            }
        }

        return combine(
            getProyectos(),
            capacidades,
            integrantes,
            publicaciones
        ) { proyectos, capacidades, integrantes, publicaciones ->
            proyectos.map { it.toContenido() } + capacidades + integrantes + publicaciones
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

    private fun Proyecto.toContenido(): Contenido {
        return Contenido(
            tipo = TipoContenido.PROYECTO,
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            meta = unirMeta(anio.toString(), estado),
            anio = anio,
            estado = estado,
            areaId = areaId,
            areaNombre = areaNombre
        )
    }

    private fun CapacidadEntity.toContenido(areas: List<AreaTematica>): Contenido {
        val areaNombre = areas.firstOrNull { it.id == idArea }?.nombre.orEmpty()
        return Contenido(
            tipo = TipoContenido.CAPACIDAD,
            id = idCapacidad,
            titulo = nombre,
            descripcion = descripcion,
            meta = areaNombre,
            areaId = idArea,
            areaNombre = areaNombre
        )
    }

    private fun PublicacionEntity.toContenido(areas: List<AreaTematica>): Contenido {
        val areaNombre = areas.firstOrNull { it.id == idArea }?.nombre.orEmpty()
        return Contenido(
            tipo = TipoContenido.PUBLICACION,
            id = idPublicacion,
            titulo = titulo,
            descripcion = resumen,
            meta = unirMeta(anio.toString(), tipo),
            anio = anio,
            areaId = idArea,
            areaNombre = areaNombre
        )
    }

    // Integrante no tiene área ni año propios: su meta es rol y organización
    private fun Integrante.toContenido(organizacionNombre: String?): Contenido {
        return Contenido(
            tipo = TipoContenido.INTEGRANTE,
            id = id,
            titulo = nombre,
            descripcion = especialidad,
            meta = unirMeta(rol, organizacionNombre.orEmpty())
        )
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

private fun unirMeta(vararg partes: String): String =
    partes.filter { it.isNotBlank() }.joinToString(" · ")
