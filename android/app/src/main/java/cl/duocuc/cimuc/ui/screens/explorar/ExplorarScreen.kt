package cl.duocuc.cimuc.ui.screens.explorar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.domain.model.TipoContenido
import cl.duocuc.cimuc.ui.components.ContenidoCard
import cl.duocuc.cimuc.ui.components.TopAppBarCommon
import cl.duocuc.cimuc.viewmodel.ExplorarUiState

// P-02 Explorar: búsqueda, filtros combinables y listado por tipo (HU-01, HU-02, HU-03, issue #4)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExplorarScreen(
    uiState: ExplorarUiState,
    onQueryChanged: (String) -> Unit,
    onTipoSelected: (TipoContenido?) -> Unit,
    onAreaSelected: (Int?) -> Unit,
    onAnioSelected: (Int?) -> Unit,
    onLimpiarFiltros: () -> Unit,
    onContenidoClick: (TipoContenido, Int) -> Unit
) {
    Scaffold(
        topBar = { TopAppBarCommon(title = "Explorar") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Campo de búsqueda con botón para borrar el texto
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                placeholder = { Text("Buscar proyectos, capacidades…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Borrar texto")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = "Campo de búsqueda de contenidos" }
            )

            // Selector de tipo de contenido
            Text(
                text = "Tipo",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(TipoContenido.entries, key = { it.ruta }) { tipo ->
                    FilterChip(
                        selected = tipo == uiState.selectedTipo,
                        onClick = { onTipoSelected(tipo) },
                        label = { Text(tipo.etiquetaPlural) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            // Selector de área temática
            Text(
                text = "Área temática",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(uiState.areas, key = { it.id }) { area ->
                    FilterChip(
                        selected = area.id == uiState.selectedAreaId,
                        onClick = { onAreaSelected(area.id) },
                        label = { Text(area.nombre) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            // Selector de año
            Text(
                text = "Año",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(uiState.aniosDisponibles, key = { it }) { anio ->
                    FilterChip(
                        selected = anio == uiState.selectedAnio,
                        onClick = { onAnioSelected(anio) },
                        label = { Text(anio.toString()) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            // Etiquetas de filtros activos con "x" y botón "Limpiar todo"
            val hayFiltros = uiState.selectedTipo != null ||
                uiState.selectedAreaId != null ||
                uiState.selectedAnio != null
            if (hayFiltros || uiState.query.isNotEmpty()) {
                // FlowRow: con tipo + área + año las etiquetas deben envolver, no empujar "Limpiar todo" fuera
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.selectedTipo?.let { tipo ->
                        FiltroActivoChip(etiqueta = tipo.etiquetaPlural) { onTipoSelected(null) }
                    }
                    uiState.areas.find { it.id == uiState.selectedAreaId }?.let { area ->
                        FiltroActivoChip(etiqueta = area.nombre) { onAreaSelected(null) }
                    }
                    uiState.selectedAnio?.let { anio ->
                        FiltroActivoChip(etiqueta = anio.toString()) { onAnioSelected(null) }
                    }
                    TextButton(onClick = onLimpiarFiltros) {
                        Text("Limpiar todo")
                    }
                }
            }

            // Contador de resultados
            Text(
                text = "${uiState.totalCoincidencias} resultados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                uiState.isEmpty -> {
                    // Estado vacío
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Sin resultados",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onLimpiarFiltros) {
                            Text("Limpiar búsqueda")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.contenidos,
                            key = { "${it.tipo.ruta}-${it.id}" }
                        ) { contenido ->
                            ContenidoCard(
                                contenido = contenido,
                                onClick = { onContenidoClick(contenido.tipo, contenido.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroActivoChip(etiqueta: String, onQuitar: () -> Unit) {
    FilterChip(
        selected = true,
        onClick = onQuitar,
        label = { Text(etiqueta) },
        trailingIcon = {
            Icon(Icons.Filled.Close, contentDescription = "Quitar filtro $etiqueta")
        },
        modifier = Modifier.heightIn(min = 48.dp)
    )
}
