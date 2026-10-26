package cl.duocuc.cimuc.data.local.seed

import cl.duocuc.cimuc.data.local.entity.ActividadEntity
import cl.duocuc.cimuc.data.local.entity.AreaTematicaEntity
import cl.duocuc.cimuc.data.local.entity.CapacidadEntity
import cl.duocuc.cimuc.data.local.entity.IntegranteEntity
import cl.duocuc.cimuc.data.local.entity.OrganizacionEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoCapacidadCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoIntegranteCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoPublicacionCrossRef
import cl.duocuc.cimuc.data.local.entity.PublicacionEntity

object CimSeedData {

    val areasTematicas = listOf(
        AreaTematicaEntity(
            idArea = 1,
            nombre = "Construcción en Madera",
            descripcion = "Sistemas constructivos industrializados, viviendas sostenibles y desempeño estructural en mediana y gran altura."
        ),
        AreaTematicaEntity(
            idArea = 2,
            nombre = "Biotecnología y Materiales",
            descripcion = "Nuevos bioproductos, modificación química de la madera y nanotecnología aplicada a derivados forestales."
        ),
        AreaTematicaEntity(
            idArea = 3,
            nombre = "Estructuras y Sismicidad",
            descripcion = "Ensayos sísmicos, diseño sismorresistente y cálculo de conexiones avanzadas en estructuras masivas."
        ),
        AreaTematicaEntity(
            idArea = 4,
            nombre = "Sostenibilidad y Ciclo de Vida",
            descripcion = "Huella de carbono, economía circular, eficiencia energética y análisis de ciclo de vida (ACV) en edificación."
        )
    )

    val organizaciones = listOf(
        OrganizacionEntity(
            idOrganizacion = 1,
            nombre = "Pontificia Universidad Católica de Chile",
            tipo = "Universidad",
            descripcion = "Institución de educación superior líder en investigación avanzada e innovación tecnológica."
        ),
        OrganizacionEntity(
            idOrganizacion = 2,
            nombre = "Centro UC de Innovación en Madera",
            tipo = "Centro Tecnológico",
            descripcion = "Centro interdisciplinario dedicado al desarrollo y transferencia en edificación y valor de la madera."
        ),
        OrganizacionEntity(
            idOrganizacion = 3,
            nombre = "Instituto Forestal (INFOR)",
            tipo = "Instituto de Investigación",
            descripcion = "Organismo estatal de investigación y desarrollo tecnológico para el sector forestal chileno."
        ),
        OrganizacionEntity(
            idOrganizacion = 4,
            nombre = "Corma Chile",
            tipo = "Asociación Gremial",
            descripcion = "Gremio de la madera y el bosque sustentable que promueve el desarrollo económico y ambiental."
        )
    )

