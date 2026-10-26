package cl.duocuc.cimuc.ui.screens.actividades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.domain.model.Actividad
import cl.duocuc.cimuc.ui.components.TopAppBarCommon
import cl.duocuc.cimuc.viewmodel.ActividadesUiState

// P-05 Actividades y oportunidades de colaboración, filtrable por área (HU-06, HU-07)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadesScreen(
    uiState: ActividadesUiState,
    onAreaSelected: (Int?) -> Unit
) {
    Scaffold(
        topBar = { TopAppBarCommon(title = "Actividades") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Selector de área temática con opción "Todas"
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedAreaId == null,
                        onClick = { onAreaSelected(null) },
                        label = { Text("Todas") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
                items(uiState.areas, key = { it.id }) { area ->
                    FilterChip(
                        selected = area.id == uiState.selectedAreaId,
                        onClick = { onAreaSelected(area.id) },
                        label = { Text(area.nombre) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                uiState.isEmpty -> {
                    Text(
                        text = "Sin actividades en esta área",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.actividades, key = { it.id }) { actividad ->
                            ActividadCard(actividad = actividad)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActividadCard(actividad: Actividad) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(onClick = {}, label = { Text(actividad.tipo) })
            Text(text = actividad.titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                text = actividad.fecha,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = actividad.descripcion, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
