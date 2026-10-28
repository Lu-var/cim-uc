package cl.duocuc.cimuc.domain.model

// Tipos de contenido navegables desde P-01 y P-02 (HU-01)
enum class TipoContenido(
    val ruta: String,
    val etiquetaPlural: String,
    val etiquetaSingular: String
) {
    PROYECTO("proyecto", "Proyectos", "Proyecto"),
    CAPACIDAD("capacidad", "Capacidades I+D", "Capacidad I+D"),
    INTEGRANTE("integrante", "Integrantes", "Integrante"),
    PUBLICACION("publicacion", "Publicaciones", "Publicación");

    companion object {
        fun fromRuta(ruta: String?): TipoContenido? =
            entries.firstOrNull { it.ruta == ruta }
    }
}

// Fila unificada de P-02 y contenido de P-03 (HU-01)
data class Contenido(
    val tipo: TipoContenido,
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val meta: String,
    val anio: Int? = null,
    val estado: String? = null,
    val areaId: Int? = null,
    val areaNombre: String = ""
)
