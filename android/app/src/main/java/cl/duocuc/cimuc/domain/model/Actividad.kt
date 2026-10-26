package cl.duocuc.cimuc.domain.model

data class Actividad(
    val id: Int = 0,
    val titulo: String = "",
    val tipo: String = "",
    val fecha: String = "",
    val descripcion: String = "",
    val areaId: Int = 0,
    val areaNombre: String = ""
)
