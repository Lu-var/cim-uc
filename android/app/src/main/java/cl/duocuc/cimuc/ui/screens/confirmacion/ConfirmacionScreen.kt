package cl.duocuc.cimuc.ui.screens.confirmacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.domain.model.SolicitudContacto

// P-07 Confirmación de solicitud enviada (HU-10, issue #4)
@Composable
fun ConfirmacionScreen(
    solicitud: SolicitudContacto?,
    onRedactarCorreo: () -> Unit,
    onVolverInicio: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(96.dp)
            )

            Text(
                text = "Solicitud enviada",
                style = MaterialTheme.typography.headlineSmall
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (solicitud == null) {
                        CircularProgressIndicator()
                    } else {
                        Text("Tu solicitud quedó registrada en este dispositivo.", style = MaterialTheme.typography.bodyLarge)
                        Text("Nombre: ${solicitud.nombreContacto}")
                        Text("Correo: ${solicitud.correoFicticio}")
                        Text("Organización: ${solicitud.organizacion}")
                        Text("Motivo: ${solicitud.motivo}")
                        Text("Contenido referido: ${solicitud.proyectoReferidoTitulo}")
                        Text("Mensaje: ${solicitud.mensaje}")
                        Text("Fecha: ${solicitud.fecha}")
                    }
                }
            }

            OutlinedButton(
                onClick = onRedactarCorreo,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Redactar correo")
            }

            Button(
                onClick = onVolverInicio,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Volver a Inicio")
            }
        }
    }
}
