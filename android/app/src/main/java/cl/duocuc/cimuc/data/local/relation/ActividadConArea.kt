package cl.duocuc.cimuc.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import cl.duocuc.cimuc.data.local.entity.ActividadEntity
import cl.duocuc.cimuc.data.local.entity.AreaTematicaEntity

data class ActividadConArea(
    @Embedded
    val actividad: ActividadEntity,

    @Relation(
        parentColumn = "id_area",
        entityColumn = "id_area"
    )
    val area: AreaTematicaEntity?
)
