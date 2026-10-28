package cl.duocuc.cimuc.navigation

import cl.duocuc.cimuc.domain.model.TipoContenido

// Rutas centralizadas de las 7 pantallas (N1 / issue #1)
sealed class Screen(val route: String) {
    data object Inicio : Screen("inicio")
    data object Explorar : Screen("explorar?tipo={tipo}") {
        fun create(tipo: TipoContenido? = null): String =
            tipo?.let { "explorar?tipo=${it.ruta}" } ?: "explorar"
    }
    data object Detalle : Screen("detalle/{tipo}/{contenidoId}") {
        fun create(tipo: TipoContenido, contenidoId: Int) = "detalle/${tipo.ruta}/$contenidoId"
    }
    data object Relacionados : Screen("relacionados/{proyectoId}") {
        fun create(proyectoId: Int) = "relacionados/$proyectoId"
    }
    data object Actividades : Screen("actividades?areaId={areaId}") {
        fun create(areaId: Int = -1) = "actividades?areaId=$areaId"
    }
    data object Contacto : Screen("contacto/{proyectoId}") {
        fun create(proyectoId: Int) = "contacto/$proyectoId"
    }
    data object Confirmacion : Screen("confirmacion/{solicitudId}") {
        fun create(solicitudId: Int) = "confirmacion/$solicitudId"
    }

    companion object {
        val bottomBarRoutes = setOf(Inicio.route, Explorar.route, Actividades.route)
    }
}
