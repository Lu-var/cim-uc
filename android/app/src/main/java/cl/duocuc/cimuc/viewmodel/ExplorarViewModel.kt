package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Proyecto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.Normalizer

data class ExplorarUiState(
    val query: String = "",
    val selectedAreaId: Int? = null,
    val selectedAnio: Int? = null,
    val selectedEstado: String? = null,
    val areas: List<AreaTematica> = emptyList(),
    val aniosDisponibles: List<Int> = emptyList(),
    val proyectos: List<Proyecto> = emptyList(),
    val totalCoincidencias: Int = 0,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)

private data class FiltroCriterios(
    val query: String,
    val areaId: Int?,
    val anio: Int?,
    val estado: String?
)

class ExplorarViewModel(private val repository: CimRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedAreaId = MutableStateFlow<Int?>(null)
    private val _selectedAnio = MutableStateFlow<Int?>(null)
    private val _selectedEstado = MutableStateFlow<String?>(null)

    private val _filtros = combine(
        _query,
        _selectedAreaId,
        _selectedAnio,
        _selectedEstado
    ) { query, areaId, anio, estado ->
        FiltroCriterios(query, areaId, anio, estado)
    }

    val uiState: StateFlow<ExplorarUiState> = combine(
        _filtros,
        repository.getProyectos(),
        repository.getAreasTematicas(),
        repository.getAniosDisponibles()
    ) { filtros, todosProyectos, areas, anios ->

        val normalizedQuery = removeDiacritics(filtros.query.trim().lowercase())

        val filtrados = todosProyectos.filter { proyecto ->
            val matchesQuery = if (normalizedQuery.isEmpty()) {
                true
            } else {
                val tituloNorm = removeDiacritics(proyecto.titulo.lowercase())
                val descNorm = removeDiacritics(proyecto.descripcion.lowercase())
                val areaNorm = removeDiacritics(proyecto.areaNombre.lowercase())
                tituloNorm.contains(normalizedQuery) ||
                        descNorm.contains(normalizedQuery) ||
                        areaNorm.contains(normalizedQuery)
            }

            val matchesArea = filtros.areaId == null || proyecto.areaId == filtros.areaId
            val matchesAnio = filtros.anio == null || proyecto.anio == filtros.anio
            val matchesEstado = filtros.estado == null || proyecto.estado.equals(filtros.estado, ignoreCase = true)

            matchesQuery && matchesArea && matchesAnio && matchesEstado
        }

        ExplorarUiState(
            query = filtros.query,
            selectedAreaId = filtros.areaId,
            selectedAnio = filtros.anio,
            selectedEstado = filtros.estado,
            areas = areas,
            aniosDisponibles = anios,
            proyectos = filtrados,
            totalCoincidencias = filtrados.size,
            isLoading = false,
            isEmpty = filtrados.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExplorarUiState(isLoading = true)
    )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onAreaSelected(areaId: Int?) {
        _selectedAreaId.value = if (_selectedAreaId.value == areaId) null else areaId
    }

    fun onAnioSelected(anio: Int?) {
        _selectedAnio.value = if (_selectedAnio.value == anio) null else anio
    }

    fun onEstadoSelected(estado: String?) {
        _selectedEstado.value = if (_selectedEstado.value.equals(estado, ignoreCase = true)) null else estado
    }

    fun limpiarFiltros() {
        _query.value = ""
        _selectedAreaId.value = null
        _selectedAnio.value = null
        _selectedEstado.value = null
    }

    private fun removeDiacritics(input: String): String {
        val nfdNormalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(nfdNormalized, "")
    }
}
