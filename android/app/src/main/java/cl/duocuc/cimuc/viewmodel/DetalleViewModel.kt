package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.Proyecto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleUiState(
    val proyecto: Proyecto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DetalleViewModel(private val repository: CimRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    fun cargarProyecto(id: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getProyectoById(id).collect { proyecto ->
                if (proyecto != null) {
                    _uiState.value = DetalleUiState(proyecto = proyecto, isLoading = false)
                } else {
                    _uiState.value = DetalleUiState(isLoading = false, error = "Proyecto no encontrado")
                }
            }
        }
    }
}
