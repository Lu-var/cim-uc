package cl.duocuc.cimuc

import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Contenido
import cl.duocuc.cimuc.domain.model.TipoContenido
import cl.duocuc.cimuc.viewmodel.ExplorarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExplorarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeContenidos = listOf(
        Contenido(
            tipo = TipoContenido.PROYECTO,
            id = 1,
            titulo = "Edificio Experimental de 6 Pisos en CLT",
            descripcion = "Estructura de madera contralaminada en zona sísmica",
            meta = "En curso",
            anio = 2025,
            estado = "En curso",
            areaId = 1,
            areaNombre = "Construcción en Madera"
        ),
        Contenido(
            tipo = TipoContenido.PROYECTO,
            id = 2,
            titulo = "Monitoreo Sísmico de Estructuras Masivas",
            descripcion = "Sensores IoT en diafragmas para disipación de energía",
            meta = "Finalizado",
            anio = 2024,
            estado = "Finalizado",
            areaId = 3,
            areaNombre = "Estructuras y Sismicidad"
        ),
        Contenido(
            tipo = TipoContenido.PROYECTO,
            id = 3,
            titulo = "Adhesivos Base Lignina Libre de Formaldehído",
            descripcion = "Resinas biobasadas para tableros OSB",
            meta = "En curso",
            anio = 2024,
            estado = "En curso",
            areaId = 2,
            areaNombre = "Biotecnología y Materiales"
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
        ),
        Contenido(
            tipo = TipoContenido.INTEGRANTE,
            id = 20,
            titulo = "Ana Pérez",
            descripcion = "Especialista en estructuras de madera",
            meta = "Investigadora · PUC"
        ),
        Contenido(
            tipo = TipoContenido.CAPACIDAD,
            id = 30,
            titulo = "Ensayo dinámico de muros",
            descripcion = "Laboratorio de ensayos a escala real",
            meta = "",
            areaId = 1,
            areaNombre = "Construcción en Madera"
        )
    )

    private val fakeAreas = listOf(
        AreaTematica(id = 1, nombre = "Construcción en Madera", descripcion = ""),
        AreaTematica(id = 2, nombre = "Biotecnología y Materiales", descripcion = ""),
        AreaTematica(id = 3, nombre = "Estructuras y Sismicidad", descripcion = "")
    )

    private val fakeRepo = object : CimRepository() {
        override fun getContenidos(): Flow<List<Contenido>> = flowOf(fakeContenidos)

        override fun getAreasTematicas(): Flow<List<AreaTematica>> = flowOf(fakeAreas)
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
    fun busquedaInsensibleAMayusculasYTildes_coincideCorrectamente() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(6, viewModel.uiState.value.totalCoincidencias)

        // Buscar raíz "sismic" sin tilde debe coincidir con "Sísmico", "sísmica" y "sísmico"
        viewModel.onQueryChanged("sismic")
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.totalCoincidencias)
        assertTrue(viewModel.uiState.value.contenidos.any { it.id == 1 }) // descripción "zona sísmica"
        assertTrue(viewModel.uiState.value.contenidos.any { it.id == 2 }) // título "Sísmico"
        assertTrue(viewModel.uiState.value.contenidos.any { it.id == 10 }) // título "sísmico"

        // Buscar con mayúsculas y tilde "CLT"
        viewModel.onQueryChanged("clt")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.totalCoincidencias)
        assertTrue(viewModel.uiState.value.contenidos.any { it.id == 1 })
        assertTrue(viewModel.uiState.value.contenidos.any { it.id == 10 })

        job.cancel()
    }

    @Test
    fun filtrosCombinadosPorAreaYAnio_filtranCorrectamente() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        // Filtrar por año 2024
        viewModel.onAnioSelected(2024)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.totalCoincidencias)

        // Añadir filtro por área 2 (Biotecnología)
        viewModel.onAreaSelected(2)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.totalCoincidencias)
        assertEquals(3, viewModel.uiState.value.contenidos.first().id)

        job.cancel()
    }

    @Test
    fun filtroPorTipo_reduceListaYAniosDisponibles() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertEquals(listOf(2025, 2024, 2023), viewModel.uiState.value.aniosDisponibles)

        viewModel.onTipoSelected(TipoContenido.PUBLICACION)
        advanceUntilIdle()

        assertEquals(TipoContenido.PUBLICACION, viewModel.uiState.value.selectedTipo)
        assertEquals(1, viewModel.uiState.value.totalCoincidencias)
        assertEquals("Análisis sísmico de muros CLT", viewModel.uiState.value.contenidos.first().titulo)

        job.cancel()
    }

    @Test
    fun tipoInicialDesdeInicio_seAplicaUnaSolaVez() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.aplicarTipoInicial(TipoContenido.PUBLICACION)
        advanceUntilIdle()
        assertEquals(TipoContenido.PUBLICACION, viewModel.uiState.value.selectedTipo)

        // El usuario cambia de tipo a mano: el tipo inicial no debe volver a imponerse
        viewModel.onTipoSelected(TipoContenido.CAPACIDAD)
        viewModel.aplicarTipoInicial(TipoContenido.PUBLICACION)
        advanceUntilIdle()

        assertEquals(TipoContenido.CAPACIDAD, viewModel.uiState.value.selectedTipo)
        assertEquals(1, viewModel.uiState.value.totalCoincidencias)

        job.cancel()
    }

    @Test
    fun filtrosEstrictos_ocultanTiposSinElAtributoFiltrado() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        // Integrante no tiene área: con filtro de área no queda nada
        viewModel.onTipoSelected(TipoContenido.INTEGRANTE)
        viewModel.onAreaSelected(1)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.totalCoincidencias)
        assertTrue(viewModel.uiState.value.isEmpty)

        // Capacidad no tiene año: con filtro de año no queda nada
        viewModel.onTipoSelected(TipoContenido.CAPACIDAD)
        viewModel.onAreaSelected(null)
        viewModel.onAnioSelected(2024)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.totalCoincidencias)

        job.cancel()
    }

    @Test
    fun busquedaEncuentraPublicacionPorSuResumen() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onTipoSelected(TipoContenido.PUBLICACION)
        viewModel.onQueryChanged("dinamicos")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.totalCoincidencias)
        assertEquals(10, viewModel.uiState.value.contenidos.first().id)

        job.cancel()
    }

    @Test
    fun limpiarFiltros_restauraTodosLosContenidos() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onQueryChanged("criterio_inexistente_xyz")
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.totalCoincidencias)
        assertTrue(viewModel.uiState.value.isEmpty)

        viewModel.limpiarFiltros()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEmpty)
        assertEquals(6, viewModel.uiState.value.totalCoincidencias)
        assertEquals(null, viewModel.uiState.value.selectedTipo)

        job.cancel()
    }

    @Test
    fun filtroPorEstado_noReduceLosEstadosDisponibles() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        // El catálogo se deriva del flujo sin filtrar, así que trae los dos estados de los proyectos
        assertEquals(listOf("En curso", "Finalizado"), viewModel.uiState.value.estadosDisponibles)

        viewModel.onEstadoSelected("Finalizado")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.totalCoincidencias)
        // Si el catálogo se derivara de la lista filtrada quedaría solo "Finalizado"
        // y el usuario no podría volver a "En curso" desde los chips
        assertEquals(2, viewModel.uiState.value.estadosDisponibles.size)

        job.cancel()
    }

    @Test
    fun filtroPorEstado_seleccionarElMismoEstadoDosVeces_loDeselecciona() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onEstadoSelected("En curso")
        advanceUntilIdle()
        assertEquals("En curso", viewModel.uiState.value.selectedEstado)
        assertEquals(2, viewModel.uiState.value.totalCoincidencias)

        viewModel.onEstadoSelected("En curso")
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.selectedEstado)
        assertEquals(6, viewModel.uiState.value.totalCoincidencias)

        job.cancel()
    }
}