    // >= 10 Proyectos
    val proyectos = listOf(
        ProyectoEntity(
            idProyecto = 1,
            titulo = "Edificio Experimental de 6 Pisos en CLT",
            descripcion = "Diseño estructural, monitoreo dinámico y análisis de comportamiento higrotérmico en torre de madera contralaminada en zona de alta sismicidad.",
            anio = 2025,
            estado = "En curso",
            idArea = 1
        ),
        ProyectoEntity(
            idProyecto = 2,
            titulo = "Vivienda Social Modular de Alta Eficiencia",
            descripcion = "Prototipo habitacional industrializado con envolvente térmica optimizada y ensamble rápido para programas del MINVU.",
            anio = 2025,
            estado = "En curso",
            idArea = 1
        ),
        ProyectoEntity(
            idProyecto = 3,
            titulo = "Monitoreo Sísmico de Estructuras Masivas",
            descripcion = "Red de sensores IoT embebidos en diafragmas de madera para detección temprana de desplazamientos y disipación de energía.",
            anio = 2024,
            estado = "Finalizado",
            idArea = 3
        ),
        ProyectoEntity(
            idProyecto = 4,
            titulo = "Adhesivos Base Lignina Libre de Formaldehído",
            descripcion = "Formulación de resinas biobasadas a partir de licor negro para la fabricación de tableros OSB y vigas laminadas.",
            anio = 2024,
            estado = "En curso",
            idArea = 2
        ),
        ProyectoEntity(
            idProyecto = 5,
            titulo = "ACV Comparativo Madera vs. Hormigón en Altura",
            descripcion = "Estudio integral de carbono incorporado y ciclo de vida de edificios residenciales en la cuenca de Santiago.",
            anio = 2024,
            estado = "Finalizado",
            idArea = 4
        ),
        ProyectoEntity(
            idProyecto = 6,
            titulo = "Conexiones Dúctiles con Pasadores de Acero",
            descripcion = "Caracterización experimental de nudos viga-columna sometidos a cargas cíclicas severas según norma NCh433.",
            anio = 2023,
            estado = "Finalizado",
            idArea = 3
        ),
        ProyectoEntity(
            idProyecto = 7,
            titulo = "Tratamiento Térmico para Especies Nativas Chilenas",
            descripcion = "Mejora de durabilidad natural y estabilidad dimensional de coigüe y roble mediante pirólisis controlada.",
            anio = 2023,
            estado = "Finalizado",
            idArea = 2
        ),
        ProyectoEntity(
            idProyecto = 8,
            titulo = "Pabellón de Ensaye Acústico de Entrepisos",
            descripcion = "Laboratorio in situ para evaluar aislamiento acústico de impacto y aéreo en losas compuestas madera-hormigón.",
            anio = 2023,
            estado = "Finalizado",
            idArea = 1
        ),
        ProyectoEntity(
            idProyecto = 9,
            titulo = "Calculadora Nacional de Huella de Carbono",
            descripcion = "Plataforma digital para estimación del carbono capturado y emisiones evitadas en proyectos de edificación pública.",
            anio = 2025,
            estado = "En curso",
            idArea = 4
        ),
        ProyectoEntity(
            idProyecto = 10,
            titulo = "Refuerzo Sismo-Resistente de Adobe con Mallas",
            descripcion = "Tecnología de encamisado con marcos de madera y geomallas para preservación de patrimonio histórico en zona central.",
            anio = 2022,
            estado = "Finalizado",
            idArea = 3
        ),
        ProyectoEntity(
            idProyecto = 11,
            titulo = "Nanocelulosa Bacteriana para Embalaje Inteligente",
            descripcion = "Desarrollo de films biodegradables activos con propiedades antioxidantes y barrera al oxígeno para industria hortofrutícola.",
            anio = 2025,
            estado = "En curso",
            idArea = 2
        )
    )

    // >= 10 Integrantes
    val integrantes = listOf(
        IntegranteEntity(
            idIntegrante = 1,
            nombre = "Dra. Francisca Silva",
            rol = "Investigadora Principal",
            especialidad = "Ingeniería Estructural y Sismo-resistencia en CLT",
            idOrganizacion = 2
        ),
        IntegranteEntity(
            idIntegrante = 2,
            nombre = "Dr. Rodrigo González",
            rol = "Director Científico",
            especialidad = "Diseño de Sistemas Constructivos Industrializados",
            idOrganizacion = 1
        ),
        IntegranteEntity(
            idIntegrante = 3,
            nombre = "MSc. Camila Morales",
            rol = "Coordinadora de Proyectos",
            especialidad = "Eficiencia Energética y Confort Ambiental",
            idOrganizacion = 2
        ),
        IntegranteEntity(
            idIntegrante = 4,
            nombre = "Dr. Alejandro Parra",
            rol = "Investigador Senior",
            especialidad = "Química de la Madera y Biopolímeros",
            idOrganizacion = 1
        ),
        IntegranteEntity(
            idIntegrante = 5,
            nombre = "Ing. Valentina Rojas",
            rol = "Ingeniera de Laboratorio",
            especialidad = "Ensayos Destructivos y No Destructivos",
            idOrganizacion = 2
        ),
        IntegranteEntity(
            idIntegrante = 6,
            nombre = "Dr. Matías Castro",
            rol = "Investigador Asociado",
            especialidad = "Dinámica de Estructuras y Ensayos en Mesa Sísmica",
            idOrganizacion = 3
        ),
        IntegranteEntity(
            idIntegrante = 7,
            nombre = "MSc. Daniela Fuentes",
            rol = "Especialista en ACV",
            especialidad = "Evaluación de Sostenibilidad y Huella de Carbono",
            idOrganizacion = 2
        ),
        IntegranteEntity(
            idIntegrante = 8,
            nombre = "Ing. Tomás Bravo",
            rol = "Desarrollador Tecnológico",
            especialidad = "Modelado BIM y Fabricación Digital CNC",
            idOrganizacion = 1
        ),
        IntegranteEntity(
            idIntegrante = 9,
            nombre = "Dra. Loreto Valenzuela",
            rol = "Asesora Científica",
            especialidad = "Seguridad contra Incendios en Madera",
            idOrganizacion = 1
        ),
        IntegranteEntity(
            idIntegrante = 10,
            nombre = "Ing. Sebastián Navarrete",
            rol = "Investigador Asistente",
            especialidad = "Monitoreo Estructural e IoT",
            idOrganizacion = 4
        ),
        IntegranteEntity(
            idIntegrante = 11,
            nombre = "Dra. Paulina Araya",
            rol = "Investigadora Postdoctoral",
            especialidad = "Nanocompuestos Celulósicos",
            idOrganizacion = 1
        )
    )

