package cl.duocuc.cimuc

import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Proyecto
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

    private val fakeProyectos = listOf(
        Proyecto(
            id = 1,
            titulo = "Edificio Experimental de 6 Pisos en CLT",
            descripcion = "Estructura de madera contralaminada en zona sísmica",
            anio = 2025,
            estado = "En curso",
            areaId = 1,
            areaNombre = "Construcción en Madera"
        ),
        Proyecto(
            id = 2,
            titulo = "Monitoreo Sísmico de Estructuras Masivas",
            descripcion = "Sensores IoT en diafragmas para disipación de energía",
            anio = 2024,
            estado = "Finalizado",
            areaId = 3,
            areaNombre = "Estructuras y Sismicidad"
        ),
        Proyecto(
            id = 3,
            titulo = "Adhesivos Base Lignina Libre de Formaldehído",
            descripcion = "Resinas biobasadas para tableros OSB",
            anio = 2024,
            estado = "En curso",
            areaId = 2,
            areaNombre = "Biotecnología y Materiales"
        )
    )

    private val fakeAreas = listOf(
        AreaTematica(id = 1, nombre = "Construcción en Madera", descripcion = ""),
        AreaTematica(id = 2, nombre = "Biotecnología y Materiales", descripcion = ""),
        AreaTematica(id = 3, nombre = "Estructuras y Sismicidad", descripcion = "")
    )

    private val fakeAnios = listOf(2025, 2024)

    private val fakeRepo = object : CimRepository() {
        override fun getProyectos(query: String?, areaId: Int?, anio: Int?, estado: String?): Flow<List<Proyecto>> {
            return flowOf(fakeProyectos)
        }

        override fun getAreasTematicas(): Flow<List<AreaTematica>> {
            return flowOf(fakeAreas)
        }

        override fun getAniosDisponibles(): Flow<List<Int>> {
            return flowOf(fakeAnios)
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
    fun busquedaInsensibleAMayusculasYTildes_coincideCorrectamente() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.totalCoincidencias)

        // Buscar raíz "sismic" sin tilde debe coincidir con "Sísmico" y "sísmica"
        viewModel.onQueryChanged("sismic")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalCoincidencias)
        assertTrue(viewModel.uiState.value.proyectos.any { it.id == 1 }) // contiene "sísmica" en descripción
        assertTrue(viewModel.uiState.value.proyectos.any { it.id == 2 }) // contiene "Sísmico" en título

        // Buscar con mayúsculas y tilde "CLT"
        viewModel.onQueryChanged("clt")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.totalCoincidencias)
        assertEquals(1, viewModel.uiState.value.proyectos.first().id)

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
        assertEquals(3, viewModel.uiState.value.proyectos.first().id)

        job.cancel()
    }

    @Test
    fun busquedaSinResultados_activaEstadoVacio() = runTest {
        val viewModel = ExplorarViewModel(fakeRepo)
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onQueryChanged("criterio_inexistente_xyz")
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.totalCoincidencias)
        assertTrue(viewModel.uiState.value.isEmpty)
        assertTrue(viewModel.uiState.value.proyectos.isEmpty())

        // Limpiar filtros restaura todos los resultados
        viewModel.limpiarFiltros()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEmpty)
        assertEquals(3, viewModel.uiState.value.totalCoincidencias)

        job.cancel()
    }
}
