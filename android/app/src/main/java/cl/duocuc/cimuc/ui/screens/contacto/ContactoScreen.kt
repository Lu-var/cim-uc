package cl.duocuc.cimuc.ui.screens.contacto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
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
    onOrganizacionChange: (String) -> Unit,
    onMotivoChange: (String) -> Unit,
    onMensajeChange: (String) -> Unit,
    onEnviarClick: () -> Unit,
    onRedactarCorreo: () -> Unit
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
                onOrganizacionChange = onOrganizacionChange,
                onMotivoChange = onMotivoChange,
                onMensajeChange = onMensajeChange,
                onEnviarClick = onEnviarClick,
                onRedactarCorreo = onRedactarCorreo
            )

            is ContactoUiState.Enviando -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = "Enviando solicitud…",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }

            is ContactoUiState.Error -> FormularioContacto(
                uiState = uiState.formulario.copy(errorMensaje = uiState.mensaje),
                contentPadding = padding,
                onNombreChange = onNombreChange,
                onCorreoChange = onCorreoChange,
                onOrganizacionChange = onOrganizacionChange,
                onMotivoChange = onMotivoChange,
                onMensajeChange = onMensajeChange,
                onEnviarClick = onEnviarClick,
                onRedactarCorreo = onRedactarCorreo
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
    onOrganizacionChange: (String) -> Unit,
    onMotivoChange: (String) -> Unit,
    onMensajeChange: (String) -> Unit,
    onEnviarClick: () -> Unit,
    onRedactarCorreo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.proyectoReferidoTitulo,
            onValueChange = {},
            readOnly = true,
            label = { Text("Contenido referido") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre") },
            isError = uiState.errorNombre != null,
            supportingText = { ErrorSupportingText(uiState.errorNombre) },
            trailingIcon = { IconEstadoCampo(uiState.errorNombre, uiState.intentoEnviado) },
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
            supportingText = { ErrorSupportingText(uiState.errorCorreo) },
            trailingIcon = { IconEstadoCampo(uiState.errorCorreo, uiState.intentoEnviado) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .semantics { contentDescription = "Campo correo" }
        )

        OutlinedTextField(
            value = uiState.organizacion,
            onValueChange = onOrganizacionChange,
            label = { Text("Organización") },
            isError = uiState.errorOrganizacion != null,
            supportingText = { ErrorSupportingText(uiState.errorOrganizacion) },
            trailingIcon = { IconEstadoCampo(uiState.errorOrganizacion, uiState.intentoEnviado) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        )

        OutlinedTextField(
            value = uiState.motivo,
            onValueChange = onMotivoChange,
            label = { Text("Motivo") },
            isError = uiState.errorMotivo != null,
            supportingText = { ErrorSupportingText(uiState.errorMotivo) },
            trailingIcon = { IconEstadoCampo(uiState.errorMotivo, uiState.intentoEnviado) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        )

        OutlinedTextField(
            value = uiState.mensaje,
            onValueChange = onMensajeChange,
            label = { Text("Mensaje") },
            isError = uiState.errorMensaje != null,
            supportingText = { ErrorSupportingText(uiState.errorMensaje) },
            trailingIcon = { IconEstadoCampo(uiState.errorMensaje, uiState.intentoEnviado) },
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

        OutlinedButton(
            onClick = onRedactarCorreo,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Redactar correo")
        }
    }
}

@Composable
private fun IconEstadoCampo(error: String?, mostrarCorrecto: Boolean) {
    when {
        error != null -> Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error)
        mostrarCorrecto -> Icon(Icons.Filled.CheckCircle, contentDescription = "Campo válido", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorSupportingText(error: String?) {
    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
