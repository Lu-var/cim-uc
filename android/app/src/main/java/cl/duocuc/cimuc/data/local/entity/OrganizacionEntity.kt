package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizacion")
data class OrganizacionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_organizacion")
    val idOrganizacion: Int,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "tipo")
    val tipo: String,
    @ColumnInfo(name = "descripcion")
    val descripcion: String
)
