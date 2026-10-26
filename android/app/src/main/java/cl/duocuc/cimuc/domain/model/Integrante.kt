package cl.duocuc.cimuc.domain.model

data class Integrante(
    val id: Int = 0,
    val nombre: String = "",
    val rol: String = "",
    val especialidad: String = "",
    val organizacionId: Int = 0,
    val organizacionNombre: String = ""
)
