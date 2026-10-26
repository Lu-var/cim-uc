package cl.duocuc.cimuc.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.cimuc.CimViewModelFactory
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.ui.components.BottomNavBar
import cl.duocuc.cimuc.ui.screens.actividades.ActividadesScreen
import cl.duocuc.cimuc.ui.screens.confirmacion.ConfirmacionScreen
import cl.duocuc.cimuc.ui.screens.contacto.ContactoScreen
import cl.duocuc.cimuc.ui.screens.detalle.DetalleScreen
import cl.duocuc.cimuc.ui.screens.explorar.ExplorarScreen
import cl.duocuc.cimuc.ui.screens.inicio.InicioScreen
import cl.duocuc.cimuc.ui.screens.relacionados.RelacionadosScreen
import cl.duocuc.cimuc.viewmodel.ActividadesViewModel
import cl.duocuc.cimuc.viewmodel.DetalleViewModel
import cl.duocuc.cimuc.viewmodel.ExplorarViewModel
import cl.duocuc.cimuc.viewmodel.InicioViewModel

// Grafo de navegación declarativo conectando P-01 a P-07 (N1 / issue #1)
@Composable
fun AppNavigation(cimRepository: CimRepository) {
    val navController = rememberNavController()
    val viewModelFactory = CimViewModelFactory(cimRepository)

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            // La barra inferior se muestra solo en P-01, P-02 y P-05 (N2 / issue #2)
            if (currentRoute in Screen.bottomBarRoutes) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Inicio.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inicio.route,
            modifier = Modifier.padding(padding)
        ) {
            // P-01 Inicio
            composable(Screen.Inicio.route) {
                val viewModel: InicioViewModel = viewModel(factory = viewModelFactory)
                InicioScreen(
                    conteos = viewModel.conteos.value,
                    onBuscar = { navController.navigate(Screen.Explorar.route) },
                    onTipoSeleccionado = { navController.navigate(Screen.Explorar.route) },
                    onActividades = {
                        navController.navigate(Screen.Actividades.create()) {
                            popUpTo(Screen.Inicio.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // P-02 Explorar
            composable(Screen.Explorar.route) {
                val viewModel: ExplorarViewModel = viewModel(factory = viewModelFactory)
                ExplorarScreen(
                    uiState = viewModel.uiState.value,
                    onQueryChanged = viewModel::onQueryChanged,
                    onAreaSelected = viewModel::onAreaSelected,
                    onAnioSelected = viewModel::onAnioSelected,
                    onLimpiarFiltros = viewModel::limpiarFiltros,
                    onProyectoClick = { id ->
                        navController.navigate(Screen.Detalle.create(id))
                    }
                )
            }

            // P-03 Detalle de proyecto
            composable(
                route = Screen.Detalle.route,
                arguments = listOf(navArgument("proyectoId") { type = NavType.IntType })
            ) { entry ->
                val proyectoId = entry.arguments?.getInt("proyectoId") ?: 0
                val viewModel: DetalleViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(proyectoId) { viewModel.cargarProyecto(proyectoId) }
                DetalleScreen(
                    uiState = viewModel.uiState.value,
                    onBack = { navController.popBackStack() },
                    onVerRelacionados = {
                        navController.navigate(Screen.Relacionados.create(proyectoId))
                    },
                    onVerActividadesDelArea = { areaId ->
                        navController.navigate(Screen.Actividades.create(areaId)) {
                            popUpTo(Screen.Inicio.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onContactar = {
                        navController.navigate(Screen.Contacto.create(proyectoId))
                    }
                )
            }

            // P-04 Contenidos relacionados
            composable(
                route = Screen.Relacionados.route,
                arguments = listOf(navArgument("proyectoId") { type = NavType.IntType })
            ) { entry ->
                val proyectoId = entry.arguments?.getInt("proyectoId") ?: 0
                val viewModel: DetalleViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(proyectoId) { viewModel.cargarProyecto(proyectoId) }
                RelacionadosScreen(
                    uiState = viewModel.uiState.value,
                    onBack = { navController.popBackStack() },
                    onContactar = {
                        navController.navigate(Screen.Contacto.create(proyectoId))
                    }
                )
            }

            // P-05 Actividades
            composable(
                route = Screen.Actividades.route,
                arguments = listOf(navArgument("areaId") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { entry ->
                val areaId = entry.arguments?.getInt("areaId") ?: -1
                val viewModel: ActividadesViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(areaId) {
                    if (areaId > 0) viewModel.onAreaSelected(areaId)
                }
                ActividadesScreen(
                    uiState = viewModel.uiState.value,
                    onAreaSelected = viewModel::onAreaSelected
                )
            }

            // P-06 Formulario de contacto
            composable(
                route = Screen.Contacto.route,
                arguments = listOf(navArgument("proyectoId") { type = NavType.IntType })
            ) { entry ->
                val proyectoId = entry.arguments?.getInt("proyectoId") ?: 0
                val viewModel: DetalleViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(proyectoId) { viewModel.cargarProyecto(proyectoId) }
                ContactoScreen(
                    proyectoReferidoTitulo = viewModel.uiState.value.proyecto?.titulo.orEmpty(),
                    onBack = { navController.popBackStack() },
                    onEnviar = { _, _, _ ->
                        // El guardado real en Room y la validación llegan con
                        // ContactoViewModel (rama feat/validacion-mvvm, issues #10 y #13)
                        navController.navigate(Screen.Confirmacion.create(0))
                    }
                )
            }

            // P-07 Confirmación
            composable(
                route = Screen.Confirmacion.route,
                arguments = listOf(navArgument("solicitudId") { type = NavType.IntType })
            ) {
                ConfirmacionScreen(
                    onVolverInicio = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Inicio.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
