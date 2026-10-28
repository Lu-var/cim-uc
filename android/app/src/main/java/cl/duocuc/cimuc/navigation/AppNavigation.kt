package cl.duocuc.cimuc.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.cimuc.CimViewModelFactory
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.data.repository.ContactoRepository
import cl.duocuc.cimuc.domain.model.Proyecto
import cl.duocuc.cimuc.domain.model.TipoContenido
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

// Grafo de navegación declarativo conectando P-01 a P-07 (N1 / issue #1)
@Composable
fun AppNavigation(cimRepository: CimRepository, contactoRepository: ContactoRepository) {
    val navController = rememberNavController()
    val viewModelFactory = CimViewModelFactory(cimRepository, contactoRepository)
    val context = LocalContext.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
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
            modifier = Modifier.padding(padding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { ancho -> ancho },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { ancho -> -ancho },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { ancho -> -ancho },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { ancho -> ancho },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            // P-01 Inicio
            composable(Screen.Inicio.route) {
                val viewModel: InicioViewModel = viewModel(factory = viewModelFactory)
                val conteos by viewModel.conteos.collectAsState()
                InicioScreen(
                    conteos = conteos,
                    onBuscar = {
                        navController.navigate(Screen.Explorar.create()) { abrirDesdeInicio() }
                    },
                    onTipoSeleccionado = { tipo ->
                        navController.navigate(Screen.Explorar.create(tipo)) { abrirDesdeInicio() }
                    },
                    onActividades = {
                        navController.navigate(Screen.Actividades.create()) {
                            popUpTo(Screen.Inicio.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // P-02 Explorar: el tipo llega como argumento desde P-01 (HU-01)
            composable(
                route = Screen.Explorar.route,
                arguments = listOf(
                    navArgument("tipo") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                val tipoInicial = TipoContenido.fromRuta(entry.arguments?.getString("tipo"))
                val viewModel: ExplorarViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(tipoInicial) { viewModel.aplicarTipoInicial(tipoInicial) }
                val uiState by viewModel.uiState.collectAsState()
                ExplorarScreen(
                    uiState = uiState,
                    onQueryChanged = viewModel::onQueryChanged,
                    onTipoSelected = viewModel::onTipoSelected,
                    onAreaSelected = viewModel::onAreaSelected,
                    onAnioSelected = viewModel::onAnioSelected,
                    onEstadoSelected = viewModel::onEstadoSelected,
                    onLimpiarFiltros = viewModel::limpiarFiltros,
                    onContenidoClick = { tipo, id ->
                        navController.navigate(Screen.Detalle.create(tipo, id))
                    }
                )
            }

            // P-03 Detalle del contenido abierto en P-02
            composable(
                route = Screen.Detalle.route,
                arguments = listOf(
                    navArgument("tipo") { type = NavType.StringType },
                    navArgument("contenidoId") { type = NavType.IntType }
                )
            ) { entry ->
                val tipo = TipoContenido.fromRuta(entry.arguments?.getString("tipo"))
                    ?: TipoContenido.PROYECTO
                val contenidoId = entry.arguments?.getInt("contenidoId") ?: 0
                val viewModel: DetalleViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(tipo, contenidoId) { viewModel.cargarContenido(tipo, contenidoId) }
                val uiState by viewModel.uiState.collectAsState()
                DetalleScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onCompartir = {
                        uiState.proyecto?.let { proyecto -> compartirProyecto(context, proyecto) }
                    },
                    onRedactarCorreo = {
                        uiState.proyecto?.let { proyecto ->
                            redactarCorreo(
                                context = context,
                                asunto = "Consulta sobre ${proyecto.titulo}",
                                mensaje = "Hola, quisiera recibir más información sobre " +
                                    "${proyecto.titulo}."
                            )
                        }
                    },
                    onVerRelacionados = {
                        navController.navigate(Screen.Relacionados.create(contenidoId))
                    },
                    onVerActividadesDelArea = { areaId ->
                        navController.navigate(Screen.Actividades.create(areaId)) {
                            popUpTo(Screen.Inicio.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onContactar = {
                        navController.navigate(Screen.Contacto.create(contenidoId))
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
                    onEnviarClick = contactoViewModel::onEnviarClick,
                    onRedactarCorreo = {
                        val formulario = when (val estado = contactoState) {
                            is ContactoUiState.Formulario -> estado
                            is ContactoUiState.Error -> estado.formulario
                            else -> null
                        }
                        if (formulario != null) {
                            redactarCorreo(
                                context = context,
                                asunto = "Consulta de contacto a CIM UC",
                                mensaje = buildString {
                                    appendLine("Nombre: ${formulario.nombre}")
                                    appendLine("Correo: ${formulario.correo}")
                                    appendLine("Organización: ${formulario.organizacion}")
                                    appendLine("Motivo: ${formulario.motivo}")
                                    appendLine("Contenido referido: ${formulario.proyectoReferidoTitulo}")
                                    appendLine("Mensaje: ${formulario.mensaje}")
                                }
                            )
                        }
                    }
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
                    onRedactarCorreo = {
                        redactarCorreo(
                            context = context,
                            asunto = "Seguimiento de solicitud a CIM UC",
                            mensaje = solicitud?.let {
                                "Hola, quisiera consultar por mi solicitud ${it.id}, " +
                                    "relacionada con ${it.proyectoReferidoTitulo}."
                            }.orEmpty()
                        )
                    },
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

// Abrir Explorar desde Inicio deja una sola entrada en el back stack (issue #5)
private fun NavOptionsBuilder.abrirDesdeInicio() {
    launchSingleTop = true
    popUpTo(Screen.Inicio.route)
}

// Destinatario ficticio: docs/01 exige datos ficticios y prohíbe usar datos
// reales de CIM UC. Si se reemplaza, que siga siendo una dirección de ejemplo.
private const val DESTINATARIO_INSTITUCIONAL = "contacto@cimuc.example"

private fun compartirProyecto(context: Context, proyecto: Proyecto) {
    val contenido = buildString {
        append(proyecto.titulo)
        if (proyecto.areaNombre.isNotBlank()) append("\n\nÁrea: ${proyecto.areaNombre}")
        if (proyecto.descripcion.isNotBlank()) append("\n\n${proyecto.descripcion}")
    }
    val intentCompartir = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, contenido)
    }

    try {
        context.startActivity(Intent.createChooser(intentCompartir, "Compartir proyecto CIM UC"))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No hay aplicaciones disponibles para compartir.", Toast.LENGTH_SHORT).show()
    }
}

private fun redactarCorreo(context: Context, asunto: String, mensaje: String) {
    if (DESTINATARIO_INSTITUCIONAL.isBlank()) {
        Toast.makeText(context, "El destinatario institucional aún no está configurado.", Toast.LENGTH_SHORT).show()
        return
    }

    val uriMailto = Uri.parse(
        "mailto:${Uri.encode(DESTINATARIO_INSTITUCIONAL, "@")}" +
            "?subject=${Uri.encode(asunto)}&body=${Uri.encode(mensaje)}"
    )
    val intentCorreo = Intent(Intent.ACTION_SENDTO).apply { data = uriMailto }
    if (intentCorreo.resolveActivity(context.packageManager) == null) {
        Toast.makeText(context, "No hay una aplicación de correo disponible.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        context.startActivity(intentCorreo)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No hay una aplicación de correo disponible.", Toast.LENGTH_SHORT).show()
    }
}
