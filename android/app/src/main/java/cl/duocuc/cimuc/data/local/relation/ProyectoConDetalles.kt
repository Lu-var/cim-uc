package cl.duocuc.cimuc.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import cl.duocuc.cimuc.data.local.entity.AreaTematicaEntity
import cl.duocuc.cimuc.data.local.entity.CapacidadEntity
import cl.duocuc.cimuc.data.local.entity.IntegranteEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoCapacidadCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoIntegranteCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoPublicacionCrossRef
import cl.duocuc.cimuc.data.local.entity.PublicacionEntity

data class ProyectoConDetalles(
    @Embedded
    val proyecto: ProyectoEntity,

    @Relation(
        parentColumn = "id_area",
        entityColumn = "id_area"
    )
    val area: AreaTematicaEntity?,

    @Relation(
        parentColumn = "id_proyecto",
        entityColumn = "id_integrante",
        associateBy = Junction(ProyectoIntegranteCrossRef::class)
    )
    val integrantes: List<IntegranteEntity> = emptyList(),

    @Relation(
        parentColumn = "id_proyecto",
        entityColumn = "id_capacidad",
        associateBy = Junction(ProyectoCapacidadCrossRef::class)
    )
    val capacidades: List<CapacidadEntity> = emptyList(),

    @Relation(
        parentColumn = "id_proyecto",
        entityColumn = "id_publicacion",
        associateBy = Junction(ProyectoPublicacionCrossRef::class)
    )
    val publicaciones: List<PublicacionEntity> = emptyList()
)