    // >= 6 Capacidades I+D
    val capacidades = listOf(
        CapacidadEntity(
            idCapacidad = 1,
            nombre = "Mesa Sísmica Biaxial de Alta Frecuencia",
            descripcion = "Simulación en tiempo real de sismos chilenos e internacionales para elementos a escala real hasta 15 toneladas.",
            idArea = 3
        ),
        CapacidadEntity(
            idCapacidad = 2,
            nombre = "Prensa Hidráulica para Elementos Masivos (CLT y Glulam)",
            descripcion = "Capacidad de prensado de hasta 12 metros de longitud con control neumático y térmico programable.",
            idArea = 1
        ),
        CapacidadEntity(
            idCapacidad = 3,
            nombre = "Cámara Climatizada de Envejecimiento Acelerado",
            descripcion = "Ciclos controlados de radiación UV, humedad relativa y temperatura extrema para durabilidad exterior.",
            idArea = 2
        ),
        CapacidadEntity(
            idCapacidad = 4,
            nombre = "Túnel de Ensayos Acústicos según Norma ISO 10140",
            descripcion = "Medición normalizada de ruido de impacto y transmisión aérea en tabiquería y losas.",
            idArea = 1
        ),
        CapacidadEntity(
            idCapacidad = 5,
            nombre = "Espectrometría FTIR y Cromatografía de Líquidos",
            descripcion = "Análisis composicional y cuantitativo de resinas, lignina modificada y nanomateriales.",
            idArea = 2
        ),
        CapacidadEntity(
            idCapacidad = 6,
            nombre = "Laboratorio de Análisis de Ciclo de Vida y Ecoinnovación",
            descripcion = "Software especializado SimaPro y base de datos Ecoinvent adaptada al contexto chileno.",
            idArea = 4
        ),
        CapacidadEntity(
            idCapacidad = 7,
            nombre = "Horno de Resistencia al Fuego de Elementos Verticales",
            descripcion = "Ensayos bajo curva normalizada ISO 834 para certificación F60, F90 y F120 en madera protegida.",
            idArea = 1
        )
    )

