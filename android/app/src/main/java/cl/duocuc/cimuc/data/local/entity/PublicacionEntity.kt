package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "publicacion",
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
data class PublicacionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_publicacion")
    val idPublicacion: Int,
    @ColumnInfo(name = "titulo")
    val titulo: String,
    @ColumnInfo(name = "anio")
    val anio: Int,
    @ColumnInfo(name = "tipo")
    val tipo: String,
    @ColumnInfo(name = "resumen")
    val resumen: String,
    @ColumnInfo(name = "id_area")
    val idArea: Int
)
