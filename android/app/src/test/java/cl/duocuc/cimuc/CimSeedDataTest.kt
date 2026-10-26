package cl.duocuc.cimuc

import cl.duocuc.cimuc.data.local.seed.CimSeedData
import org.junit.Assert.assertTrue
import org.junit.Test

class CimSeedDataTest {

    @Test
    fun seedData_cumpleRequisitosMinimosDeCatalogo() {
        // Criterio Issue #21: >=10 proyectos, >=10 integrantes, >=6 capacidades, >=8 publicaciones, >=6 actividades
        assertTrue("Debe tener al menos 10 proyectos", CimSeedData.proyectos.size >= 10)
        assertTrue("Debe tener al menos 10 integrantes", CimSeedData.integrantes.size >= 10)
        assertTrue("Debe tener al menos 6 capacidades", CimSeedData.capacidades.size >= 6)
        assertTrue("Debe tener al menos 8 publicaciones", CimSeedData.publicaciones.size >= 8)
        assertTrue("Debe tener al menos 6 actividades", CimSeedData.actividades.size >= 6)
    }

    @Test
    fun seedData_integridadReferencialEsValida() {
        val areaIds = CimSeedData.areasTematicas.map { it.idArea }.toSet()
        val organizacionIds = CimSeedData.organizaciones.map { it.idOrganizacion }.toSet()
        val proyectoIds = CimSeedData.proyectos.map { it.idProyecto }.toSet()
        val integranteIds = CimSeedData.integrantes.map { it.idIntegrante }.toSet()
        val capacidadIds = CimSeedData.capacidades.map { it.idCapacidad }.toSet()
        val publicacionIds = CimSeedData.publicaciones.map { it.idPublicacion }.toSet()

        // Cada proyecto referencia un area valida
        CimSeedData.proyectos.forEach { p ->
            assertTrue("Proyecto ${p.idProyecto} referencia área inválida", areaIds.contains(p.idArea))
        }

        // Cada integrante referencia una organizacion valida
        CimSeedData.integrantes.forEach { i ->
            assertTrue("Integrante ${i.idIntegrante} referencia organización inválida", organizacionIds.contains(i.idOrganizacion))
        }

        // Cada actividad referencia un area valida
        CimSeedData.actividades.forEach { a ->
            assertTrue("Actividad ${a.idActividad} referencia área inválida", areaIds.contains(a.idArea))
        }

        // Cross refs
        CimSeedData.proyectoIntegrantes.forEach { ref ->
            assertTrue(proyectoIds.contains(ref.idProyecto))
            assertTrue(integranteIds.contains(ref.idIntegrante))
        }

        CimSeedData.proyectoCapacidades.forEach { ref ->
            assertTrue(proyectoIds.contains(ref.idProyecto))
            assertTrue(capacidadIds.contains(ref.idCapacidad))
        }

        CimSeedData.proyectoPublicaciones.forEach { ref ->
            assertTrue(proyectoIds.contains(ref.idProyecto))
            assertTrue(publicacionIds.contains(ref.idPublicacion))
        }
    }
}
