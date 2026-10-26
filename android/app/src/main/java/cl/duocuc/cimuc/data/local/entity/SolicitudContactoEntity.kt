package cl.duocuc.cimuc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "solicitud_contacto",
    foreignKeys = [
        ForeignKey(
            entity = ProyectoEntity::class,
            parentColumns = ["id_proyecto"],
            childColumns = ["id_contenido_referido"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_contenido_referido"])
    ]
)
data class SolicitudContactoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_solicitud")
    val idSolicitud: Int = 0,
    @ColumnInfo(name = "nombre_contacto")
    val nombreContacto: String,
    @ColumnInfo(name = "correo_ficticio")
    val correoFicticio: String,
    @ColumnInfo(name = "mensaje")
    val mensaje: String,
    @ColumnInfo(name = "fecha")
    val fecha: String,
    @ColumnInfo(name = "id_contenido_referido")
    val idContenidoReferido: Int
)
