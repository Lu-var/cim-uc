package cl.duocuc.cimuc.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import cl.duocuc.cimuc.data.local.entity.ProyectoEntity
import cl.duocuc.cimuc.data.local.entity.SolicitudContactoEntity

data class SolicitudConProyecto(
    @Embedded
    val solicitud: SolicitudContactoEntity,

    @Relation(
        parentColumn = "id_contenido_referido",
        entityColumn = "id_proyecto"
    )
    val proyectoReferido: ProyectoEntity?
)
