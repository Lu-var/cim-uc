package cl.duocuc.cimuc.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import cl.duocuc.cimuc.data.repository.ContactoRepository
import cl.duocuc.cimuc.ui.components.BottomNavBar
import cl.duocuc.cimuc.ui.screens.actividades.ActividadesScreen
import cl.duocuc.cimuc.ui.screens.confirmacion.ConfirmacionScreen
import cl.duocuc.cimuc.ui.screens.contacto.ContactoScreen
import cl.duocuc.cimuc.ui.screens.detalle.DetalleScreen
import cl.duocuc.cimuc.ui.screens.explorar.ExplorarScreen
import cl.duocuc.cimuc.ui.screens.inicio.InicioScreen
import cl.duocuc.cimuc.ui.screens.relacionados.RelacionadosScreen
import cl.duocuc.cimuc.viewmodel.ActividadesViewModel
import cl.duocuc.cimuc.viewmodel.ContactoUiState
import cl.duocuc.cimuc.viewmodel.ContactoViewModel
import cl.duocuc.cimuc.viewmodel.DetalleViewModel
import cl.duocuc.cimuc.viewmodel.ExplorarViewModel
import cl.duocuc.cimuc.viewmodel.InicioViewModel
import androidx.compose.runtime.remember

// Grafo de navegación declarativo conectando P-01 a P-07 (N1 / issue #1)
@Composable
fun AppNavigation(cimRepository: CimRepository, contactoRepository: ContactoRepository) {
    val navController = rememberNavController()
    val viewModelFactory = CimViewModelFactory(cimRepository, contactoRepository)

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            // La barra inferior se muestra solo en P-01, P-02 y P-05 (N2 / issue #2)
            if (currentRoute in Screen.bottomBarRoutes) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == Screen.Inicio.route) {
                            // Inicio va directo al home: sin saveState/restoreState,
                            // para no restaurar fotos previas del stack (#bug botón Inicio)
                            if (currentRoute != Screen.Inicio.route) {
                                navController.navigate(route) {
                                    popUpTo(Screen.Inicio.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Inicio.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
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
                val conteos by viewModel.conteos.collectAsState()
                InicioScreen(
                    conteos = conteos,
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
                val uiState by viewModel.uiState.collectAsState()
                ExplorarScreen(
                    uiState = uiState,
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
                val uiState by viewModel.uiState.collectAsState()
                DetalleScreen(
                    uiState = uiState,
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
                val uiState by viewModel.uiState.collectAsState()
                RelacionadosScreen(
                    uiState = uiState,
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
                val uiState by viewModel.uiState.collectAsState()
                ActividadesScreen(
                    uiState = uiState,
                    onAreaSelected = viewModel::onAreaSelected
                )
            }

            // P-06 Formulario de contacto
            composable(
                route = Screen.Contacto.route,
                arguments = listOf(navArgument("proyectoId") { type = NavType.IntType })
            ) { entry ->
                val proyectoId = entry.arguments?.getInt("proyectoId") ?: 0
                val detalleViewModel: DetalleViewModel = viewModel(factory = viewModelFactory)
                val contactoViewModel: ContactoViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(proyectoId) { detalleViewModel.cargarProyecto(proyectoId) }

                val contactoState by contactoViewModel.uiState.collectAsState()
                val detalleState by detalleViewModel.uiState.collectAsState()
                val proyectoTitulo = detalleState.proyecto?.titulo.orEmpty()
                LaunchedEffect(proyectoTitulo) {
                    if (proyectoTitulo.isNotEmpty()) {
                        contactoViewModel.cargarProyectoReferido(proyectoTitulo, proyectoId)
                    }
                }

                // Tras el éxito (Exito con solicitudId) se avanza a P-07 (#13)
                LaunchedEffect(contactoState) {
                    val exito = contactoState as? ContactoUiState.Exito ?: return@LaunchedEffect
                    navController.navigate(Screen.Confirmacion.create(exito.solicitudId.toInt())) {
                        popUpTo(Screen.Inicio.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }

                ContactoScreen(
                    uiState = contactoState,
                    onBack = { navController.popBackStack() },
                    onNombreChange = contactoViewModel::onNombreChange,
                    onCorreoChange = contactoViewModel::onCorreoChange,
                    onOrganizacionChange = contactoViewModel::onOrganizacionChange,
                    onMotivoChange = contactoViewModel::onMotivoChange,
                    onMensajeChange = contactoViewModel::onMensajeChange,
                    onEnviarClick = contactoViewModel::onEnviarClick
                )
            }

            // P-07 Confirmación
            composable(
                route = Screen.Confirmacion.route,
                arguments = listOf(navArgument("solicitudId") { type = NavType.IntType })
            ) { entry ->
                val solicitudId = entry.arguments?.getInt("solicitudId") ?: 0
                val solicitud by remember(solicitudId) {
                    contactoRepository.getSolicitudById(solicitudId)
                }.collectAsState(initial = null)
                ConfirmacionScreen(
                    solicitud = solicitud,
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
