package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.Contenido
import cl.duocuc.cimuc.domain.model.Proyecto
import cl.duocuc.cimuc.domain.model.TipoContenido
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleUiState(
    val tipo: TipoContenido? = null,
    val contenido: Contenido? = null,
    val proyecto: Proyecto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DetalleViewModel(private val repository: CimRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    private var contenidoJob: Job? = null
    private var proyectoJob: Job? = null

    // P-03 muestra el detalle del tipo abierto; solo el proyecto carga sus relaciones y contacto
    fun cargarContenido(tipo: TipoContenido, id: Int) {
        contenidoJob?.cancel()
        proyectoJob?.cancel()
        _uiState.value = DetalleUiState(tipo = tipo, isLoading = true)

        contenidoJob = viewModelScope.launch {
            repository.getContenidos().collect { contenidos ->
                val contenido = contenidos.firstOrNull { it.tipo == tipo && it.id == id }
                if (contenido == null) {
                    _uiState.value = DetalleUiState(
                        tipo = tipo,
                        isLoading = false,
                        error = "${tipo.etiquetaSingular} no encontrado"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        contenido = contenido,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }

        if (tipo == TipoContenido.PROYECTO) {
            proyectoJob = viewModelScope.launch {
                repository.getProyectoById(id).collect { proyecto ->
                    if (proyecto != null) {
                        _uiState.value = _uiState.value.copy(proyecto = proyecto)
                    }
                }
            }
        }
    }

    fun cargarProyecto(id: Int) = cargarContenido(TipoContenido.PROYECTO, id)
}
