package cl.duocuc.cimuc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.data.repository.ContactoRepository
import cl.duocuc.cimuc.viewmodel.ActividadesViewModel
import cl.duocuc.cimuc.viewmodel.ContactoViewModel
import cl.duocuc.cimuc.viewmodel.DetalleViewModel
import cl.duocuc.cimuc.viewmodel.ExplorarViewModel
import cl.duocuc.cimuc.viewmodel.InicioViewModel

// Factory simple para inyectar el repositorio en los ViewModels (MVVM)
class CimViewModelFactory(
    private val cimRepository: CimRepository,
    private val contactoRepository: ContactoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(InicioViewModel::class.java) ->
                InicioViewModel(cimRepository) as T
            modelClass.isAssignableFrom(ExplorarViewModel::class.java) ->
                ExplorarViewModel(cimRepository) as T
            modelClass.isAssignableFrom(DetalleViewModel::class.java) ->
                DetalleViewModel(cimRepository) as T
            modelClass.isAssignableFrom(ActividadesViewModel::class.java) ->
                ActividadesViewModel(cimRepository) as T
            modelClass.isAssignableFrom(ContactoViewModel::class.java) ->
                ContactoViewModel(contactoRepository) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
