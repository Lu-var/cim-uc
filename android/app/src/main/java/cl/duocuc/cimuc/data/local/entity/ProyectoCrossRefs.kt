package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "proyecto_integrante",
    primaryKeys = ["id_proyecto", "id_integrante"],
    foreignKeys = [
        ForeignKey(
            entity = ProyectoEntity::class,
            parentColumns = ["id_proyecto"],
            childColumns = ["id_proyecto"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IntegranteEntity::class,
            parentColumns = ["id_integrante"],
            childColumns = ["id_integrante"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_proyecto"]),
        Index(value = ["id_integrante"])
    ]
)
data class ProyectoIntegranteCrossRef(
    @ColumnInfo(name = "id_proyecto")
    val idProyecto: Int,
    @ColumnInfo(name = "id_integrante")
    val idIntegrante: Int
)

@Entity(
    tableName = "proyecto_capacidad",
    primaryKeys = ["id_proyecto", "id_capacidad"],
    foreignKeys = [
        ForeignKey(
            entity = ProyectoEntity::class,
            parentColumns = ["id_proyecto"],
            childColumns = ["id_proyecto"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CapacidadEntity::class,
            parentColumns = ["id_capacidad"],
            childColumns = ["id_capacidad"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_proyecto"]),
        Index(value = ["id_capacidad"])
    ]
)
data class ProyectoCapacidadCrossRef(
    @ColumnInfo(name = "id_proyecto")
    val idProyecto: Int,
    @ColumnInfo(name = "id_capacidad")
    val idCapacidad: Int
)

@Entity(
    tableName = "proyecto_publicacion",
    primaryKeys = ["id_proyecto", "id_publicacion"],
    foreignKeys = [
        ForeignKey(
            entity = ProyectoEntity::class,
            parentColumns = ["id_proyecto"],
            childColumns = ["id_proyecto"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PublicacionEntity::class,
            parentColumns = ["id_publicacion"],
            childColumns = ["id_publicacion"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_proyecto"]),
        Index(value = ["id_publicacion"])
    ]
)
data class ProyectoPublicacionCrossRef(
    @ColumnInfo(name = "id_proyecto")
    val idProyecto: Int,
    @ColumnInfo(name = "id_publicacion")
    val idPublicacion: Int
)
