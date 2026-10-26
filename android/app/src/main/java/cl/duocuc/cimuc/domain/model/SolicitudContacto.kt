package cl.duocuc.cimuc.domain.model

data class SolicitudContacto(
    val id: Int = 0,
    val nombreContacto: String = "",
    val correoFicticio: String = "",
    val organizacion: String = "",
    val motivo: String = "",
    val mensaje: String = "",
    val fecha: String = "",
    val idContenidoReferido: Int = 0,
    val proyectoReferidoTitulo: String = ""
)
