package cl.duocuc.cimuc.domain.model

data class Proyecto(
    val id: Int = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val anio: Int = 0,
    val estado: String = "",
    val areaId: Int = 0,
    val areaNombre: String = "",
    val integrantes: List<Integrante> = emptyList(),
    val capacidades: List<String> = emptyList(),
    val publicaciones: List<String> = emptyList()
)
