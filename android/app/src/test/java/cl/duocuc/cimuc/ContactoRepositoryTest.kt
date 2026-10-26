package cl.duocuc.cimuc

import cl.duocuc.cimuc.data.local.dao.SolicitudContactoDao
import cl.duocuc.cimuc.data.local.entity.ProyectoEntity
import cl.duocuc.cimuc.data.local.entity.SolicitudContactoEntity
import cl.duocuc.cimuc.data.local.relation.SolicitudConProyecto
import cl.duocuc.cimuc.data.repository.ContactoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ContactoRepositoryTest {

    private val fakeDao = object : SolicitudContactoDao {
        private val list = mutableListOf<SolicitudContactoEntity>()
        private var nextId = 1

        override suspend fun insert(solicitud: SolicitudContactoEntity): Long {
            val inserted = solicitud.copy(idSolicitud = nextId++)
            list.add(inserted)
            return inserted.idSolicitud.toLong()
        }

        override fun getById(id: Int): Flow<SolicitudContactoEntity?> {
            return flowOf(list.find { it.idSolicitud == id })
        }

        override fun getWithProyectoById(id: Int): Flow<SolicitudConProyecto?> {
            val item = list.find { it.idSolicitud == id }
            return flowOf(
                item?.let {
                    SolicitudConProyecto(
                        solicitud = it,
                        proyectoReferido = ProyectoEntity(
                            idProyecto = it.idContenidoReferido,
                            titulo = "Edificio Experimental de 6 Pisos",
                            descripcion = "Torre CLT",
                            anio = 2025,
                            estado = "En curso",
                            idArea = 1
                        )
                    )
                }
            )
        }

        override fun getAll(): Flow<List<SolicitudContactoEntity>> {
            return flowOf(list.toList())
        }
    }

    private val fakeRepo = object : ContactoRepository() {
        override suspend fun guardarSolicitud(
            nombre: String,
            correo: String,
            mensaje: String,
            idProyectoReferido: Int,
            fecha: String?,
            organizacion: String,
            motivo: String
        ): Long {
            return fakeDao.insert(
                SolicitudContactoEntity(
                    nombreContacto = nombre,
                    correoFicticio = correo,
                    organizacion = organizacion,
                    motivo = motivo,
                    mensaje = mensaje,
                    fecha = fecha ?: "2026-10-26",
                    idContenidoReferido = idProyectoReferido
                )
            )
        }

        override fun getSolicitudById(id: Int) = kotlinx.coroutines.flow.flow {
            fakeDao.getWithProyectoById(id).collect { rel ->
                emit(
                    rel?.let {
                        cl.duocuc.cimuc.domain.model.SolicitudContacto(
                            id = it.solicitud.idSolicitud,
                            nombreContacto = it.solicitud.nombreContacto,
                            correoFicticio = it.solicitud.correoFicticio,
                            organizacion = it.solicitud.organizacion,
                            motivo = it.solicitud.motivo,
                            mensaje = it.solicitud.mensaje,
                            fecha = it.solicitud.fecha,
                            idContenidoReferido = it.solicitud.idContenidoReferido,
                            proyectoReferidoTitulo = it.proyectoReferido?.titulo.orEmpty()
                        )
                    }
                )
            }
        }
    }

    @Test
    fun guardarSolicitud_persisteYRecuperaResumenConProyecto() = runTest {
        val id = fakeRepo.guardarSolicitud(
            nombre = "Ana Morales",
            correo = "ana.morales@empresa.cl",
            mensaje = "Interés en cotización y asesoría técnica",
            idProyectoReferido = 1,
            fecha = "2026-10-26",
            organizacion = "Universidad",
            motivo = "Cotización"
        )

        assertEquals(1L, id)

        val recuperada = fakeRepo.getSolicitudById(1).first()
        assertNotNull(recuperada)
        assertEquals("Ana Morales", recuperada?.nombreContacto)
        assertEquals("ana.morales@empresa.cl", recuperada?.correoFicticio)
        assertEquals("Universidad", recuperada?.organizacion)
        assertEquals("Cotización", recuperada?.motivo)
        assertEquals(1, recuperada?.idContenidoReferido)
        assertEquals("Edificio Experimental de 6 Pisos", recuperada?.proyectoReferidoTitulo)
    }
}
