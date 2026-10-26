package cl.duocuc.cimuc.data.repository

import cl.duocuc.cimuc.data.local.AppDatabase
import cl.duocuc.cimuc.data.local.entity.SolicitudContactoEntity
import cl.duocuc.cimuc.data.local.relation.SolicitudConProyecto
import cl.duocuc.cimuc.domain.model.SolicitudContacto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class ContactoRepository(private val database: AppDatabase? = null) {

    open suspend fun guardarSolicitud(
        nombre: String,
        correo: String,
        mensaje: String,
        idProyectoReferido: Int,
        fecha: String? = null,
        organizacion: String = "",
        motivo: String = ""
    ): Long {
        val db = database ?: return -1L
        val fechaRegistro = fecha ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val entity = SolicitudContactoEntity(
            nombreContacto = nombre.trim(),
            correoFicticio = correo.trim(),
            organizacion = organizacion.trim(),
            motivo = motivo.trim(),
            mensaje = mensaje.trim(),
            fecha = fechaRegistro,
            idContenidoReferido = idProyectoReferido
        )
        return db.solicitudContactoDao().insert(entity)
    }

    open fun getSolicitudById(id: Int): Flow<SolicitudContacto?> {
        val db = database ?: return emptyFlow()
        return db.solicitudContactoDao().getWithProyectoById(id).map { it?.toDomain() }
    }

    open fun getAllSolicitudes(): Flow<List<SolicitudContacto>> {
        val db = database ?: return emptyFlow()
        return db.solicitudContactoDao().getAll().map { list ->
            list.map {
                SolicitudContacto(
                    id = it.idSolicitud,
                    nombreContacto = it.nombreContacto,
                    correoFicticio = it.correoFicticio,
                    organizacion = it.organizacion,
                    motivo = it.motivo,
                    mensaje = it.mensaje,
                    fecha = it.fecha,
                    idContenidoReferido = it.idContenidoReferido
                )
            }
        }
    }

    private fun SolicitudConProyecto.toDomain(): SolicitudContacto {
        return SolicitudContacto(
            id = solicitud.idSolicitud,
            nombreContacto = solicitud.nombreContacto,
            correoFicticio = solicitud.correoFicticio,
            organizacion = solicitud.organizacion,
            motivo = solicitud.motivo,
            mensaje = solicitud.mensaje,
            fecha = solicitud.fecha,
            idContenidoReferido = solicitud.idContenidoReferido,
            proyectoReferidoTitulo = proyectoReferido?.titulo.orEmpty()
        )
    }
}