    // >= 8 Publicaciones
    val publicaciones = listOf(
        PublicacionEntity(
            idPublicacion = 1,
            titulo = "Respuesta Sísmica No Lineal de Muros de Corte CLT",
            anio = 2024,
            tipo = "Artículo ISI/WoS",
            resumen = "Evaluación numérica y experimental de la disipación energética en hold-downs de acero durante excitaciones de campo cercano.",
            idArea = 3
        ),
        PublicacionEntity(
            idPublicacion = 2,
            titulo = "Manual de Diseño y Construcción en Madera para Chile",
            anio = 2024,
            tipo = "Libro Técnico",
            resumen = "Guía completa de cálculo estructural, física de la construcción y detalles constructivos para profesionales de la ingeniería.",
            idArea = 1
        ),
        PublicacionEntity(
            idPublicacion = 3,
            titulo = "Sustitución de Fenol por Lignina Kraft en Adhesivos Estructurales",
            anio = 2023,
            tipo = "Artículo ISI/WoS",
            resumen = "Optimización de parámetros cinéticos de curado y resistencia al cizalle en probetas de pino radiata laminado.",
            idArea = 2
        ),
        PublicacionEntity(
            idPublicacion = 4,
            titulo = "Balance de Carbono en Viviendas de Madera Industrializada",
            anio = 2024,
            tipo = "Informe Técnico",
            resumen = "Comparación de emisiones desde la extracción de materia prima hasta el fin de vida entre tres tipologías residenciales.",
            idArea = 4
        ),
        PublicacionEntity(
            idPublicacion = 5,
            titulo = "Comportamiento Acústico de Entrepisos Mixtos Madera-Hormigón",
            anio = 2023,
            tipo = "Conference Paper",
            resumen = "Efecto de membranas elastoméricas resilientes en la atenuación del ruido de pasos en frecuencias bajas.",
            idArea = 1
        ),
        PublicacionEntity(
            idPublicacion = 6,
            titulo = "Comportamiento al Fuego de Muros Perimetrales con Aislación Biobasada",
            anio = 2023,
            tipo = "Artículo Scopus",
            resumen = "Evaluación de la tasa de carbonización y estanqueidad térmica en tabiques aislados con fibra de madera y celulosa.",
            idArea = 1
        ),
        PublicacionEntity(
            idPublicacion = 7,
            titulo = "Sensorica Inalámbrica para Detección de Humedad en Fachadas Ventiladas",
            anio = 2025,
            tipo = "Artículo ISI/WoS",
            resumen = "Algoritmo de calibración capacitiva para prevención de biodeterioro en edificaciones costeras del sur de Chile.",
            idArea = 2
        ),
        PublicacionEntity(
            idPublicacion = 8,
            titulo = "Estudio de Mercado y Desafíos Normativos para la Madera en Altura",
            anio = 2022,
            tipo = "Informe de Política Pública",
            resumen = "Recomendaciones estratégicas para modernizar la Ordenanza General de Urbanismo y Construcciones (OGUC).",
            idArea = 4
        ),
        PublicacionEntity(
            idPublicacion = 9,
            titulo = "Fragilidad Sísmica de Estructuras Híbridas Hormigón-Madera",
            anio = 2025,
            tipo = "Artículo ISI/WoS",
            resumen = "Desarrollo de curvas de fragilidad probabilísticas para tipología de zócalo rígido y niveles superiores livianos.",
            idArea = 3
        )
    )

    // >= 6 Actividades
    val actividades = listOf(
        ActividadEntity(
            idActividad = 1,
            titulo = "Seminario Internacional Madera en Altura 2026",
            tipo = "Evento",
            fecha = "2026-11-15",
            descripcion = "Encuentro con referentes internacionales de Canadá, Austria y Japón sobre el futuro de los rascacielos sustentables.",
            idArea = 1
        ),
        ActividadEntity(
            idActividad = 2,
            titulo = "Workshop de Modelado Sísmico en OpenSees",
            tipo = "Formación",
            fecha = "2026-11-22",
            descripcion = "Curso práctico intensivo de simulación computacional de conexiones y muros de madera para ingenieros calculistas.",
            idArea = 3
        ),
        ActividadEntity(
            idActividad = 3,
            titulo = "Mesa Redonda: Financiamiento y Seguros para Vivienda en Madera",
            tipo = "Colaboración",
            fecha = "2026-12-05",
            descripcion = "Diálogo público-privado con bancos, aseguradoras y el Ministerio de Vivienda para viabilizar hipotecas verdes.",
            idArea = 4
        ),
        ActividadEntity(
            idActividad = 4,
            titulo = "Curso de Inspección Técnica de Obras en Madera",
            tipo = "Formación",
            fecha = "2026-12-12",
            descripcion = "Capacitación en control de calidad, tolerancias de montaje y control de humedad durante faena constructiva.",
            idArea = 1
        ),
        ActividadEntity(
            idActividad = 5,
            titulo = "Feria de Ecoinnovación y Materiales del Bosque",
            tipo = "Evento",
            fecha = "2027-01-20",
            descripcion = "Exhibición de startups y prototipos tecnológicos basados en biopolímeros, celulosa modificada y derivados.",
            idArea = 2
        ),
        ActividadEntity(
            idActividad = 6,
            titulo = "Taller de Fabricación Digital y Ensamble Robotizado",
            tipo = "Formación",
            fecha = "2027-02-10",
            descripcion = "Sesión práctica en laboratorio CNC para parametrización de uniones carpinteras sin elementos metálicos.",
            idArea = 1
        ),
        ActividadEntity(
            idActividad = 7,
            titulo = "Lanzamiento Guía de Carbono Neutralidad en Edificación",
            tipo = "Evento",
            fecha = "2027-03-04",
            descripcion = "Presentación pública del documento metodológico y estándares de cálculo de huella ambiental de proyectos.",
            idArea = 4
        )
    )

