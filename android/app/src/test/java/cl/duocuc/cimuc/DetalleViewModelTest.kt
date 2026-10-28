package cl.duocuc.cimuc

import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.Contenido
import cl.duocuc.cimuc.domain.model.Proyecto
import cl.duocuc.cimuc.domain.model.TipoContenido
import cl.duocuc.cimuc.viewmodel.DetalleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeProyecto = Proyecto(
        id = 1,
        titulo = "Edificio Experimental de 6 Pisos en CLT",
        descripcion = "Estructura de madera contralaminada en zona sísmica",
        anio = 2025,
        estado = "En curso",
        areaId = 1,
        areaNombre = "Construcción en Madera"
    )

    private val fakeContenidos = listOf(
        Contenido(
            tipo = TipoContenido.PROYECTO,
            id = 1,
            titulo = fakeProyecto.titulo,
            descripcion = fakeProyecto.descripcion,
            meta = "En curso",
            anio = 2025,
            estado = "En curso",
            areaId = 1,
            areaNombre = "Construcción en Madera"
        ),
        Contenido(
            tipo = TipoContenido.PUBLICACION,
            id = 10,
            titulo = "Análisis sísmico de muros CLT",
            descripcion = "Resumen sobre ensayos dinámicos en altura",
            meta = "Artículo",
            anio = 2023,
            areaId = 1,
            areaNombre = "Construcción en Madera"
        )
    )

    private val fakeRepo = object : CimRepository() {
        override fun getContenidos(): Flow<List<Contenido>> = flowOf(fakeContenidos)

        override fun getProyectoById(id: Int): Flow<Proyecto?> =
            flowOf(fakeProyecto.takeIf { it.id == id })
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
    fun cargarPublicacion_muestraContenidoSinProyecto() = runTest {
        val viewModel = DetalleViewModel(fakeRepo)

        viewModel.cargarContenido(TipoContenido.PUBLICACION, 10)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TipoContenido.PUBLICACION, state.tipo)
        assertEquals("Análisis sísmico de muros CLT", state.contenido?.titulo)
        assertNull(state.proyecto)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun cargarProyecto_muestraContenidoYSusRelaciones() = runTest {
        val viewModel = DetalleViewModel(fakeRepo)

        viewModel.cargarContenido(TipoContenido.PROYECTO, 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TipoContenido.PROYECTO, state.tipo)
        assertNotNull(state.contenido)
        assertEquals(fakeProyecto, state.proyecto)
    }

    @Test
    fun idInexistente_muestraError() = runTest {
        val viewModel = DetalleViewModel(fakeRepo)

        viewModel.cargarContenido(TipoContenido.PUBLICACION, 99)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.contenido)
        assertNotNull(state.error)
        assertFalse(state.isLoading)
    }
}
