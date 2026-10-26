package cl.duocuc.cimuc

import android.app.Application
import cl.duocuc.cimuc.data.local.AppDatabase
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.data.repository.ContactoRepository

// Punto único de composición de la capa de datos (N1 / arquitectura MVVM)
class CimApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val cimRepository: CimRepository by lazy { CimRepository(database) }
    val contactoRepository: ContactoRepository by lazy { ContactoRepository(database) }
}
