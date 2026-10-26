package cl.duocuc.cimuc.ui.screens.contacto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.ui.components.TopAppBarCommon
import cl.duocuc.cimuc.viewmodel.ContactoUiState

// P-06 desacoplada (V4 / issue #14): componente declarativo que observa el
// UiState y delega eventos mediante lambdas. Cero lógica de validación o
// persistencia en el Composable.
@Composable
fun ContactoScreen(
    uiState: ContactoUiState,
    onBack: () -> Unit,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onMensajeChange: (String) -> Unit,
    onEnviarClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBarCommon(title = "Contactar a CIM UC", onBack = onBack) }
    ) { padding ->
        when (uiState) {
            is ContactoUiState.Formulario -> FormularioContacto(
                uiState = uiState,
                contentPadding = padding,
                onNombreChange = onNombreChange,
                onCorreoChange = onCorreoChange,
                onMensajeChange = onMensajeChange,
                onEnviarClick = onEnviarClick
            )

            is ContactoUiState.Enviando -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Enviando solicitud…",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            is ContactoUiState.Error -> FormularioContacto(
                uiState = uiState.formulario.copy(errorMensaje = uiState.mensaje),
                contentPadding = padding,
                onNombreChange = onNombreChange,
                onCorreoChange = onCorreoChange,
                onMensajeChange = onMensajeChange,
                onEnviarClick = onEnviarClick
            )

            // Tras el éxito la navegación a P-07 la orquesta el NavHost
            is ContactoUiState.Exito -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun FormularioContacto(
    uiState: ContactoUiState.Formulario,
    contentPadding: PaddingValues,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onMensajeChange: (String) -> Unit,
    onEnviarClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta "Contenido referido" de solo lectura (issue #6)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Contenido referido",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.proyectoReferidoTitulo,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre") },
            isError = uiState.errorNombre != null,
            supportingText = {
                uiState.errorNombre?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .semantics { contentDescription = "Campo nombre" }
        )

        OutlinedTextField(
            value = uiState.correo,
            onValueChange = onCorreoChange,
            label = { Text("Correo (ficticio)") },
            isError = uiState.errorCorreo != null,
            supportingText = {
                uiState.errorCorreo?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .semantics { contentDescription = "Campo correo" }
        )

        OutlinedTextField(
            value = uiState.mensaje,
            onValueChange = onMensajeChange,
            label = { Text("Mensaje") },
            isError = uiState.errorMensaje != null,
            supportingText = {
                uiState.errorMensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            minLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Campo mensaje" }
        )

        Button(
            onClick = onEnviarClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Enviar solicitud")
        }
    }
}
