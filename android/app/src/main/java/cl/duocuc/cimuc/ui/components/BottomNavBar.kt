package cl.duocuc.cimuc.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import cl.duocuc.cimuc.navigation.Screen

private data class DestinoBarra(
    // destination.route es el patrón, no la ruta rellena: se compara contra este
    val patron: String,
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

private val destinos = listOf(
    DestinoBarra(Screen.Inicio.route, Screen.Inicio.route, "Inicio", Icons.Filled.Home),
    DestinoBarra(Screen.Explorar.route, Screen.Explorar.create(), "Explorar", Icons.Filled.Search),
    DestinoBarra(Screen.Actividades.route, Screen.Actividades.create(), "Actividades", Icons.Filled.Event)
)

// Barra inferior con 3 destinos, visible solo en P-01, P-02 y P-05 (N2 / issue #2)
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        destinos.forEach { destino ->
            NavigationBarItem(
                selected = currentRoute == destino.patron,
                // Tocar la pestaña actual no debe reiniciar su estado (filtros, área)
                onClick = { if (currentRoute != destino.patron) onNavigate(destino.ruta) },
                icon = {
                    Icon(
                        imageVector = destino.icono,
                        contentDescription = destino.etiqueta
                    )
                },
                label = { Text(destino.etiqueta) }
            )
        }
    }
}
