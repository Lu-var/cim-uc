package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.ContactoRepository
import cl.duocuc.cimuc.validation.ContactoValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Estados de UI del formulario de contacto (V3 / issue #13)
sealed interface ContactoUiState {
    data class Formulario(
        val nombre: String = "",
        val correo: String = "",
        val organizacion: String = "",
        val motivo: String = "",
        val mensaje: String = "",
        val proyectoReferidoTitulo: String = "",
        val idProyectoReferido: Int = 0,
        val errorNombre: String? = null,
        val errorCorreo: String? = null,
        val errorOrganizacion: String? = null,
        val errorMotivo: String? = null,
        val errorMensaje: String? = null,
        val intentoEnviado: Boolean = false
    ) : ContactoUiState

    data class Enviando(
        val nombre: String, val correo: String, val organizacion: String,
        val motivo: String, val mensaje: String, val idProyectoReferido: Int
    ) : ContactoUiState

    data class Error(val mensaje: String, val formulario: Formulario) : ContactoUiState

    data class Exito(
        val solicitudId: Long,
        val nombre: String,
        val correo: String,
        val mensaje: String,
        val fecha: String,
        val proyectoReferidoTitulo: String
    ) : ContactoUiState
}

// MVVM unidireccional: la UI delega eventos y observa el UiState (V3 y V4)
class ContactoViewModel(
    private val contactoRepository: ContactoRepository,
    private val validador: ContactoValidator = ContactoValidator()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactoUiState>(ContactoUiState.Formulario())
    val uiState: StateFlow<ContactoUiState> = _uiState.asStateFlow()

    fun cargarProyectoReferido(titulo: String, id: Int? = null) {
        val actual = _uiState.value as? ContactoUiState.Formulario ?: return
        _uiState.value = actual.copy(proyectoReferidoTitulo = titulo, idProyectoReferido = id ?: actual.idProyectoReferido)
    }

    // Los campos son estado controlado: no se limpian al fallar la validación (#9)
    fun onNombreChange(valor: String) {
        actualizarFormulario { it.copy(nombre = valor, errorNombre = null) }
    }

    fun onCorreoChange(valor: String) {
        actualizarFormulario { it.copy(correo = valor, errorCorreo = null) }
    }

    fun onOrganizacionChange(valor: String) {
        actualizarFormulario { it.copy(organizacion = valor, errorOrganizacion = null) }
    }

    fun onMotivoChange(valor: String) {
        actualizarFormulario { it.copy(motivo = valor, errorMotivo = null) }
    }

    fun onMensajeChange(valor: String) {
        actualizarFormulario { it.copy(mensaje = valor, errorMensaje = null) }
    }

    fun onEnviarClick() {
        val formulario = _uiState.value as? ContactoUiState.Formulario ?: return
        val resultado = validador.validarFormulario(
            formulario.nombre, formulario.correo, formulario.mensaje,
            formulario.organizacion, formulario.motivo
        )

        if (!resultado.esValido) {
            _uiState.value = formulario.copy(
                errorNombre = resultado.errorNombre,
                errorCorreo = resultado.errorCorreo,
                errorOrganizacion = resultado.errorOrganizacion,
                errorMotivo = resultado.errorMotivo,
                errorMensaje = resultado.errorMensaje,
                intentoEnviado = true
            )
            return
        }

        _uiState.value = ContactoUiState.Enviando(
            nombre = formulario.nombre,
            correo = formulario.correo,
            organizacion = formulario.organizacion,
            motivo = formulario.motivo,
            mensaje = formulario.mensaje,
            idProyectoReferido = formulario.idProyectoReferido
        )

        viewModelScope.launch {
            try {
                val id = contactoRepository.guardarSolicitud(
                    nombre = formulario.nombre,
                    correo = formulario.correo,
                    mensaje = formulario.mensaje,
                    idProyectoReferido = formulario.idProyectoReferido,
                    organizacion = formulario.organizacion,
                    motivo = formulario.motivo
                )
                if (id > 0) {
                    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    _uiState.value = ContactoUiState.Exito(
                        solicitudId = id,
                        nombre = formulario.nombre,
                        correo = formulario.correo,
                        mensaje = formulario.mensaje,
                        fecha = fecha,
                        proyectoReferidoTitulo = formulario.proyectoReferidoTitulo
                    )
                } else {
                    _uiState.value = ContactoUiState.Error(
                        mensaje = "No se pudo registrar la solicitud",
                        formulario = formulario
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ContactoUiState.Error(
                    mensaje = "Ocurrió un error al enviar la solicitud",
                    formulario = formulario
                )
            }
        }
    }

    // Recupera el formulario tras un error de persistencia (#13 estados Error)
    fun reintentarDesdeError() {
        val errorState = _uiState.value as? ContactoUiState.Error ?: return
        _uiState.value = errorState.formulario
    }

    private fun actualizarFormulario(transform: (ContactoUiState.Formulario) -> ContactoUiState.Formulario) {
        val actual = _uiState.value as? ContactoUiState.Formulario ?: return
        _uiState.value = transform(actual)
    }
}
