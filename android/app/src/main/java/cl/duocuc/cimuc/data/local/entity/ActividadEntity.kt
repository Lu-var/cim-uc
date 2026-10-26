package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actividad",
    foreignKeys = [
        ForeignKey(
            entity = AreaTematicaEntity::class,
            parentColumns = ["id_area"],
            childColumns = ["id_area"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_area"])
    ]
)
data class ActividadEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_actividad")
    val idActividad: Int,
    @ColumnInfo(name = "titulo")
    val titulo: String,
    @ColumnInfo(name = "tipo")
    val tipo: String,
    @ColumnInfo(name = "fecha")
    val fecha: String,
    @ColumnInfo(name = "descripcion")
    val descripcion: String,
    @ColumnInfo(name = "id_area")
    val idArea: Int
)