    // CrossRefs
    val proyectoIntegrantes = listOf(
        ProyectoIntegranteCrossRef(idProyecto = 1, idIntegrante = 1),
        ProyectoIntegranteCrossRef(idProyecto = 1, idIntegrante = 2),
        ProyectoIntegranteCrossRef(idProyecto = 1, idIntegrante = 5),
        ProyectoIntegranteCrossRef(idProyecto = 2, idIntegrante = 2),
        ProyectoIntegranteCrossRef(idProyecto = 2, idIntegrante = 3),
        ProyectoIntegranteCrossRef(idProyecto = 2, idIntegrante = 8),
        ProyectoIntegranteCrossRef(idProyecto = 3, idIntegrante = 1),
        ProyectoIntegranteCrossRef(idProyecto = 3, idIntegrante = 6),
        ProyectoIntegranteCrossRef(idProyecto = 3, idIntegrante = 10),
        ProyectoIntegranteCrossRef(idProyecto = 4, idIntegrante = 4),
        ProyectoIntegranteCrossRef(idProyecto = 4, idIntegrante = 11),
        ProyectoIntegranteCrossRef(idProyecto = 5, idIntegrante = 3),
        ProyectoIntegranteCrossRef(idProyecto = 5, idIntegrante = 7),
        ProyectoIntegranteCrossRef(idProyecto = 6, idIntegrante = 1),
        ProyectoIntegranteCrossRef(idProyecto = 6, idIntegrante = 6),
        ProyectoIntegranteCrossRef(idProyecto = 7, idIntegrante = 4),
        ProyectoIntegranteCrossRef(idProyecto = 7, idIntegrante = 5),
        ProyectoIntegranteCrossRef(idProyecto = 8, idIntegrante = 3),
        ProyectoIntegranteCrossRef(idProyecto = 9, idIntegrante = 7),
        ProyectoIntegranteCrossRef(idProyecto = 10, idIntegrante = 1),
        ProyectoIntegranteCrossRef(idProyecto = 11, idIntegrante = 4),
        ProyectoIntegranteCrossRef(idProyecto = 11, idIntegrante = 11)
    )

    val proyectoCapacidades = listOf(
        ProyectoCapacidadCrossRef(idProyecto = 1, idCapacidad = 1),
        ProyectoCapacidadCrossRef(idProyecto = 1, idCapacidad = 2),
        ProyectoCapacidadCrossRef(idProyecto = 2, idCapacidad = 2),
        ProyectoCapacidadCrossRef(idProyecto = 2, idCapacidad = 4),
        ProyectoCapacidadCrossRef(idProyecto = 3, idCapacidad = 1),
        ProyectoCapacidadCrossRef(idProyecto = 4, idCapacidad = 3),
        ProyectoCapacidadCrossRef(idProyecto = 4, idCapacidad = 5),
        ProyectoCapacidadCrossRef(idProyecto = 5, idCapacidad = 6),
        ProyectoCapacidadCrossRef(idProyecto = 6, idCapacidad = 1),
        ProyectoCapacidadCrossRef(idProyecto = 7, idCapacidad = 3),
        ProyectoCapacidadCrossRef(idProyecto = 8, idCapacidad = 4),
        ProyectoCapacidadCrossRef(idProyecto = 9, idCapacidad = 6),
        ProyectoCapacidadCrossRef(idProyecto = 11, idCapacidad = 5)
    )

    val proyectoPublicaciones = listOf(
        ProyectoPublicacionCrossRef(idProyecto = 1, idPublicacion = 1),
        ProyectoPublicacionCrossRef(idProyecto = 1, idPublicacion = 2),
        ProyectoPublicacionCrossRef(idProyecto = 2, idPublicacion = 2),
        ProyectoPublicacionCrossRef(idProyecto = 3, idPublicacion = 1),
        ProyectoPublicacionCrossRef(idProyecto = 3, idPublicacion = 9),
        ProyectoPublicacionCrossRef(idProyecto = 4, idPublicacion = 3),
        ProyectoPublicacionCrossRef(idProyecto = 5, idPublicacion = 4),
        ProyectoPublicacionCrossRef(idProyecto = 6, idPublicacion = 1),
        ProyectoPublicacionCrossRef(idProyecto = 8, idPublicacion = 5),
        ProyectoPublicacionCrossRef(idProyecto = 9, idPublicacion = 4),
        ProyectoPublicacionCrossRef(idProyecto = 9, idPublicacion = 8)
    )
}
