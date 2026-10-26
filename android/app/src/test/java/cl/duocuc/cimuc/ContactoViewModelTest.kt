package cl.duocuc.cimuc

import cl.duocuc.cimuc.data.repository.ContactoRepository
import cl.duocuc.cimuc.viewmodel.ContactoUiState
import cl.duocuc.cimuc.viewmodel.ContactoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private fun repositoryFalsa(registrarOk: Boolean = true) = object : ContactoRepository() {
        override suspend fun guardarSolicitud(
            nombre: String,
            correo: String,
            mensaje: String,
            idProyectoReferido: Int,
            fecha: String?,
            organizacion: String,
            motivo: String
        ): Long {
            return if (registrarOk) 42L else -1L
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun datosSeConservanAlFallarValidacion() = runTest {
        val viewModel = ContactoViewModel(repositoryFalsa())

        viewModel.onNombreChange("An")
        viewModel.onCorreoChange("correo-malo")
        viewModel.onOrganizacionChange("Universidad")
        viewModel.onMotivoChange("Consulta")
        viewModel.onMensajeChange("Hola")
        viewModel.onEnviarClick()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado is ContactoUiState.Formulario)
        val formulario = estado as ContactoUiState.Formulario

        // Los datos escritos permanecen intactos (#9)
        assertEquals("An", formulario.nombre)
        assertEquals("correo-malo", formulario.correo)
        assertEquals("Universidad", formulario.organizacion)
        assertEquals("Consulta", formulario.motivo)
        assertEquals("Hola", formulario.mensaje)

        // Y los errores por campo están marcados (#13 estados)
        assertNotNull(formulario.errorNombre)
        assertNotNull(formulario.errorCorreo)
        assertNotNull(formulario.errorMensaje)
    }

    @Test
    fun envioValido_pasaPorEnviando_yTerminaEnExito() = runTest {
        val viewModel = ContactoViewModel(repositoryFalsa(registrarOk = true))

        viewModel.onNombreChange("Ana Morales")
        viewModel.onCorreoChange("ana@duocuc.cl")
        viewModel.onOrganizacionChange("CIM UC")
        viewModel.onMotivoChange("Asesoría")
        viewModel.onMensajeChange("Me interesa una asesoría técnica completa")
        viewModel.onEnviarClick()

        assertTrue(viewModel.uiState.value is ContactoUiState.Enviando)

        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado is ContactoUiState.Exito)
        val exito = estado as ContactoUiState.Exito
        assertEquals(42L, exito.solicitudId)
        assertEquals("Ana Morales", exito.nombre)
        assertTrue(exito.fecha.isNotEmpty())
    }

    @Test
    fun falloDePersistencia_muestraError_yPermiteReintentar() = runTest {
        val viewModel = ContactoViewModel(repositoryFalsa(registrarOk = false))

        viewModel.onNombreChange("Ana Morales")
        viewModel.onCorreoChange("ana@duocuc.cl")
        viewModel.onOrganizacionChange("CIM UC")
        viewModel.onMotivoChange("Asesoría")
        viewModel.onMensajeChange("Consulta sobre capacidades I+D del centro")
        viewModel.onEnviarClick()
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado is ContactoUiState.Error)

        viewModel.reintentarDesdeError()
        val formulario = viewModel.uiState.value as ContactoUiState.Formulario
        assertEquals("Ana Morales", formulario.nombre)
        assertEquals("ana@duocuc.cl", formulario.correo)
        assertEquals("Consulta sobre capacidades I+D del centro", formulario.mensaje)
    }

    @Test
    fun formularioVacio_noIntentaPersistir() = runTest {
        val viewModel = ContactoViewModel(repositoryFalsa())

        viewModel.onEnviarClick()
        advanceUntilIdle()

        val estado = viewModel.uiState.value as ContactoUiState.Formulario
        assertTrue(estado.intentoEnviado)
        assertEquals("", estado.nombre)
    }
}
