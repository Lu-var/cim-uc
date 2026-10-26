package cl.duocuc.cimuc.ui.screens.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.domain.model.Conteos
import cl.duocuc.cimuc.ui.components.TopAppBarCommon

private data class AccesoTipo(
    val etiqueta: String,
    val conteo: Int,
    val icono: ImageVector
)

// P-01 Inicio: acceso directo a cada tipo de contenido (HU-01, issues #4 y #5)
@Composable
fun InicioScreen(
    conteos: Conteos,
    onBuscar: () -> Unit,
    onTipoSeleccionado: (String) -> Unit,
    onActividades: () -> Unit
) {
    val accesos = listOf(
        AccesoTipo("Proyectos", conteos.proyectos, Icons.Filled.Hub),
        AccesoTipo("Capacidades I+D", conteos.capacidades, Icons.Filled.WorkspacePremium),
        AccesoTipo("Integrantes", conteos.integrantes, Icons.Filled.Groups),
        AccesoTipo("Publicaciones", conteos.publicaciones, Icons.Filled.Search),
        AccesoTipo("Actividades", conteos.actividades, Icons.Filled.Event)
    )

    Scaffold(
        topBar = { TopAppBarCommon(title = "CIM UC") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido al Centro UC de Innovación en Madera. Explora proyectos, " +
                        "capacidades I+D, integrantes, publicaciones y actividades del centro.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Campo de búsqueda: al tocarlo abre P-02 con el teclado activo (HU-01)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Buscar proyectos, capacidades…") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = "Campo de búsqueda" },
                enabled = true,
                interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource().also { source ->
                    androidx.compose.runtime.LaunchedEffect(source) {
                        source.interactions.collect { interaction ->
                            if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                onBuscar()
                            }
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            accesos.forEach { acceso ->
                Card(
                    onClick = {
                        if (acceso.etiqueta == "Actividades") onActividades()
                        else onTipoSeleccionado(acceso.etiqueta)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = acceso.icono,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = acceso.etiqueta,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = acceso.conteo.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
