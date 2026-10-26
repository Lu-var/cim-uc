package cl.duocuc.cimuc.ui.screens.contacto

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.ui.components.TopAppBarCommon

// P-06 Formulario de contacto simulado (HU-08, HU-09, issue #6)
@Composable
fun ContactoScreen(
    proyectoReferidoTitulo: String,
    onBack: () -> Unit,
    onEnviar: (nombre: String, correo: String, mensaje: String) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var mensaje by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBarCommon(title = "Contactar a CIM UC", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta "Contenido referido" de solo lectura
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Contenido referido",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = proyectoReferidoTitulo,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = "Campo nombre" }
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo (ficticio)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = "Campo correo" }
            )

            OutlinedTextField(
                value = mensaje,
                onValueChange = { mensaje = it },
                label = { Text("Mensaje") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo mensaje" }
            )

            Button(
                onClick = { onEnviar(nombre, correo, mensaje) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Enviar solicitud")
            }
        }
    }
}
