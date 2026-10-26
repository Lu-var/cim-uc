package cl.duocuc.cimuc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ChipOption(
    val id: Int,
    val label: String
)

// Grupo de etiquetas de filtro en fila horizontal (RNF-08)
@Composable
fun FilterChipGroup(
    options: List<ChipOption>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(options, key = { it.id }) { option ->
            FilterChip(
                selected = option.id == selectedId,
                onClick = { onSelect(option.id) },
                label = { Text(option.label) },
                modifier = Modifier.heightIn(min = 48.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
