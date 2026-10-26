package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "integrante",
    foreignKeys = [
        ForeignKey(
            entity = OrganizacionEntity::class,
            parentColumns = ["id_organizacion"],
            childColumns = ["id_organizacion"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_organizacion"])
    ]
)
data class IntegranteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_integrante")
    val idIntegrante: Int,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "rol")
    val rol: String,
    @ColumnInfo(name = "especialidad")
    val especialidad: String,
    @ColumnInfo(name = "id_organizacion")
    val idOrganizacion: Int
)
