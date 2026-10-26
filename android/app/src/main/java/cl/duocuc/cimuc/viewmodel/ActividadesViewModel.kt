package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.Actividad
import cl.duocuc.cimuc.domain.model.AreaTematica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ActividadesUiState(
    val areas: List<AreaTematica> = emptyList(),
    val actividades: List<Actividad> = emptyList(),
    val selectedAreaId: Int? = null,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)

// P-05: listado de actividades filtrable por área temática (HU-06, HU-07)
class ActividadesViewModel(repository: CimRepository) : ViewModel() {

    private val _selectedAreaId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ActividadesUiState> = combine(
        _selectedAreaId,
        repository.getAreasTematicas(),
        repository.getActividades()
    ) { selectedAreaId, areas, todas ->
        val filtradas = if (selectedAreaId == null) {
            todas
        } else {
            todas.filter { it.areaId == selectedAreaId }
        }
        ActividadesUiState(
            areas = areas,
            actividades = filtradas,
            selectedAreaId = selectedAreaId,
            isLoading = false,
            isEmpty = filtradas.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActividadesUiState(isLoading = true)
    )

    fun onAreaSelected(areaId: Int?) {
        _selectedAreaId.value = if (_selectedAreaId.value == areaId) null else areaId
    }
}
