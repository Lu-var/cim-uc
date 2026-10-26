package cl.duocuc.cimuc.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

// Barra superior común; con retroceso en P-03, P-04 y P-06 (N3 / issue #3)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarCommon(
    title: String,
    onBack: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = androidx.compose.ui.Modifier.semantics {
                            contentDescription = "Volver"
                        }
                    )
                }
            }
        }
    )
}
