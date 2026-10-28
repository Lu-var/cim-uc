package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Contenido
import cl.duocuc.cimuc.domain.model.TipoContenido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.Normalizer

data class ExplorarUiState(
    val query: String = "",
    val selectedTipo: TipoContenido? = null,
    val selectedAreaId: Int? = null,
    val selectedAnio: Int? = null,
    val selectedEstado: String? = null,
    val areas: List<AreaTematica> = emptyList(),
    val aniosDisponibles: List<Int> = emptyList(),
    val contenidos: List<Contenido> = emptyList(),
    val totalCoincidencias: Int = 0,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)

private data class FiltroCriterios(
    val query: String,
    val tipo: TipoContenido?,
    val areaId: Int?,
    val anio: Int?,
    val estado: String?
)

class ExplorarViewModel(private val repository: CimRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedTipo = MutableStateFlow<TipoContenido?>(null)
    private val _selectedAreaId = MutableStateFlow<Int?>(null)
    private val _selectedAnio = MutableStateFlow<Int?>(null)
    private val _selectedEstado = MutableStateFlow<String?>(null)
    private var tipoInicialAplicado = false

    private val _filtros = combine(
        _query,
        _selectedTipo,
        _selectedAreaId,
        _selectedAnio,
        _selectedEstado
    ) { query, tipo, areaId, anio, estado ->
        FiltroCriterios(query, tipo, areaId, anio, estado)
    }

    val uiState: StateFlow<ExplorarUiState> = combine(
        _filtros,
        repository.getContenidos(),
        repository.getAreasTematicas()
    ) { filtros, contenidos, areas ->

        val normalizedQuery = removeDiacritics(filtros.query.trim().lowercase())

        val filtrados = contenidos.filter { contenido ->
            val matchesQuery = if (normalizedQuery.isEmpty()) {
                true
            } else {
                listOf(contenido.titulo, contenido.descripcion, contenido.areaNombre, contenido.meta)
                    .any { removeDiacritics(it.lowercase()).contains(normalizedQuery) }
            }

            // Filtros estrictos: si el tipo no tiene el atributo, no pasa el filtro (HU-01, HU-03)
            val matchesTipo = filtros.tipo == null || contenido.tipo == filtros.tipo
            val matchesArea = filtros.areaId == null || contenido.areaId == filtros.areaId
            val matchesAnio = filtros.anio == null || contenido.anio == filtros.anio
            val matchesEstado = filtros.estado == null ||
                contenido.estado.equals(filtros.estado, ignoreCase = true)

            matchesQuery && matchesTipo && matchesArea && matchesAnio && matchesEstado
        }.sortedWith(
            compareBy<Contenido> { it.anio == null }
                .thenByDescending { it.anio ?: 0 }
                .thenBy { it.titulo }
        )

        ExplorarUiState(
            query = filtros.query,
            selectedTipo = filtros.tipo,
            selectedAreaId = filtros.areaId,
            selectedAnio = filtros.anio,
            selectedEstado = filtros.estado,
            areas = areas,
            aniosDisponibles = contenidos.mapNotNull { it.anio }.distinct().sortedDescending(),
            contenidos = filtrados,
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

    // Tipo que llega preseleccionado desde P-01; se aplica una sola vez por ViewModel
    fun aplicarTipoInicial(tipo: TipoContenido?) {
        if (tipoInicialAplicado) return
        tipoInicialAplicado = true
        if (tipo != null) _selectedTipo.value = tipo
    }

    fun onTipoSelected(tipo: TipoContenido?) {
        tipoInicialAplicado = true
        _selectedTipo.value = if (_selectedTipo.value == tipo) null else tipo
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
        tipoInicialAplicado = true
        _query.value = ""
        _selectedTipo.value = null
        _selectedAreaId.value = null
        _selectedAnio.value = null
        _selectedEstado.value = null
    }

    private fun removeDiacritics(input: String): String {
        val nfdNormalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(nfdNormalized, "")
    }
}
