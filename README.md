# CIM UC

Aplicación móvil Android (MVP académico) para explorar proyectos, capacidades I+D, integrantes, publicaciones y actividades de CIM UC, relacionarlos entre sí y orientar al usuario hacia oportunidades de colaboración y un contacto simulado.

## Integrantes

| Nombre             |
| ------------------ |
| Juan Pablo Casassa |
| Martin Gonzalez    |
| Claudio Jaime      |
| Lucas Vargas       |

## Funcionalidades

- [ ] Navegación entre las 7 pantallas del flujo principal (Inicio, Explorar, Detalle de proyecto, Contenidos relacionados, Actividades y oportunidades, Formulario de contacto, Confirmación).
- [ ] Búsqueda por texto insensible a mayúsculas y tildes.
- [ ] Filtros combinables por tipo, área temática y año, con etiquetas activas, contador de resultados y estado vacío.
- [ ] Detalle de proyecto con año, estado, área temática y descripción.
- [ ] Contenidos relacionados: integrantes, capacidades I+D y publicaciones de un proyecto.
- [ ] Actividades y oportunidades de colaboración filtrables por área temática.
- [ ] Formulario de contacto con validaciones visuales por campo y conservación de datos al corregir.
- [ ] Persistencia local de la solicitud de contacto.
- [ ] Al menos dos recursos nativos del dispositivo integrados en el flujo.
- [ ] Animaciones funcionales de navegación y retroalimentación.

## Arquitectura

El proyecto sigue el patrón **MVVM** con persistencia local mediante **Room**:

```
UI (Fragment / Adapter)  →  ViewModel  →  Repository  →  Room (DAO / Entity)
```

Las validaciones de formulario se centralizan en una capa lógica independiente de la vista.

## Tecnología

| Elemento               | Versión / detalle               |
| ---------------------- | -------------------------------- |
| Lenguaje               | Kotlin                           |
| Android Gradle Plugin  | 9.4.1                            |
| Gradle                 | 9.6.0                            |
| compileSdk / targetSdk | 37                               |
| minSdk                 | 33                               |
| UI                     | Android Views (XML) + Material 3 |
| Arquitectura           | MVVM                             |
| Persistencia           | Room                             |

## Estructura del repositorio

```
cim-uc/
├── android/                 # Proyecto Android (Gradle)
│   ├── app/
│   │   └── src/main/    
│   ├── gradle/          
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── docs/                    # Especificación y planificación
│   ├── 01-replanteamiento-caso.md
│   ├── 02-requerimientos-alto-nivel.md
│   ├── 03-requerimientos-f-rnf.md
│   ├── 04-casos-uso-o-historias.md
│   ├── 05-modelo-datos.md
│   └── 06-mockups.md
└── prototipo_interactivo_mvp_cim_uc.html
```

## Cómo ejecutar

### Requisitos

- Android Studio (versión compatible con AGP 9.4.1)
- JDK 11 o superior
- Android SDK con API 37 instalada
- Emulador o dispositivo físico con Android 13 (API 33) o superior

### Pasos

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/Lu-var/cim-uc.git
   cd cim-uc
   ```
2. Abrir la carpeta `android/` en Android Studio (no la raíz del repositorio).
3. Esperar la sincronización de Gradle.
4. Ejecutar la configuración `app` en un emulador o dispositivo conectado

### Compilar desde consola

```bash
cd android
./gradlew assembleDebug
```

El APK se genera en `android/app/build/outputs/apk/debug/`.

## Documentación

- [Replanteamiento del caso](docs/01-replanteamiento-caso.md)
- [Requerimientos de alto nivel](docs/02-requerimientos-alto-nivel.md)
- [Requerimientos funcionales y no funcionales](docs/03-requerimientos-f-rnf.md)
- [Historias de usuario](docs/04-casos-uso-o-historias.md)
- [Modelo de datos](docs/05-modelo-datos.md)
- [Mockups por pantalla](docs/06-mockups.md)

## Planificación

El seguimiento de tareas se realiza en GitHub Issues:

- Issues: https://github.com/Lu-var/cim-uc/issues
