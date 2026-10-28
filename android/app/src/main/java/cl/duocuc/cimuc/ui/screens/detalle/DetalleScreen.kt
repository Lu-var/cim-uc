package cl.duocuc.cimuc.ui.screens.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.domain.model.TipoContenido
import cl.duocuc.cimuc.ui.components.TopAppBarCommon
import cl.duocuc.cimuc.viewmodel.DetalleUiState

// P-03 Detalle: del proyecto (HU-04, HU-06) y de cualquier otro tipo abierto desde P-02 (HU-01)
@Composable
fun DetalleScreen(
    uiState: DetalleUiState,
    onBack: () -> Unit,
    onVerRelacionados: () -> Unit,
    onVerActividadesDelArea: (Int) -> Unit,
    onCompartir: () -> Unit,
    onRedactarCorreo: () -> Unit,
    onContactar: () -> Unit
) {
    val proyecto = uiState.proyecto
    val contenido = uiState.contenido

    Scaffold(
        topBar = {
            TopAppBarCommon(
                title = uiState.tipo?.etiquetaSingular ?: "Contenido",
                onBack = onBack
            )
        },
        bottomBar = {
            // El contacto solo aplica al proyecto (mockups P-03, variante por tipo)
            if (proyecto != null) {
                Button(
                    onClick = onContactar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(min = 48.dp)
                ) {
                    Text("Contactar a CIM UC")
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
            }
            contenido != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = contenido.titulo,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(contenido.tipo.etiquetaSingular) }
                        )
                        contenido.anio?.let { anio ->
                            AssistChip(onClick = {}, label = { Text(anio.toString()) })
                        }
                        contenido.estado?.takeIf { it.isNotBlank() }?.let { estado ->
                            AssistChip(onClick = {}, label = { Text(estado) })
                        }
                        if (contenido.areaNombre.isNotEmpty()) {
                            AssistChip(onClick = {}, label = { Text(contenido.areaNombre) })
                        }
                    }

                    Text(
                        text = contenido.descripcion,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Rol y organización del integrante, o tipo de publicación
                    if (contenido.tipo in setOf(TipoContenido.INTEGRANTE, TipoContenido.PUBLICACION) &&
                        contenido.meta.isNotEmpty()
                    ) {
                        Text(
                            text = contenido.meta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (proyecto != null) {
                        OutlinedButton(
                            onClick = onCompartir,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Compartir proyecto")
                        }

                        OutlinedButton(
                            onClick = onRedactarCorreo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Redactar correo")
                        }

                        OutlinedButton(
                            onClick = onVerRelacionados,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Ver contenidos relacionados")
                        }

                        OutlinedButton(
                            onClick = { onVerActividadesDelArea(proyecto.areaId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Ver actividades de esta área")
                        }
                    }
                }
            }
        }
    }
}
