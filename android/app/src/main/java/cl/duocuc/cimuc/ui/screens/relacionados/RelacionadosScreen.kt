package cl.duocuc.cimuc.ui.screens.relacionados

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.ui.components.TopAppBarCommon
import cl.duocuc.cimuc.viewmodel.DetalleUiState

// P-04 Contenidos relacionados del proyecto (HU-05, issue #4)
@Composable
fun RelacionadosScreen(
    uiState: DetalleUiState,
    onBack: () -> Unit,
    onContactar: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBarCommon(title = "Relacionados", onBack = onBack) },
        bottomBar = {
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
            uiState.proyecto != null -> {
                val proyecto = uiState.proyecto
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = proyecto.titulo,
                        style = MaterialTheme.typography.titleLarge
                    )

                    SeccionRelacionados(titulo = "Integrantes") {
                        if (proyecto.integrantes.isEmpty()) {
                            Text(
                                text = "Sin elementos asociados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        proyecto.integrantes.forEach { integrante ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(integrante.nombre, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        integrante.rol,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    SeccionRelacionados(titulo = "Capacidades I+D") {
                        if (proyecto.capacidades.isEmpty()) {
                            Text(
                                text = "Sin elementos asociados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        proyecto.capacidades.forEach { capacidad ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = capacidad,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    SeccionRelacionados(titulo = "Publicaciones") {
                        if (proyecto.publicaciones.isEmpty()) {
                            Text(
                                text = "Sin elementos asociados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        proyecto.publicaciones.forEach { publicacion ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = publicacion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionRelacionados(
    titulo: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium)
        content()
    }
}
