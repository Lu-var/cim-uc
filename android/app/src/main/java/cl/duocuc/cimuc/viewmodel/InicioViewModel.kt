package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.Conteos
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Conteos de registros para las tarjetas de acceso de P-01 (HU-01)
class InicioViewModel(repository: CimRepository) : ViewModel() {

    val conteos: StateFlow<Conteos> = repository.getConteos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Conteos()
        )
}
