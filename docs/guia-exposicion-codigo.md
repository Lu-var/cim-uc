# CIM UC — Guía de código para la exposición

> Documento de estudio armado a partir del código real de `android/`. No reemplaza la documentación de `docs/`; la complementa.
>
> **Ruta rápida si tienes 15 minutos:** sección 2.5 (`by lazy`), sección 3 (Compose), sección 4 (Room), sección 6.6 (filtros) y sección 6.8 (formulario).
>
> **Estado verificado:** `./gradlew assembleDebug` compila sin errores. Hay 18 métodos `@Test` (17 de dominio + 1 de ejemplo). El APK queda en `android/app/build/outputs/apk/debug/app-debug.apk`.

---

## Índice

1. [Resumen y estado del proyecto](#1-resumen-y-estado-del-proyecto)
2. [Kotlin mínimo para leer el proyecto](#2-kotlin-mínimo-para-leer-el-proyecto)
3. [Compose: cómo la pantalla se actualiza sola](#3-compose-cómo-la-pantalla-se-actualiza-sola)
4. [Room: cómo se guardan los datos](#4-room-cómo-se-guardan-los-datos)
5. [Arquitectura MVVM](#5-arquitectura-mvvm)
6. [Los 10 bloques de código](#6-los-10-bloques-de-código)
7. [Modificar en vivo](#7-modificar-en-vivo)
8. [Guion de la demo](#8-guion-de-la-demo)
9. [Preguntas difíciles](#9-preguntas-difíciles)
10. [Chuleta y comandos](#10-chuleta-y-comandos)
- [Anexo A: cómo agregar un campo nuevo](#anexo-a-cómo-agregar-un-campo-nuevo)
- [Anexo B: glosario](#anexo-b-glosario)

---

<a id="1-resumen-y-estado-del-proyecto"></a>
## 1. Resumen y estado del proyecto

### Qué es

Aplicación Android nativa para explorar el catálogo de CIM UC: proyectos, capacidades I+D, integrantes, publicaciones y actividades. Permite buscar, filtrar, ver relaciones entre contenidos y enviar una solicitud de contacto que queda guardada en el dispositivo.

### Stack real

| Elemento | Lo que usa el código |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Persistencia | Room sobre SQLite |
| Arquitectura | MVVM + repositorio |
| Procesador de anotaciones | KSP (Room) |
| Build | Gradle 9.6, AGP 9.4.1, `minSdk 33`, `targetSdk 37` |

### Números que puedes citar

- 7 pantallas: Inicio, Explorar, Detalle, Relacionados, Actividades, Contacto y Confirmación.
- 11 proyectos, 4 áreas temáticas, 7 capacidades, 11 integrantes, 9 publicaciones y 7 actividades precargadas.
- 8 entidades Room + 3 tablas puente.
- 17 pruebas de dominio (validador, ViewModels, repositorio y seed) + 1 de ejemplo.
- Búsqueda insensible a mayúsculas y tildes.
- Persistencia local de la solicitud de contacto.

### Lo que sí está implementado

- Navegación entre las 7 pantallas con animaciones.
- Barra inferior solo en Inicio, Explorar y Actividades.
- Búsqueda por texto con normalización NFD.
- Filtros combinables por área y año, con chips activos, contador y estado vacío.
- Detalle con año, estado, área y descripción.
- Relacionados: integrantes, capacidades y publicaciones.
- Actividades filtrables por área.
- Formulario con validación por campo y conservación de datos.
- Persistencia de la solicitud en Room.
- Confirmación con los datos guardados.

### Cosas que no conviene afirmar

- **El README está desactualizado**: dice “Android Views (XML) + Fragment/Adapter”, pero el código es 100% Compose. Si lo leen, aclara que quedó de una versión anterior.
- **No hay recursos nativos** (cámara, `Intent`, compartir, permisos). El manifest solo declara la Activity. Si la rúbrica exige dos recursos nativos, es la brecha real del entregable.
- **El filtro por estado** está modelado en el ViewModel y en el DAO, pero la UI solo muestra chips de área y año.
- **El estado `Error` del formulario** conserva los datos, pero los campos no aceptan edición mientras se esté en ese estado. Solo se alcanza si falla la persistencia; en la demo normal no ocurre.
- **No hay pantalla de administración** de solicitudes: se guardan y se muestran en la confirmación, pero no hay listado.

---

<a id="2-kotlin-mínimo-para-leer-el-proyecto"></a>
## 2. Kotlin mínimo para leer el proyecto

No necesitas dominar Kotlin para exponer. Con estos conceptos puedes leer casi todo el código.

### 2.1 `val` y `var`

```kotlin
val nombre = "CIM UC"   // solo lectura: no se puede reasignar
var contador = 0        // mutable: sí se puede reasignar
```

En el proyecto casi todo es `val`: estados inmutables, listas, dependencias. Eso reduce efectos secundarios.

### 2.2 Tipos y nullabilidad

```kotlin
val titulo: String = "Proyecto"     // no puede ser null
val area: String? = null            // puede ser null
val largo = area?.length ?: 0       // si es null, usa 0
```

- `?` después del tipo significa “puede ser null”.
- `?.` es llamada segura: si el receptor es null, devuelve null y no explota.
- `?:` es el operador Elvis: entrega un valor por defecto.
- En el código verás `area?.nombre.orEmpty()`: si no hay área, devuelve `""`.

### 2.3 Funciones y lambdas

```kotlin
fun sumar(a: Int, b: Int): Int = a + b

val alClick = { println("tocado") }   // lambda: función sin nombre
```

Una lambda entre llaves se pasa como último parámetro:

```kotlin
Button(onClick = { navController.popBackStack() }) {
    Text("Volver")
}
```

`onClick = { ... }` es una lambda. El bloque `{ Text("Volver") }` es el contenido del botón, que también es una función (`@Composable`).

### 2.4 Clases que aparecen en el proyecto

**`data class`** — datos con `equals`, `hashCode`, `toString` y `copy` gratis:

```kotlin
data class ExplorarUiState(val query: String = "", val isLoading: Boolean = false)

val nuevo = estado.copy(query = "madera")
```

`copy()` crea una copia con un campo cambiado. Es la base de los estados inmutables.

**`sealed interface`** — una jerarquía cerrada de estados:

```kotlin
sealed interface ContactoUiState {
    data class Formulario(...) : ContactoUiState
    data class Enviando(...) : ContactoUiState
    data class Error(...) : ContactoUiState
    data class Exito(...) : ContactoUiState
}
```

El compilador obliga a cubrir todos los casos en un `when`. Si mañana agregas un estado, el código no compila hasta que lo manejes.

**`object`** — singleton:

```kotlin
object CimSeedData {
    val proyectos = listOf(...)
}
```

`CimSeedData.proyectos` se accede directo, sin instanciar.

**`companion object`** — miembros “estáticos” de una clase:

```kotlin
class AppDatabase : RoomDatabase() {
    companion object {
        fun get(context: Context): AppDatabase { ... }
    }
}
```

Se llama `AppDatabase.get(context)`.

**`open`** — permite heredar:

```kotlin
open class CimRepository(private val database: AppDatabase? = null) { ... }
```

Los tests crean un `object : CimRepository() { ... }` y sobrescriben funciones. Por eso las funciones del repositorio también son `open`.

### 2.5 `by lazy`: la duda más común

Esta línea:

```kotlin
val database: AppDatabase by lazy { AppDatabase.get(this) }
```

**No es un `fetch`. No hace ninguna petición. No es “lazy loading” de una lista.**

`by lazy` es una **inicialización diferida**: el bloque entre llaves se ejecuta la **primera vez que alguien lee la propiedad**, y el resultado se guarda para las siguientes lecturas.

Equivale, simplificando, a esto:

```kotlin
private val databaseDelegate = lazy { AppDatabase.get(this) }

val database: AppDatabase
    get() = databaseDelegate.value
```

- `lazy { ... }` devuelve un objeto `Lazy<T>`.
- `by` delega el `get()` de la propiedad a `Lazy.value`.
- La primera lectura ejecuta el bloque; las siguientes devuelven el valor guardado.
- Si nadie lee la propiedad, el bloque **nunca** se ejecuta.

Ejemplo mínimo:

```kotlin
val saludo: String by lazy {
    println("calculando...")
    "hola"
}

fun main() {
    println(saludo) // imprime "calculando..." y luego "hola"
    println(saludo) // imprime solo "hola"
}
```

#### Qué significa en `CimApplication`

```kotlin
class CimApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val cimRepository: CimRepository by lazy { CimRepository(database) }
    val contactoRepository: ContactoRepository by lazy { ContactoRepository(database) }
}
```

- La base y los repositorios se construyen **cuando se leen por primera vez**, no cuando arranca el proceso.
- En esta app, `MainActivity` los lee casi de inmediato, así que el ahorro real es pequeño. El valor está en el **orden de construcción** y en no repetir la creación.
- `AppDatabase.get(this)` sí hace trabajo real: crea el `RoomDatabase`. La apertura física del archivo SQLite ocurre en la primera consulta.
- `by lazy` por defecto es **thread-safe** (`LazyThreadSafetyMode.SYNCHRONIZED`): si dos hilos leen a la vez, uno construye y el otro espera. Por eso es seguro en una `Application`.

#### `by lazy` no es `lateinit`

| `by lazy` | `lateinit var` |
|---|---|
| `val`: solo lectura | `var`: mutable |
| Se inicializa solo, en la primera lectura | Lo asignas tú manualmente |
| Thread-safe por defecto | No es thread-safe por sí solo |
| Puede tener lógica de creación | Solo admite tipos no nulos |
| Si nunca se lee, no se ejecuta | Si nunca se asigna y se lee, lanza excepción |

#### El otro `by` del proyecto (no es lazy)

```kotlin
val conteos by viewModel.conteos.collectAsState()
```

Ese `by` **no tiene nada que ver con `lazy`**. `collectAsState()` devuelve un `State<Conteos>`, y `by` delega la lectura a `State.value`. Es decir, `conteos` es de tipo `Conteos`, no `State<Conteos>`.

Sin `by` sería:

```kotlin
val conteosState = viewModel.conteos.collectAsState()
val conteos = conteosState.value
```

Para que `by` funcione con `State` necesitas este import:

```kotlin
import androidx.compose.runtime.getValue
```

Y para `MutableState` con escritura:

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
```

Moraleja para la exposición: **`by` significa “delegar la propiedad”, no “cargar desde internet”**. El primer `by` delega en `lazy`; el segundo delega en `State`.

### 2.6 Funciones de extensión

```kotlin
private fun ProyectoConDetalles.toDomain(): Proyecto { ... }
```

Agrega una función a un tipo existente sin heredar. Se llama así:

```kotlin
val proyecto: Proyecto = detalle.toDomain()
```

En el repositorio se usa para traducir `Entity` (base de datos) a modelo de dominio (UI).

### 2.7 `suspend`: funciones que pueden esperar

```kotlin
suspend fun guardarSolicitud(...): Long
```

Una función `suspend` puede **pausarse sin bloquear el hilo** mientras espera una operación lenta (base de datos, red). Solo se puede llamar desde una corrutina o desde otra función `suspend`.

En el proyecto:

```kotlin
viewModelScope.launch {
    val id = contactoRepository.guardarSolicitud(...)
}
```

`launch` inicia una corrutina; `viewModelScope` la cancela cuando el ViewModel muere.

### 2.8 `when`

```kotlin
when (estado) {
    is ContactoUiState.Formulario -> FormularioContacto(...)
    is ContactoUiState.Enviando -> Spinner()
    is ContactoUiState.Error -> FormularioContacto(...)
    is ContactoUiState.Exito -> ...
}
```

Es el `switch` de Kotlin, pero puede devolver un valor y, con `sealed`, es exhaustivo.

### 2.9 Tabla de símbolos

| Símbolo | Significado |
|---|---|
| `val` / `var` | Solo lectura / mutable |
| `: Tipo` | Declaración de tipo |
| `?` | Nullable |
| `?.` | Llamada segura |
| `?:` | Valor por defecto si es null |
| `!!` | Forzar no-null; si es null, excepción (evitarlo) |
| `->` | Separa parámetros del cuerpo de una lambda; rama de `when` |
| `::` | Referencia a función (`viewModel::onQueryChanged`) |
| `it` | Parámetro implícito de una lambda de un solo argumento |
| `<T>` | Genérico (`Flow<List<Proyecto>>`) |
| `@Algo` | Anotación |
| `by` | Delegación de propiedad |
| `suspend` | Función pausable |
| `copy()` | Copia de un `data class` |
| `object` | Singleton |
| `companion object` | Miembros estáticos de una clase |

---

<a id="3-compose-cómo-la-pantalla-se-actualiza-sola"></a>
## 3. Compose: cómo la pantalla se actualiza sola

### 3.1 La diferencia mental con XML

Con XML (enfoque imperativo) harías:

```kotlin
val texto = findViewById<TextView>(R.id.titulo)
texto.text = proyecto.titulo
```

Con Compose describes **cómo se ve la UI para un estado dado**:

```kotlin
Text(text = proyecto.titulo)
```

Cuando el estado cambia, Compose vuelve a ejecutar la función y actualiza solo lo necesario. Tú no buscas vistas ni las modificas a mano.

### 3.2 `@Composable` y recomposición

Una función `@Composable` es una función que **emite UI**. Solo se puede llamar desde otra función `@Composable` o desde `setContent`.

**Recomposición** es el proceso por el que Compose vuelve a ejecutar una función cuando cambia un estado que esa función lee.

```kotlin
@Composable
fun Contador() {
    var n by remember { mutableStateOf(0) }

    Button(onClick = { n++ }) {
        Text("$n")
    }
}
```

- `mutableStateOf(0)` crea un estado observable.
- `n++` cambia el estado.
- Compose detecta que `Text` lee `n` y vuelve a ejecutar esa parte.
- `remember` conserva `n` entre recomposiciones. Sin `remember`, cada recomposición volvería a crear el estado en 0.

En el proyecto no se usa `mutableStateOf` directamente en las pantallas: el estado vive en el ViewModel y llega como `StateFlow`. Pero el principio es el mismo.

### 3.3 State hoisting: el estado baja, los eventos suben

Mira la firma de `InicioScreen`:

```kotlin
@Composable
fun InicioScreen(
    conteos: Conteos,
    onBuscar: () -> Unit,
    onTipoSeleccionado: (String) -> Unit,
    onActividades: () -> Unit
)
```

- **Estado hacia abajo:** `conteos`.
- **Eventos hacia arriba:** `onBuscar`, `onTipoSeleccionado`, `onActividades`.

La pantalla no sabe que existe un ViewModel, ni Room, ni la navegación. Solo dibuja y avisa. Eso la hace reutilizable y testeable.

Quien conecta todo es `AppNavigation`:

```kotlin
val viewModel: InicioViewModel = viewModel(factory = viewModelFactory)
val conteos by viewModel.conteos.collectAsState()
InicioScreen(conteos = conteos, onBuscar = { ... })
```

### 3.4 `collectAsState`: el puente entre Flow y Compose

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

- `viewModel.uiState` es un `StateFlow<ExplorarUiState>`.
- `collectAsState()` se suscribe al flujo y devuelve un `State<ExplorarUiState>`.
- Cada emisión actualiza el `State` y dispara recomposición.
- `by` hace que uses `uiState` directo, sin `.value`.

Sin `collectAsState`, la pantalla se quedaría con el valor inicial para siempre. Fue un bug real del proyecto: “carga infinita” por no suscribir los `UiState`.

Alternativa recomendada en Android moderno: `collectAsStateWithLifecycle()`, que pausa la recolección cuando la app pasa a segundo plano. El proyecto usa `collectAsState()` por simplicidad.

### 3.5 `remember` y `rememberSaveable`

```kotlin
val solicitud by remember(solicitudId) {
    contactoRepository.getSolicitudById(solicitudId)
}.collectAsState(initial = null)
```

- `remember` guarda un valor entre recomposiciones.
- `remember(solicitudId)` lo reinicia si cambia la clave.
- `rememberSaveable` además sobrevive a rotaciones y muerte del proceso; no se usa aquí.
- El estado importante vive en el ViewModel, que sobrevive a cambios de configuración. Eso es mejor que `rememberSaveable` para lógica.

### 3.6 `LaunchedEffect`: efectos secundarios controlados

```kotlin
LaunchedEffect(proyectoId) {
    viewModel.cargarProyecto(proyectoId)
}
```

- Se ejecuta al entrar en composición y cada vez que cambia la clave (`proyectoId`).
- Se cancela al salir de la pantalla.
- Sirve para llamar funciones `suspend`, navegar o cargar datos.
- **No** debes llamar `cargarProyecto` directamente en el cuerpo del composable: se ejecutaría en cada recomposición.

### 3.7 `Modifier`: cadena de modificadores

```kotlin
modifier = modifier
    .fillMaxWidth()
    .heightIn(min = 48.dp)
    .padding(16.dp)
```

- `Modifier` describe tamaño, padding, click, fondo, semántica, etc.
- El **orden importa**: `padding(16.dp).background(rojo)` pinta el fondo dentro del padding; `background(rojo).padding(16.dp)` pinta también el área del padding.
- `heightIn(min = 48.dp)` cumple el target táctil mínimo de accesibilidad.

### 3.8 `Scaffold`, `MaterialTheme` y `LazyColumn`

**`Scaffold`** organiza una pantalla con slots:

```kotlin
Scaffold(
    topBar = { TopAppBarCommon(title = "Explorar") },
    bottomBar = { ... }
) { padding ->
    Column(modifier = Modifier.padding(padding)) { ... }
}
```

El `padding` que entrega el `Scaffold` evita que el contenido quede debajo de las barras.

**`MaterialTheme`** entrega tokens de diseño:

```kotlin
color = MaterialTheme.colorScheme.primary
style = MaterialTheme.typography.titleMedium
```

Cambiar `RojoUC` en `Color.kt` repinta todo lo que use `primary`.

**`LazyColumn`** compone solo los ítems visibles:

```kotlin
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(uiState.proyectos, key = { it.id }) { proyecto ->
        ProyectoCard(
            proyecto = proyecto,
            onClick = { onProyectoClick(proyecto.id) },
            modifier = Modifier.animateItem()
        )
    }
}
```

- `items` recibe la lista.
- `key = { it.id }` da identidad estable a cada ítem; sin key, al filtrar se pueden reutilizar mal los estados internos.
- `animateItem()` anima aparición, desaparición y movimiento al cambiar la lista.

### 3.9 Errores típicos de Compose (y cómo los evita el proyecto)

| Error | Qué pasa | Cómo se evita aquí |
|---|---|---|
| Crear estado dentro del composable sin `remember` | Se reinicia en cada recomposición | Estado en ViewModel |
| Llamar una función `suspend` en el cuerpo | No compila | `LaunchedEffect` + `viewModelScope` |
| Leer el estado demasiado arriba | Recompone toda la pantalla | Se pasa a componentes pequeños |
| Mutar una lista directamente | Compose no detecta el cambio | `copy()` y listas nuevas |
| Olvidar `collectAsState` | La UI no se actualiza | `collectAsState()` en cada pantalla |
| Olvidar `getValue` al usar `by` | Error de compilación | `import androidx.compose.runtime.getValue` |

### 3.10 Por qué no hay XML

- Menos archivos: una pantalla es una función Kotlin.
- El estado y la UI están en el mismo lenguaje.
- Material 3 viene con componentes listos.
- La vista previa y los tests de UI son más directos.
- El proyecto migró de la idea original con Views a Compose; el README no se actualizó.

---

<a id="4-room-cómo-se-guardan-los-datos"></a>
## 4. Room: cómo se guardan los datos

### 4.1 Qué es Room

SQLite viene en Android, pero usarlo directo implica escribir mucho código repetitivo. Room es una capa sobre SQLite que:

- Convierte clases Kotlin en tablas con anotaciones.
- Verifica el SQL **en tiempo de compilación**.
- Genera el código de acceso (`AppDatabase_Impl`, `ProyectoDao_Impl`, etc.) mediante KSP.
- Devuelve `Flow` observables.
- Maneja migraciones de esquema.

No es una base de datos distinta: sigue siendo SQLite. Es un **mapeador objeto-relacional**.

### 4.2 Entidad = tabla

```kotlin
@Entity(tableName = "proyecto")
data class ProyectoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_proyecto")
    val idProyecto: Int,
    @ColumnInfo(name = "titulo")
    val titulo: String,
    @ColumnInfo(name = "anio")
    val anio: Int,
    @ColumnInfo(name = "id_area")
    val idArea: Int
)
```

- `@Entity` crea la tabla `proyecto`.
- `@PrimaryKey` marca la clave primaria.
- `@ColumnInfo(name = "...")` cambia el nombre de la columna en SQLite (snake_case) sin cambiar el nombre en Kotlin (camelCase).
- `@ForeignKey` declara la relación con `area_tematica`; `onDelete = CASCADE` significa que al borrar un área se borran sus proyectos.
- `@Index` acelera las búsquedas por clave foránea.

### 4.3 DAO = acceso a datos

```kotlin
@Dao
interface ProyectoDao {
    @Query("SELECT * FROM proyecto ORDER BY anio DESC, titulo ASC")
    fun getAll(): Flow<List<ProyectoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(proyectos: List<ProyectoEntity>)

    @Query("SELECT DISTINCT anio FROM proyecto ORDER BY anio DESC")
    fun getAniosDisponibles(): Flow<List<Int>>
}
```

- Es una **interfaz**: Room genera la implementación.
- `@Query` valida el SQL al compilar. Si escribes mal una columna, el build falla.
- `Flow<T>` hace la consulta observable.
- `suspend` hace la escritura asíncrona.

### 4.4 Por qué `Flow` se actualiza solo

Cuando devuelves `Flow`, Room:

1. Ejecuta la consulta en un hilo de fondo.
2. Registra qué tablas leyó (por ejemplo, `proyecto` y `area_tematica`).
3. Observa cambios en esas tablas.
4. Cuando hay un `INSERT`, `UPDATE` o `DELETE`, invalida la consulta.
5. Vuelve a ejecutarla y emite la lista nueva.

Ese mecanismo se llama **InvalidationTracker**. Es la razón por la que el contador de Inicio cambia solo después del seed, sin refrescar la pantalla.

El `Flow` es **frío**: la consulta no se ejecuta hasta que alguien lo recolecta. El ViewModel lo mantiene “caliente” con `stateIn`.

### 4.5 Relaciones muchos-a-muchos

Un proyecto tiene varios integrantes y un integrante puede estar en varios proyectos. En SQL se resuelve con una **tabla puente**:

```kotlin
@Entity(
    tableName = "proyecto_integrante",
    primaryKeys = ["id_proyecto", "id_integrante"]
)
data class ProyectoIntegranteCrossRef(
    val idProyecto: Int,
    val idIntegrante: Int
)
```

Y Room lo lee con `@Relation` + `Junction`:

```kotlin
data class ProyectoConDetalles(
    @Embedded val proyecto: ProyectoEntity,

    @Relation(
        parentColumn = "id_proyecto",
        entityColumn = "id_integrante",
        associateBy = Junction(ProyectoIntegranteCrossRef::class)
    )
    val integrantes: List<IntegranteEntity> = emptyList()
)
```

- `@Embedded` aplana las columnas del proyecto en el objeto.
- `@Relation` trae la lista relacionada.
- `Junction` indica la tabla puente.
- `@Transaction` en el DAO garantiza que el proyecto y sus listas se lean del mismo estado consistente.

La relación uno-a-muchos (`proyecto` → `area_tematica`) usa `parentColumn = "id_area"` y `entityColumn = "id_area"`, sin `Junction`.

### 4.6 `suspend`, hilos y el hilo principal

Room **no permite** consultas bloqueantes en el hilo principal: lanza `IllegalStateException: Cannot access database on the main thread`.

Por eso:

- Las consultas que devuelven `Flow` se ejecutan en un executor de Room.
- Las escrituras son `suspend`, así que Room las mueve fuera del hilo principal.
- El ViewModel las llama dentro de `viewModelScope.launch`.
- La UI nunca llama a un DAO directamente.

### 4.7 Migraciones: por qué la base es versión 2

La app empezó con una tabla `solicitud_contacto` sin `organizacion` ni `motivo`. Al agregarlas, cambió el esquema:

```kotlin
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE solicitud_contacto ADD COLUMN organizacion TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE solicitud_contacto ADD COLUMN motivo TEXT NOT NULL DEFAULT ''")
    }
}
```

Puntos clave:

- `version = 2` en `@Database`.
- `addMigrations(MIGRATION_1_2)` registra la migración.
- `ALTER TABLE ... ADD COLUMN ... NOT NULL DEFAULT ''`: si la columna es obligatoria y la tabla ya tiene filas, **necesitas un default** o SQLite falla.
- Room guarda un hash del esquema. Si cambias una entidad y **no** subes la versión, al abrir la app falla con “Room cannot verify the data integrity”.
- Si subes la versión y **no** das migración, falla con “A migration from 2 to 3 was required but not found”.
- `fallbackToDestructiveMigration()` borraría la base y perdería las solicitudes; **no se usa**, y es correcto para este caso.

### 4.8 Seed: los datos de demostración

```kotlin
.addCallback(object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val database = get(context)
            database.areaTematicaDao().insertAll(CimSeedData.areasTematicas)
            database.organizacionDao().insertAll(CimSeedData.organizaciones)
            database.proyectoDao().insertAll(CimSeedData.proyectos)
            // ... capacidades, publicaciones, integrantes, actividades
            // ... tablas puente al final
        }
    }
})
```

- `onCreate` corre **una sola vez**, cuando se crea la base. No corre al abrirla de nuevo.
- Por eso, si cambias `CimSeedData` y no borras los datos de la app, no verás el cambio.
- El seed se lanza en `Dispatchers.IO` para no bloquear el hilo principal.
- Se insertan primero las tablas padre (áreas, organizaciones) y al final las tablas puente, para respetar las claves foráneas.
- `REPLACE` en las tablas maestras y `IGNORE` en las puentes hacen la precarga idempotente.
- Mejora posible: envolver todo el seed en `database.withTransaction { ... }` para que sea atómico. Hoy, si falla a mitad, podría quedar una precarga parcial.

### 4.9 Singleton: una sola instancia

```kotlin
@Volatile
private var INSTANCE: AppDatabase? = null

fun get(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(...).build()
        INSTANCE = instance
        instance
    }
}
```

- `@Volatile` asegura que todos los hilos vean el valor actualizado.
- `synchronized` evita que dos hilos construyan dos instancias a la vez.
- Room no permite dos instancias sobre el mismo archivo de base de datos.

### 4.10 Errores de Room que conviene reconocer

| Error | Causa | Solución |
|---|---|---|
| `Cannot access database on the main thread` | Llamada bloqueante en UI | Usar `Flow` o `suspend` |
| `Room cannot verify the data integrity` | Entidad cambiada sin subir versión | Subir versión |
| `A migration from X to Y was required but not found` | Versión subida sin migración | Agregar `Migration` |
| `FOREIGN KEY constraint failed` | Insertar hijo antes que padre | Respetar orden del seed |
| `no such column` | `@ColumnInfo` o SQL desactualizado | Corregir nombre o migrar |
| `UNIQUE constraint failed` | Clave primaria duplicada | Usar `REPLACE`/`IGNORE` o revisar datos |

---

<a id="5-arquitectura-mvvm"></a>
## 5. Arquitectura MVVM

### 5.1 El flujo completo

```text
Room (SQLite)
   │  Flow<List<Entity>>
   ▼
Repositorio
   │  Flow<List<Modelo>>
   ▼
ViewModel
   │  StateFlow<UiState>
   ▼
Compose
   │  eventos (click, texto)
   └──────────────► ViewModel
```

La UI **nunca** toca Room. El ViewModel **nunca** toca Compose. El repositorio **nunca** sabe que existe una pantalla.

### 5.2 Responsabilidades

| Capa | Sabe de | No sabe de |
|---|---|---|
| `Entity` / `Dao` | SQL, tablas, columnas | UI, ViewModel |
| `Repository` | DAOs, mapeo a dominio | Compose, navegación |
| `ViewModel` | Repositorio, estado de pantalla | Composable, Android Views |
| `Screen` | Estado y callbacks | Room, DAOs |
| `Navigation` | Rutas, ViewModels, repositorios | SQL |

### 5.3 Flujo unidireccional

1. La pantalla recibe un `UiState` inmutable.
2. El usuario genera un evento (`onQueryChanged`, `onEnviarClick`).
3. El ViewModel actualiza su estado.
4. Compose observa el nuevo estado y se redibuja.

No hay dos fuentes de verdad. El estado siempre va en una dirección.

### 5.4 La factory de ViewModels

`android/app/src/main/java/cl/duocuc/cimuc/CimViewModelFactory.kt`

```kotlin
package cl.duocuc.cimuc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.data.repository.ContactoRepository
import cl.duocuc.cimuc.viewmodel.ActividadesViewModel
import cl.duocuc.cimuc.viewmodel.ContactoViewModel
import cl.duocuc.cimuc.viewmodel.DetalleViewModel
import cl.duocuc.cimuc.viewmodel.ExplorarViewModel
import cl.duocuc.cimuc.viewmodel.InicioViewModel

// Factory simple para inyectar el repositorio en los ViewModels (MVVM)
class CimViewModelFactory(
    private val cimRepository: CimRepository,
    private val contactoRepository: ContactoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(InicioViewModel::class.java) ->
                InicioViewModel(cimRepository) as T
            modelClass.isAssignableFrom(ExplorarViewModel::class.java) ->
                ExplorarViewModel(cimRepository) as T
            modelClass.isAssignableFrom(DetalleViewModel::class.java) ->
                DetalleViewModel(cimRepository) as T
            modelClass.isAssignableFrom(ActividadesViewModel::class.java) ->
                ActividadesViewModel(cimRepository) as T
            modelClass.isAssignableFrom(ContactoViewModel::class.java) ->
                ContactoViewModel(contactoRepository) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
```

**Léelo así**

- `viewModel(factory = viewModelFactory)` pide un ViewModel; la factory decide cuál construir y qué repositorio inyectar.
- `isAssignableFrom` comprueba si la clase pedida corresponde a ese ViewModel.
- El `as T` es un cast; por eso lleva `@Suppress("UNCHECKED_CAST")`.
- Si se pide un ViewModel no registrado, lanza una excepción explícita. Es mejor que fallar en silencio.
- No se usa Hilt porque el MVP tiene pocas dependencias; una factory manual es suficiente y se explica en una frase.

### 5.5 Decisiones que conviene saber defender

- **Dos repositorios:** `CimRepository` para contenido y `ContactoRepository` para solicitudes. Separa responsabilidades y permite testear el formulario sin arrastrar todo el catálogo.
- **Base nullable en repositorios:** permite crear el repositorio sin Room en tests unitarios. Sin base, devuelve `emptyFlow()`.
- **Clases y funciones `open`:** permiten herencia y fakes en tests (`object : CimRepository() { ... }`).
- **Modelo de dominio separado de `Entity`:** la UI no depende del esquema SQL. Si cambia una columna, se ajusta el mapper.
- **Estado en el ViewModel, no en el composable:** sobrevive a rotaciones y a recomposiciones.

### 5.6 Traza completa de un clic

1. El usuario toca una tarjeta en Explorar.
2. `ProyectoCard` ejecuta `onClick`.
3. `ExplorarScreen` llama `onProyectoClick(proyecto.id)`.
4. `AppNavigation` navega a `detalle/3`.
5. `NavHost` crea `DetalleScreen` y su `DetalleViewModel`.
6. `LaunchedEffect(proyectoId)` llama `viewModel.cargarProyecto(3)`.
7. El ViewModel lanza una corrutina y pide `repository.getProyectoById(3)`.
8. El repositorio llama `proyectoDao().getWithDetallesById(3)`.
9. Room ejecuta la consulta con `@Transaction` y arma `ProyectoConDetalles`.
10. El repositorio mapea a `Proyecto` y lo emite en un `Flow`.
11. El ViewModel actualiza `DetalleUiState`.
12. `collectAsState()` dispara la recomposición.
13. `DetalleScreen` pinta título, chips y descripción.
14. Al tocar “Contactar a CIM UC”, se navega a `contacto/3` y se repite el ciclo con el formulario.

---

<a id="6-los-10-bloques-de-código"></a>
## 6. Los 10 bloques de código

Cada bloque incluye el código completo, qué hace y cómo leerlo. Si solo puedes memorizar tres: **6.3 (Room)**, **6.6 (filtros)** y **6.7 (validación)**.

### 6.1 Arranque y composición: `CimApplication.kt` + `MainActivity.kt`

`android/app/src/main/java/cl/duocuc/cimuc/CimApplication.kt`

```kotlin
package cl.duocuc.cimuc

import android.app.Application
import cl.duocuc.cimuc.data.local.AppDatabase
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.data.repository.ContactoRepository

// Punto único de composición de la capa de datos (N1 / arquitectura MVVM)
class CimApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val cimRepository: CimRepository by lazy { CimRepository(database) }
    val contactoRepository: ContactoRepository by lazy { ContactoRepository(database) }
}
```

`android/app/src/main/java/cl/duocuc/cimuc/MainActivity.kt`

```kotlin
package cl.duocuc.cimuc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duocuc.cimuc.navigation.AppNavigation
import cl.duocuc.cimuc.ui.theme.CimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as CimApplication
        setContent {
            CimTheme {
                AppNavigation(
                    cimRepository = application.cimRepository,
                    contactoRepository = application.contactoRepository
                )
            }
        }
    }
}
```

**Qué hace:** `CimApplication` es el composition root: construye la base y los repositorios una sola vez. `MainActivity` enciende Compose y entrega esas dependencias a la navegación.

**Léelo así**

- No hay Hilt ni Koin; la inyección es manual y explícita.
- `by lazy` retrasa la construcción hasta la primera lectura (ver sección 2.5). No es un `fetch`.
- `setContent` es la prueba de que la app es Compose.
- `application as CimApplication` funciona porque el manifest declara `android:name=".CimApplication"`.
- La Activity no tiene lógica de negocio.

**Frase:** “La Activity no sabe de proyectos ni de Room; solo enciende Compose y entrega las dependencias ya armadas.”

### 6.2 Rutas centralizadas: `Screen.kt`

`android/app/src/main/java/cl/duocuc/cimuc/navigation/Screen.kt`

```kotlin
package cl.duocuc.cimuc.navigation

// Rutas centralizadas de las 7 pantallas (N1 / issue #1)
sealed class Screen(val route: String) {
    data object Inicio : Screen("inicio")
    data object Explorar : Screen("explorar")
    data object Detalle : Screen("detalle/{proyectoId}") {
        fun create(proyectoId: Int) = "detalle/$proyectoId"
    }
    data object Relacionados : Screen("relacionados/{proyectoId}") {
        fun create(proyectoId: Int) = "relacionados/$proyectoId"
    }
    data object Actividades : Screen("actividades?areaId={areaId}") {
        fun create(areaId: Int = -1) = "actividades?areaId=$areaId"
    }
    data object Contacto : Screen("contacto/{proyectoId}") {
        fun create(proyectoId: Int) = "contacto/$proyectoId"
    }
    data object Confirmacion : Screen("confirmacion/{solicitudId}") {
        fun create(solicitudId: Int) = "confirmacion/$solicitudId"
    }

    companion object {
        val bottomBarRoutes = setOf(Inicio.route, Explorar.route, Actividades.route)
    }
}
```

**Qué hace:** centraliza las rutas y sus constructores. Evita strings regados por el código.

**Léelo así**

- `sealed class` + `data object`: cada pantalla es una constante.
- `{proyectoId}` es un **path param**; `?areaId={areaId}` es un **query param** opcional.
- `create()` arma la ruta con el id. Así no escribes `"detalle/$id"` en cada pantalla.
- `bottomBarRoutes` decide dónde se muestra la barra inferior.
- Si renombras una ruta, se cambia en un solo archivo.

**Frase:** “Las rutas viven en un solo archivo; la navegación no está regada por las pantallas.”

### 6.3 Room: `AppDatabase.kt`

`android/app/src/main/java/cl/duocuc/cimuc/data/local/AppDatabase.kt`

```kotlin
package cl.duocuc.cimuc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duocuc.cimuc.data.local.dao.ActividadDao
import cl.duocuc.cimuc.data.local.dao.AreaTematicaDao
import cl.duocuc.cimuc.data.local.dao.CapacidadDao
import cl.duocuc.cimuc.data.local.dao.IntegranteDao
import cl.duocuc.cimuc.data.local.dao.OrganizacionDao
import cl.duocuc.cimuc.data.local.dao.ProyectoDao
import cl.duocuc.cimuc.data.local.dao.PublicacionDao
import cl.duocuc.cimuc.data.local.dao.SolicitudContactoDao
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
import cl.duocuc.cimuc.data.local.entity.SolicitudContactoEntity
import cl.duocuc.cimuc.data.local.seed.CimSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AreaTematicaEntity::class,
        OrganizacionEntity::class,
        ProyectoEntity::class,
        CapacidadEntity::class,
        PublicacionEntity::class,
        IntegranteEntity::class,
        ActividadEntity::class,
        SolicitudContactoEntity::class,
        ProyectoIntegranteCrossRef::class,
        ProyectoCapacidadCrossRef::class,
        ProyectoPublicacionCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun proyectoDao(): ProyectoDao
    abstract fun areaTematicaDao(): AreaTematicaDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun capacidadDao(): CapacidadDao
    abstract fun publicacionDao(): PublicacionDao
    abstract fun integranteDao(): IntegranteDao
    abstract fun actividadDao(): ActividadDao
    abstract fun solicitudContactoDao(): SolicitudContactoDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE solicitud_contacto ADD COLUMN organizacion TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE solicitud_contacto ADD COLUMN motivo TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cim_uc.db"
                ).addMigrations(MIGRATION_1_2).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = get(context)
                            // Precarga idempotente en orden de dependencias FK
                            database.areaTematicaDao().insertAll(CimSeedData.areasTematicas)
                            database.organizacionDao().insertAll(CimSeedData.organizaciones)
                            database.proyectoDao().insertAll(CimSeedData.proyectos)
                            database.capacidadDao().insertAll(CimSeedData.capacidades)
                            database.publicacionDao().insertAll(CimSeedData.publicaciones)
                            database.integranteDao().insertAll(CimSeedData.integrantes)
                            database.actividadDao().insertAll(CimSeedData.actividades)

                            database.proyectoDao().insertCrossRefIntegrantes(CimSeedData.proyectoIntegrantes)
                            database.proyectoDao().insertCrossRefCapacidades(CimSeedData.proyectoCapacidades)
                            database.proyectoDao().insertCrossRefPublicaciones(CimSeedData.proyectoPublicaciones)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Qué hace:** declara la base `cim_uc.db`, sus 11 tablas, la versión 2, la migración y la precarga inicial.

**Léelo así**

- 8 entidades reales + 3 tablas puente (`CrossRef`) para relaciones muchos-a-muchos.
- `version = 2` porque la solicitud de contacto ganó `organizacion` y `motivo`.
- `ALTER TABLE ... NOT NULL DEFAULT ''`: el default es obligatorio para filas existentes.
- `@Volatile` + `synchronized`: singleton seguro entre hilos.
- `onCreate` corre solo al crear la base. **No** al abrirla de nuevo. Por eso cambiar el seed requiere borrar datos.
- El seed respeta el orden de claves foráneas y se ejecuta en `Dispatchers.IO`.
- La precarga es asíncrona: los contadores pueden mostrar 0 un instante y luego actualizarse por los `Flow`.

**Frase:** “Room crea la base una vez, aplica migraciones versionadas y precarga los datos de demostración en segundo plano.”

### 6.4 Consultas: `ProyectoDao.kt`

`android/app/src/main/java/cl/duocuc/cimuc/data/local/dao/ProyectoDao.kt`

```kotlin
package cl.duocuc.cimuc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cl.duocuc.cimuc.data.local.entity.ProyectoCapacidadCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoEntity
import cl.duocuc.cimuc.data.local.entity.ProyectoIntegranteCrossRef
import cl.duocuc.cimuc.data.local.entity.ProyectoPublicacionCrossRef
import cl.duocuc.cimuc.data.local.relation.ProyectoConDetalles
import kotlinx.coroutines.flow.Flow

@Dao
interface ProyectoDao {

    @Query("SELECT * FROM proyecto ORDER BY anio DESC, titulo ASC")
    fun getAll(): Flow<List<ProyectoEntity>>

    @Transaction
    @Query("SELECT * FROM proyecto ORDER BY anio DESC, titulo ASC")
    fun getAllWithDetalles(): Flow<List<ProyectoConDetalles>>

    @Query("SELECT * FROM proyecto WHERE id_proyecto = :id")
    fun getById(id: Int): Flow<ProyectoEntity?>

    @Transaction
    @Query("SELECT * FROM proyecto WHERE id_proyecto = :id")
    fun getWithDetallesById(id: Int): Flow<ProyectoConDetalles?>

    @Transaction
    @Query("""
        SELECT * FROM proyecto
        WHERE (:query IS NULL OR :query = '' OR LOWER(titulo) LIKE '%' || LOWER(:query) || '%' OR LOWER(descripcion) LIKE '%' || LOWER(:query) || '%')
          AND (:idArea IS NULL OR id_area = :idArea)
          AND (:anio IS NULL OR anio = :anio)
          AND (:estado IS NULL OR :estado = '' OR LOWER(estado) = LOWER(:estado))
        ORDER BY anio DESC, titulo ASC
    """)
    fun searchAndFilterWithDetalles(
        query: String?,
        idArea: Int?,
        anio: Int?,
        estado: String?
    ): Flow<List<ProyectoConDetalles>>

    @Query("SELECT DISTINCT anio FROM proyecto ORDER BY anio DESC")
    fun getAniosDisponibles(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(proyectos: List<ProyectoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefIntegrantes(crossRefs: List<ProyectoIntegranteCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefCapacidades(crossRefs: List<ProyectoCapacidadCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefPublicaciones(crossRefs: List<ProyectoPublicacionCrossRef>)
}
```

**Qué hace:** define todas las consultas de proyectos y las escrituras del seed.

**Léelo así**

- `@Dao` es una interfaz; KSP genera la implementación.
- `Flow` convierte la consulta en observable: Room reemite cuando cambian las tablas involucradas.
- `@Transaction` asegura consistencia al leer proyecto + relaciones.
- La consulta de búsqueda usa **parámetros opcionales**: `(:query IS NULL OR ...)`. Una sola función sirve para combinar texto, área, año y estado.
- `LOWER(...) LIKE ...` ignora mayúsculas, pero **no** tildes. La normalización de tildes vive en el ViewModel.
- `DISTINCT` evita años repetidos en los chips.
- `REPLACE` en tablas maestras e `IGNORE` en puentes hacen el seed idempotente.
- La app hoy llama `getProyectos()` sin filtros y filtra en el ViewModel; la consulta SQL queda lista para cuando el catálogo crezca.

**Frase:** “El DAO devuelve `Flow`, así que la base notifica cambios y la pantalla se vuelve a dibujar sin refrescar a mano.”

### 6.5 Repositorio: `CimRepository.kt`

`android/app/src/main/java/cl/duocuc/cimuc/data/repository/CimRepository.kt`

```kotlin
package cl.duocuc.cimuc.data.repository

import cl.duocuc.cimuc.data.local.AppDatabase
import cl.duocuc.cimuc.data.local.relation.ActividadConArea
import cl.duocuc.cimuc.data.local.relation.ProyectoConDetalles
import cl.duocuc.cimuc.domain.model.Actividad
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Conteos
import cl.duocuc.cimuc.domain.model.Integrante
import cl.duocuc.cimuc.domain.model.Proyecto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

open class CimRepository(private val database: AppDatabase? = null) {

    open fun getProyectos(
        query: String? = null,
        areaId: Int? = null,
        anio: Int? = null,
        estado: String? = null
    ): Flow<List<Proyecto>> {
        val db = database ?: return emptyFlow()
        val sanitizedQuery = query?.trim()?.ifEmpty { null }
        return db.proyectoDao()
            .searchAndFilterWithDetalles(sanitizedQuery, areaId, anio, estado)
            .map { list -> list.map { it.toDomain() } }
    }

    open fun getProyectoById(id: Int): Flow<Proyecto?> {
        val db = database ?: return emptyFlow()
        return db.proyectoDao().getWithDetallesById(id).map { it?.toDomain() }
    }

    open fun getAreasTematicas(): Flow<List<AreaTematica>> {
        val db = database ?: return emptyFlow()
        return db.areaTematicaDao().getAll().map { list ->
            list.map { AreaTematica(id = it.idArea, nombre = it.nombre, descripcion = it.descripcion) }
        }
    }

    open fun getAniosDisponibles(): Flow<List<Int>> {
        val db = database ?: return emptyFlow()
        return db.proyectoDao().getAniosDisponibles()
    }

    open fun getActividades(): Flow<List<Actividad>> {
        val db = database ?: return emptyFlow()
        return db.actividadDao().getAllWithArea().map { list ->
            list.map { it.toDomain() }
        }
    }

    open fun getIntegrantes(): Flow<List<Integrante>> {
        val db = database ?: return emptyFlow()
        return db.integranteDao().getAll().map { list ->
            list.map {
                Integrante(
                    id = it.idIntegrante,
                    nombre = it.nombre,
                    rol = it.rol,
                    especialidad = it.especialidad,
                    organizacionId = it.idOrganizacion
                )
            }
        }
    }

    // Conteos para las tarjetas de acceso de P-01 (mockup HU-01)
    open fun getConteos(): Flow<Conteos> {
        val db = database ?: return emptyFlow()
        return combine(
            db.proyectoDao().getAll(),
            db.capacidadDao().getAll(),
            db.integranteDao().getAll(),
            db.publicacionDao().getAll(),
            db.actividadDao().getAll()
        ) { proyectos, capacidades, integrantes, publicaciones, actividades ->
            Conteos(
                proyectos = proyectos.size,
                capacidades = capacidades.size,
                integrantes = integrantes.size,
                publicaciones = publicaciones.size,
                actividades = actividades.size
            )
        }
    }

    private fun ProyectoConDetalles.toDomain(): Proyecto {
        return Proyecto(
            id = proyecto.idProyecto,
            titulo = proyecto.titulo,
            descripcion = proyecto.descripcion,
            anio = proyecto.anio,
            estado = proyecto.estado,
            areaId = proyecto.idArea,
            areaNombre = area?.nombre.orEmpty(),
            integrantes = integrantes.map {
                Integrante(
                    id = it.idIntegrante,
                    nombre = it.nombre,
                    rol = it.rol,
                    especialidad = it.especialidad,
                    organizacionId = it.idOrganizacion
                )
            },
            capacidades = capacidades.map { it.nombre },
            publicaciones = publicaciones.map { it.titulo }
        )
    }

    private fun ActividadConArea.toDomain(): Actividad {
        return Actividad(
            id = actividad.idActividad,
            titulo = actividad.titulo,
            tipo = actividad.tipo,
            fecha = actividad.fecha,
            descripcion = actividad.descripcion,
            areaId = actividad.idArea,
            areaNombre = area?.nombre.orEmpty()
        )
    }
}
```

**Qué hace:** es la frontera entre datos y dominio. Traduce entidades a modelos simples y ofrece `Flow` a los ViewModels.

**Léelo así**

- `database: AppDatabase? = null` + `emptyFlow()` permiten tests sin base.
- `open class` y funciones `open` permiten fakes.
- `sanitizedQuery` convierte `""` en `null`, para que “sin búsqueda” signifique “sin filtro”.
- `map { it.toDomain() }` traduce. La UI nunca ve `id_area` ni `ProyectoEntity`.
- `toDomain()` aplana relaciones: integrantes completos, capacidades y publicaciones solo con nombre/título.
- `getConteos()` combina cinco `Flow`; cuando cambia cualquiera de las cinco tablas, recalcula.
- `getProyectoById` trae el grafo completo del proyecto en una consulta transaccional.

**Frase:** “El repositorio traduce Room a un modelo de dominio; la UI no sabe que existe SQLite.”

### 6.6 El corazón de la búsqueda: `ExplorarViewModel.kt`

`android/app/src/main/java/cl/duocuc/cimuc/viewmodel/ExplorarViewModel.kt`

```kotlin
package cl.duocuc.cimuc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.cimuc.data.repository.CimRepository
import cl.duocuc.cimuc.domain.model.AreaTematica
import cl.duocuc.cimuc.domain.model.Proyecto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.Normalizer

data class ExplorarUiState(
    val query: String = "",
    val selectedAreaId: Int? = null,
    val selectedAnio: Int? = null,
    val selectedEstado: String? = null,
    val areas: List<AreaTematica> = emptyList(),
    val aniosDisponibles: List<Int> = emptyList(),
    val proyectos: List<Proyecto> = emptyList(),
    val totalCoincidencias: Int = 0,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)

private data class FiltroCriterios(
    val query: String,
    val areaId: Int?,
    val anio: Int?,
    val estado: String?
)

class ExplorarViewModel(private val repository: CimRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedAreaId = MutableStateFlow<Int?>(null)
    private val _selectedAnio = MutableStateFlow<Int?>(null)
    private val _selectedEstado = MutableStateFlow<String?>(null)

    private val _filtros = combine(
        _query,
        _selectedAreaId,
        _selectedAnio,
        _selectedEstado
    ) { query, areaId, anio, estado ->
        FiltroCriterios(query, areaId, anio, estado)
    }

    val uiState: StateFlow<ExplorarUiState> = combine(
        _filtros,
        repository.getProyectos(),
        repository.getAreasTematicas(),
        repository.getAniosDisponibles()
    ) { filtros, todosProyectos, areas, anios ->

        val normalizedQuery = removeDiacritics(filtros.query.trim().lowercase())

        val filtrados = todosProyectos.filter { proyecto ->
            val matchesQuery = if (normalizedQuery.isEmpty()) {
                true
            } else {
                val tituloNorm = removeDiacritics(proyecto.titulo.lowercase())
                val descNorm = removeDiacritics(proyecto.descripcion.lowercase())
                val areaNorm = removeDiacritics(proyecto.areaNombre.lowercase())
                tituloNorm.contains(normalizedQuery) ||
                        descNorm.contains(normalizedQuery) ||
                        areaNorm.contains(normalizedQuery)
            }

            val matchesArea = filtros.areaId == null || proyecto.areaId == filtros.areaId
            val matchesAnio = filtros.anio == null || proyecto.anio == filtros.anio
            val matchesEstado = filtros.estado == null || proyecto.estado.equals(filtros.estado, ignoreCase = true)

            matchesQuery && matchesArea && matchesAnio && matchesEstado
        }

        ExplorarUiState(
            query = filtros.query,
            selectedAreaId = filtros.areaId,
            selectedAnio = filtros.anio,
            selectedEstado = filtros.estado,
            areas = areas,
            aniosDisponibles = anios,
            proyectos = filtrados,
            totalCoincidencias = filtrados.size,
            isLoading = false,
            isEmpty = filtrados.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExplorarUiState(isLoading = true)
    )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onAreaSelected(areaId: Int?) {
        _selectedAreaId.value = if (_selectedAreaId.value == areaId) null else areaId
    }

    fun onAnioSelected(anio: Int?) {
        _selectedAnio.value = if (_selectedAnio.value == anio) null else anio
    }

    fun onEstadoSelected(estado: String?) {
        _selectedEstado.value = if (_selectedEstado.value.equals(estado, ignoreCase = true)) null else estado
    }

    fun limpiarFiltros() {
        _query.value = ""
        _selectedAreaId.value = null
        _selectedAnio.value = null
        _selectedEstado.value = null
    }

    private fun removeDiacritics(input: String): String {
        val nfdNormalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(nfdNormalized, "")
    }
}
```

**Qué hace:** mantiene el estado de la pantalla Explorar y recalcula la lista cada vez que cambia la búsqueda o un filtro.

**Léelo así**

- `ExplorarUiState` contiene todo lo que la pantalla necesita en un objeto inmutable.
- Cuatro `MutableStateFlow` privados para los filtros. La UI no los toca: llama métodos.
- `_filtros` agrupa los cuatro criterios para simplificar el `combine` principal.
- El segundo `combine` junta criterios + proyectos + áreas + años. Cualquier cambio recalcula el estado.
- `normalizedQuery` aplica `trim`, `lowercase` y `removeDiacritics`. Lo mismo a título, descripción y área.
- `Normalizer.Form.NFD` separa letra y tilde; el regex borra las marcas diacríticas. En Kotlin el string lleva doble barra (`"\\p{...}"`).
- Los cuatro `matches*` se combinan con `&&`: los filtros son acumulativos.
- `onAreaSelected` hace toggle: tocar el chip activo lo quita.
- `stateIn(viewModelScope, WhileSubscribed(5000), initialValue)`:
  - ata el flujo al ciclo de vida del ViewModel;
  - deja de recolectar 5 segundos después de que la pantalla deja de observar;
  - entrega un valor inicial con `isLoading = true`.
- `selectedEstado` existe en la lógica, pero la UI del MVP solo muestra área y año.

**Frase:** “La búsqueda no consulta la base en cada tecla: los proyectos ya están en un `Flow` y el ViewModel recombina filtros en memoria; la normalización NFD es lo que permite buscar sin tildes.”

**Demo:** escribe `sismico` y luego `Sísmico`; ambos traen el mismo proyecto.

### 6.7 Validación pura: `ContactoValidator.kt`

`android/app/src/main/java/cl/duocuc/cimuc/validation/ContactoValidator.kt`

```kotlin
package cl.duocuc.cimuc.validation

// Validador puro del formulario de contacto: funciones sin dependencias de
// Android Views, Compose ni contexto (V2 / issue #12, regla de correo #8)
class ContactoValidator {

    fun validarNombre(nombre: String): ValidationResult {
        return if (nombre.trim().isBlank()) {
            ValidationResult.error("El nombre es obligatorio")
        } else if (nombre.trim().length < 3) {
            ValidationResult.error("El nombre debe tener al menos 3 caracteres")
        } else {
            ValidationResult.success()
        }
    }

    fun validarCorreo(correo: String): ValidationResult {
        val limpio = correo.trim()
        return when {
            limpio.isBlank() ->
                ValidationResult.error("El correo es obligatorio")
            !EMAIL_REGEX.matches(limpio) ->
                ValidationResult.error("Ingresa un correo válido, por ejemplo nombre@dominio.cl")
            else ->
                ValidationResult.success()
        }
    }

    fun validarOrganizacion(organizacion: String): ValidationResult =
        if (organizacion.trim().isBlank()) ValidationResult.error("La organización es obligatoria")
        else ValidationResult.success()

    fun validarMotivo(motivo: String): ValidationResult =
        if (motivo.trim().isBlank()) ValidationResult.error("El motivo es obligatorio")
        else ValidationResult.success()

    fun validarMensaje(mensaje: String): ValidationResult {
        return when {
            mensaje.isBlank() ->
                ValidationResult.error("El mensaje es obligatorio")
            mensaje.trim().length < 10 ->
                ValidationResult.error("El mensaje debe tener al menos 10 caracteres")
            else ->
                ValidationResult.success()
        }
    }

    // Valida el formulario completo y retorna el resultado por campo
    fun validarFormulario(
        nombre: String,
        correo: String,
        mensaje: String,
        organizacion: String = "",
        motivo: String = ""
    ): FormularioValidationResult {
        val nombreResult = validarNombre(nombre)
        val correoResult = validarCorreo(correo)
        val organizacionResult = validarOrganizacion(organizacion)
        val motivoResult = validarMotivo(motivo)
        val mensajeResult = validarMensaje(mensaje)
        val esValido = nombreResult.isValid && correoResult.isValid && organizacionResult.isValid &&
            motivoResult.isValid && mensajeResult.isValid
        return FormularioValidationResult(
            esValido = esValido,
            errorNombre = nombreResult.errorMessage,
            errorCorreo = correoResult.errorMessage,
            errorOrganizacion = organizacionResult.errorMessage,
            errorMotivo = motivoResult.errorMessage,
            errorMensaje = mensajeResult.errorMessage
        )
    }

    // Formato mínimo: algo@algo.dominio con TLD de al menos 2 letras (#8)
    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    }
}

data class FormularioValidationResult(
    val esValido: Boolean,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorOrganizacion: String? = null,
    val errorMotivo: String? = null,
    val errorMensaje: String? = null
)
```

**Qué hace:** valida cada campo y devuelve los cinco errores a la vez.

**Léelo así**

- No importa Android: es JUnit puro, sin emulador.
- Cada regla devuelve `ValidationResult` (`isValid` + `errorMessage?`).
- `validarNombre`: vacío, luego mínimo 3.
- `validarCorreo`: vacío, luego regex `usuario@dominio.tld` con TLD de 2+ letras.
- `validarOrganizacion` y `validarMotivo`: obligatorios.
- `validarMensaje`: vacío o menos de 10.
- `validarFormulario` agrega los cinco resultados; eso permite pintar cada campo por separado.
- La regla del mensaje aparece dos veces (condición y texto). Si cambias 10 → 25, edita ambas.
- `ContactoValidatorTest` cubre estos casos.

**Frase:** “Las reglas viven en una clase pura; la pantalla solo pinta los mensajes que recibe.”

### 6.8 El formulario como máquina de estados: `ContactoViewModel.kt`

Bloque 1, el contrato de estados:

```kotlin
sealed interface ContactoUiState {
    data class Formulario(
        val nombre: String = "",
        val correo: String = "",
        val organizacion: String = "",
        val motivo: String = "",
        val mensaje: String = "",
        val proyectoReferidoTitulo: String = "",
        val idProyectoReferido: Int = 0,
        val errorNombre: String? = null,
        val errorCorreo: String? = null,
        val errorOrganizacion: String? = null,
        val errorMotivo: String? = null,
        val errorMensaje: String? = null,
        val intentoEnviado: Boolean = false
    ) : ContactoUiState

    data class Enviando(
        val nombre: String, val correo: String, val organizacion: String,
        val motivo: String, val mensaje: String, val idProyectoReferido: Int
    ) : ContactoUiState

    data class Error(val mensaje: String, val formulario: Formulario) : ContactoUiState

    data class Exito(
        val solicitudId: Long,
        val nombre: String,
        val correo: String,
        val mensaje: String,
        val fecha: String,
        val proyectoReferidoTitulo: String
    ) : ContactoUiState
}
```

Bloque 2, el envío:

```kotlin
fun onEnviarClick() {
    val formulario = _uiState.value as? ContactoUiState.Formulario ?: return
    val resultado = validador.validarFormulario(
        formulario.nombre, formulario.correo, formulario.mensaje,
        formulario.organizacion, formulario.motivo
    )

    if (!resultado.esValido) {
        _uiState.value = formulario.copy(
            errorNombre = resultado.errorNombre,
            errorCorreo = resultado.errorCorreo,
            errorOrganizacion = resultado.errorOrganizacion,
            errorMotivo = resultado.errorMotivo,
            errorMensaje = resultado.errorMensaje,
            intentoEnviado = true
        )
        return
    }

    _uiState.value = ContactoUiState.Enviando(
        nombre = formulario.nombre,
        correo = formulario.correo,
        organizacion = formulario.organizacion,
        motivo = formulario.motivo,
        mensaje = formulario.mensaje,
        idProyectoReferido = formulario.idProyectoReferido
    )

    viewModelScope.launch {
        try {
            val id = contactoRepository.guardarSolicitud(
                nombre = formulario.nombre,
                correo = formulario.correo,
                mensaje = formulario.mensaje,
                idProyectoReferido = formulario.idProyectoReferido,
                organizacion = formulario.organizacion,
                motivo = formulario.motivo
            )
            if (id > 0) {
                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                _uiState.value = ContactoUiState.Exito(
                    solicitudId = id,
                    nombre = formulario.nombre,
                    correo = formulario.correo,
                    mensaje = formulario.mensaje,
                    fecha = fecha,
                    proyectoReferidoTitulo = formulario.proyectoReferidoTitulo
                )
            } else {
                _uiState.value = ContactoUiState.Error(
                    mensaje = "No se pudo registrar la solicitud",
                    formulario = formulario
                )
            }
        } catch (e: Exception) {
            _uiState.value = ContactoUiState.Error(
                mensaje = "Ocurrió un error al enviar la solicitud",
                formulario = formulario
            )
        }
    }
}
```

**Qué hace:** modela el formulario como una máquina de estados y guarda la solicitud en Room.

**Léelo así**

- Cuatro estados: **Formulario → Enviando → Exito | Error**. El `when` de la UI es exhaustivo.
- `Formulario` guarda valores y errores por campo. `copy()` crea un estado nuevo.
- `onEnviarClick` empieza con un cast seguro: si no está en `Formulario`, no hace nada.
- Valida antes de pasar a `Enviando`. Si falla, conserva los datos y agrega los cinco mensajes.
- Si es válido, pasa a `Enviando` y lanza la corrutina en `viewModelScope`.
- `guardarSolicitud` devuelve el id autogenerado. `id > 0` es éxito; `-1` significa que no había base (tests).
- En éxito se construye `Exito` con la fecha y el título del proyecto; la navegación observa ese estado y va a Confirmación.
- `catch` convierte cualquier excepción en `Error` sin romper la app.

Detalle: en estado `Error`, `actualizarFormulario` no acepta cambios porque solo opera sobre `Formulario`. Solo se alcanza si falla la persistencia. Si preguntan, reconócelo como limitación conocida; `reintentarDesdeError` existe pero la UI no lo invoca.

**Frase:** “El formulario es una máquina de estados explícita: la pantalla no decide si mostrar spinner o error, solo renderiza el estado que recibe.”

### 6.9 Una pantalla tonta: `ProyectoCard.kt`

`android/app/src/main/java/cl/duocuc/cimuc/ui/components/ProyectoCard.kt`

```kotlin
package cl.duocuc.cimuc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.cimuc.domain.model.Proyecto

// Tarjeta reutilizable de proyecto (RNF-08: mismos componentes en todas las pantallas)
@Composable
fun ProyectoCard(
    proyecto: Proyecto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp), // target táctil mínimo (RNF-02)
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("Proyecto") }
                )
                Text(
                    text = proyecto.areaNombre,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = proyecto.titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Año ${proyecto.anio} · ${proyecto.estado}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**Qué hace:** pinta un proyecto en una tarjeta y avisa cuando la tocan.

**Léelo así**

- Composable sin estado: recibe `Proyecto` y `onClick`. No consulta la base.
- `Card(onClick = ...)` de Material 3 trae ripple, foco y semántica.
- `heightIn(min = 48.dp)` cumple el target táctil mínimo.
- Colores y tipografías salen de `MaterialTheme`; cambiar `RojoUC` repinta todo.
- `modifier = Modifier` permite personalizar desde afuera.
- Se usa en `ExplorarScreen` dentro de un `LazyColumn` con `key`.
- `AssistChip(onClick = {})` es decorativo; no navega.

**Frase:** “La tarjeta no sabe de dónde viene el proyecto; solo lo pinta y avisa cuando la tocan.”

### 6.10 Navegación: `Screen.kt` + `BottomNavBar.kt` + `AppNavigation.kt`

`android/app/src/main/java/cl/duocuc/cimuc/ui/components/BottomNavBar.kt`

```kotlin
package cl.duocuc.cimuc.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

private data class DestinoBarra(
    val route: String,
    val etiqueta: String,
    val icono: ImageVector
)

private val destinos = listOf(
    DestinoBarra("inicio", "Inicio", Icons.Filled.Home),
    DestinoBarra("explorar", "Explorar", Icons.Filled.Search),
    DestinoBarra("actividades", "Actividades", Icons.Filled.Event)
)

// Barra inferior con 3 destinos, visible solo en P-01, P-02 y P-05 (N2 / issue #2)
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        destinos.forEach { destino ->
            NavigationBarItem(
                selected = currentRoute == destino.route,
                onClick = { onNavigate(destino.route) },
                icon = {
                    Icon(
                        imageVector = destino.icono,
                        contentDescription = destino.etiqueta
                    )
                },
                label = { Text(destino.etiqueta) }
            )
        }
    }
}
```

Bloque del `Scaffold` y las transiciones (de `AppNavigation.kt`):

```kotlin
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
        // ... rutas de P-01 a P-07
    }
}
```

Bloque de la ruta Inicio:

```kotlin
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
```

**Qué hace:** muestra la barra solo en tres pantallas, conserva el estado de las pestañas y anima las transiciones.

**Léelo así**

- `currentBackStackEntryAsState()` convierte la ruta actual en estado observable.
- La barra solo aparece en Inicio, Explorar y Actividades.
- **Inicio** usa `popUpTo(Inicio) { inclusive = true }` y no restaura estado. El comentario documenta un bug real: se veían “fotos” viejas del stack.
- Las otras pestañas usan `saveState` + `restoreState`: al volver a Explorar conservas búsqueda, filtros y scroll.
- `launchSingleTop` evita duplicar la misma pantalla.
- Las cuatro transiciones definen la dirección: avanzar entra desde la derecha; volver hace lo inverso. `tween(300)` + fade.
- `Modifier.padding(padding)` evita que el contenido quede debajo de la barra.
- `viewModel(factory = viewModelFactory)` crea el ViewModel atado a esa entrada del back stack; al salir, se limpia.
- `collectAsState()` suscribe la pantalla al `StateFlow`; sin eso, se queda con el valor inicial.
- `Screen.Actividades.create()` arma la ruta con el `areaId` opcional.

**Frase:** “La barra inferior se muestra solo en las tres pantallas de navegación principal; el resto es un flujo lineal que termina en la confirmación.”

---

<a id="7-modificar-en-vivo"></a>
## 7. Modificar en vivo

### 7.1 Qué significa “en vivo”

Hay dos formas de aplicar un cambio:

| Forma | Qué aplica | Cuándo usarla |
|---|---|---|
| **Live Edit** de Android Studio | Cambios dentro de funciones `@Composable` (textos, colores, layouts) | Demo rápida de UI |
| **Run / installDebug** | Cualquier cambio Kotlin, incluidos ViewModels, validadores, rutas y firmas | Lógica, datos y navegación |

Live Edit no cambia firmas de funciones ni lógica de negocio. Si editas un ViewModel, necesitas Run.

### 7.2 Tabla de cambios seguros

| Cambio | Archivo | Cómo se aplica | Tiempo |
|---|---|---|---|
| Color de marca | `ui/theme/Color.kt` | Live Edit | 10 s |
| Texto visible | `ui/screens/...` | Live Edit | 10 s |
| Regla de validación | `validation/ContactoValidator.kt` | Run | 30 s |
| Velocidad de animación | `navigation/AppNavigation.kt` | Run | 30 s |
| Datos de ejemplo | `data/local/seed/CimSeedData.kt` | Borrar datos + Run | 1 min |
| Búsqueda por integrante | `viewmodel/ExplorarViewModel.kt` | Run | 1–2 min |

### 7.3 Color de marca (10 segundos)

`android/app/src/main/java/cl/duocuc/cimuc/ui/theme/Color.kt`, línea 6:

```kotlin
val RojoUC = Color(0xFFB01F24)
```

Cámbialo por:

```kotlin
val RojoUC = Color(0xFF0B6E4F)
```

Todo lo que usa `MaterialTheme.colorScheme.primary` cambia: botones, chips, contadores, indicador de la barra. Es el cambio más visible con menos riesgo.

### 7.4 Texto visible (10 segundos)

`android/app/src/main/java/cl/duocuc/cimuc/ui/screens/inicio/InicioScreen.kt`, línea 73:

```kotlin
text = "Bienvenido al Centro UC de Innovación en Madera. Explora proyectos, " +
```

O `ui/screens/contacto/ContactoScreen.kt`, línea 210:

```kotlin
Text("Enviar solicitud")
```

Cambia el texto y muestra que Compose es declarativo.

### 7.5 Regla de validación (30 segundos, requiere Run)

`android/app/src/main/java/cl/duocuc/cimuc/validation/ContactoValidator.kt`, líneas 41–42:

```kotlin
mensaje.trim().length < 10 ->
    ValidationResult.error("El mensaje debe tener al menos 10 caracteres")
```

Cambia ambos `10` por `25`:

```kotlin
mensaje.trim().length < 25 ->
    ValidationResult.error("El mensaje debe tener al menos 25 caracteres")
```

Prueba con un mensaje corto. Refuerza que la regla vive fuera de la vista y tiene tests.

### 7.6 Animación exagerada (30 segundos, requiere Run)

En `android/app/src/main/java/cl/duocuc/cimuc/navigation/AppNavigation.kt`, reemplaza todas las apariciones de:

```kotlin
tween(300)
```

por:

```kotlin
tween(900)
```

La navegación se vuelve dramáticamente lenta y se nota muchísimo. En Android Studio puedes usar “Replace All” dentro del archivo.

### 7.7 Dato nuevo (la trampa del seed)

Editar un título en `CimSeedData.kt` **no se verá** si la base ya existe: `onCreate` solo corre al crearla. Para aplicar:

```bash
adb shell pm clear cl.duocuc.cimuc
adb shell am start -n cl.duocuc.cimuc/.MainActivity
```

O en el emulador: Ajustes → Apps → CIM UC → Almacenamiento → Borrar datos.

Esto también borra las solicitudes de contacto. Hazlo **antes** de la presentación, no durante.

Al reabrir, la base se crea y se precarga. Tarda alrededor de un segundo; si ves 0 al inicio, espera.

### 7.8 Búsqueda por integrante (opcional, 1–2 minutos)

En `ExplorarViewModel.kt`, dentro del `else` de `matchesQuery`, agrega:

```kotlin
val integrantesNorm = proyecto.integrantes.joinToString(" ") {
    removeDiacritics(it.nombre.lowercase())
}
```

y suma la comparación:

```kotlin
tituloNorm.contains(normalizedQuery) ||
        descNorm.contains(normalizedQuery) ||
        areaNorm.contains(normalizedQuery) ||
        integrantesNorm.contains(normalizedQuery)
```

El bloque completo queda así:

```kotlin
val matchesQuery = if (normalizedQuery.isEmpty()) {
    true
} else {
    val tituloNorm = removeDiacritics(proyecto.titulo.lowercase())
    val descNorm = removeDiacritics(proyecto.descripcion.lowercase())
    val areaNorm = removeDiacritics(proyecto.areaNombre.lowercase())
    val integrantesNorm = proyecto.integrantes.joinToString(" ") {
        removeDiacritics(it.nombre.lowercase())
    }
    tituloNorm.contains(normalizedQuery) ||
            descNorm.contains(normalizedQuery) ||
            areaNorm.contains(normalizedQuery) ||
            integrantesNorm.contains(normalizedQuery)
}
```

Luego busca el apellido de un integrante y aparece su proyecto. Es un “wow” extra, pero no lo intentes si estás apurado.

### 7.9 Antes de tocar código

```bash
git status
git diff
```

Si algo sale mal durante la demo, puedes volver con:

```bash
git checkout -- android/app/src/main/java/cl/duocuc/cimuc/ui/theme/Color.kt
```

No hagas `git reset --hard` con cambios que quieras conservar.

---

<a id="8-guion-de-la-demo"></a>
## 8. Guion de la demo

Duración objetivo: **4 minutos**. Ten la app abierta en Inicio y Android Studio en `Color.kt`.

### 8.1 Antes de empezar

- Emulador encendido y app instalada.
- Datos borrados para que el seed esté limpio.
- Notificaciones silenciadas.
- Fuente del sistema en tamaño normal.
- Android Studio con `Color.kt` y `ExplorarViewModel.kt` abiertos.
- APK de respaldo en `android/app/build/outputs/apk/debug/app-debug.apk`.

### 8.2 Paso 1 — Inicio (30 s)

Muestra los contadores.

> “La pantalla de Inicio no calcula nada: pide cinco conteos al repositorio. Room los entrega como `Flow` y Compose los observa. Si la base cambia, los números se actualizan solos.”

### 8.3 Paso 2 — Explorar (60 s)

1. Toca el buscador.
2. Escribe `sismico` **sin tilde**.
3. Aparece “Monitoreo Sísmico…”.

> “La búsqueda normaliza con `Normalizer` NFD: separa la tilde de la letra y la elimina. Por eso `sismico` encuentra `Sísmico`. Todo ocurre en el ViewModel; no consultamos la base en cada tecla.”

4. Selecciona un área y un año.
5. Muestra el contador y los chips activos.
6. Toca “Limpiar todo”.

> “Los filtros son `StateFlow` que se combinan; cada cambio recalcula el estado de la pantalla.”

### 8.4 Paso 3 — Detalle y relacionados (45 s)

1. Abre un proyecto.
2. Muestra año, estado, área y descripción.
3. Toca “Ver contenidos relacionados”.
4. Muestra integrantes, capacidades y publicaciones.

> “Un proyecto se relaciona con integrantes, capacidades y publicaciones mediante tablas puente. Room lo resuelve con `@Relation` y `Junction` en una sola consulta transaccional.”

### 8.5 Paso 4 — Contacto (60 s)

1. Toca “Contactar a CIM UC”.
2. Escribe un correo inválido (`abc`).
3. Toca “Enviar solicitud”.
4. Muestra el error por campo.
5. Corrige el correo sin volver a escribir todo.

> “Los datos no se borran al fallar: el estado conserva el formulario y solo agrega los errores. La validación es una clase pura, testeable sin Android.”

6. Completa y envía.

### 8.6 Paso 5 — Confirmación (30 s)

> “La solicitud quedó guardada en Room. La confirmación la lee por id y muestra el proyecto referido. Si cierras la app y vuelves, la solicitud sigue ahí.”

### 8.7 Paso 6 — Cambio en vivo (45 s)

1. Ve a `Color.kt`.
2. Cambia `RojoUC` a `0xFF0B6E4F`.
3. Vuelve al emulador.
4. Muestra cómo cambian botones, chips y contadores.

> “Un solo token de color repinta toda la app porque las pantallas usan `MaterialTheme`, no colores hardcodeados.”

Si Live Edit no aplica, no insistas: vuelve a la app ya compilada y explica el código.

### 8.8 Cierre (15 s)

> “En resumen: Room guarda, el repositorio traduce, el ViewModel decide y Compose dibuja. Cada capa se puede probar y cambiar por separado.”

---

<a id="9-preguntas-difíciles"></a>
## 9. Preguntas difíciles

### ¿Qué es `by lazy`?

Inicialización diferida: el bloque se ejecuta la primera vez que se lee la propiedad y el resultado se guarda. No es una petición ni un `fetch`. En `CimApplication` retrasa la creación de la base y los repositorios. Es thread-safe por defecto.

### ¿Por qué usaron `Flow` y no `LiveData`?

Porque Compose consume `StateFlow` con `collectAsState`, los flujos se combinan con `combine` y el código queda en corrutinas. `LiveData` es de la era Views y no se integra tan natural con Compose.

### ¿Por qué `stateIn` con `WhileSubscribed(5000)`?

Para que la consulta deje de recolectarse cuando la pantalla no está visible y se reenganche al volver. Los 5 segundos evitan cancelar y recrear por cambios rápidos de configuración. `StateFlow` siempre tiene un valor actual, así que la UI nunca se queda sin estado.

### ¿Por qué no usaron Hilt?

El MVP tiene dos repositorios y cinco ViewModels. Una factory manual es suficiente, se entiende en una lectura y no agrega dependencias. Hilt sería razonable al crecer.

### ¿Dónde se filtra realmente?

En `ExplorarViewModel`, en memoria, después de recibir todos los proyectos. El DAO también tiene una consulta con filtros opcionales, pero la pantalla llama `getProyectos()` sin argumentos. Con 11 proyectos es instantáneo; al crecer, se movería a SQL o FTS.

### ¿Por qué la búsqueda ignora tildes y el SQL no?

Porque `LIKE` en SQLite no normaliza diacríticos. El ViewModel usa `Normalizer.Form.NFD` y elimina las marcas. Es lógica de presentación, no de base de datos.

### ¿Qué pasa si cambio el seed y no veo el cambio?

`RoomDatabase.Callback.onCreate` solo corre al crear la base. Hay que borrar los datos de la app o desinstalarla. Es la trampa clásica de la demo.

### ¿Qué pasa si subo la versión de la base sin migración?

La app falla al abrir con “A migration from X to Y was required but not found”. Room no adivina el cambio. Se agrega una `Migration` con el SQL o se usa `fallbackToDestructiveMigration` (que borra datos y aquí no se usa).

### ¿Qué pasa al girar el teléfono?

El ViewModel sobrevive a cambios de configuración, así que el estado y los datos se conservan. La navegación de Compose también guarda el back stack. `remember` solo se perdería; por eso el estado importante no vive en `remember`.

### ¿Cómo se prueba sin emulador?

Los tests unitarios usan repositorios falsos (`object : CimRepository()`) y `StandardTestDispatcher`. El validador es JUnit puro. Para Room real se necesitan tests instrumentados o una base en memoria.

### ¿Qué pasa si borro un proyecto?

Las claves foráneas están con `onDelete = CASCADE`: se borran sus tablas puente y también sus solicitudes de contacto. Es una decisión de integridad; si quisieras conservar las solicitudes, habría que usar `SET_NULL` y hacer el campo nullable.

### ¿Qué mejorarías?

- Filtrado en SQL/FTS al crecer el catálogo.
- `collectAsStateWithLifecycle()` en vez de `collectAsState()`.
- `flatMapLatest` en `DetalleViewModel` para cancelar la carga anterior.
- Seed dentro de una transacción.
- Inyección con Hilt.
- Exponer el filtro por estado.
- Un recurso nativo real (compartir la solicitud con `Intent`).
- Fecha como `Long`/`Instant` en vez de `String`.
- Pantalla de administración de solicitudes.

### ¿Por qué dos repositorios?

`CimRepository` maneja el catálogo; `ContactoRepository` maneja solicitudes. Separar evita que el formulario dependa de todo el modelo de contenido y facilita tests.

### ¿Por qué el repositorio acepta `database = null`?

Para tests: sin base devuelve `emptyFlow()`. También permite construir el repositorio antes de que la base esté lista. Es una decisión de testabilidad.

### ¿Por qué `id` es `Int` y `solicitudId` es `Long`?

Los ids del catálogo son `Int` porque vienen del seed y son pequeños. `insert` de Room devuelve `Long` (rowid de SQLite), y ese valor se usa para navegar a la confirmación. Es una inconsistencia menor; se podría unificar.

---

<a id="10-chuleta-y-comandos"></a>
## 10. Chuleta y comandos

### 10.1 Mapa de archivos

| Archivo | Rol | Qué decir |
|---|---|---|
| `MainActivity.kt` | Entrada Compose | “Enciende la UI y entrega dependencias.” |
| `CimApplication.kt` | Composition root | “Construye base y repositorios una vez.” |
| `navigation/Screen.kt` | Rutas | “Las 7 rutas viven en un archivo.” |
| `navigation/AppNavigation.kt` | Grafo y transiciones | “Barra condicional y animaciones.” |
| `ui/components/BottomNavBar.kt` | Barra inferior | “Tres destinos.” |
| `ui/components/ProyectoCard.kt` | Tarjeta reutilizable | “Composable sin estado.” |
| `ui/screens/*` | Pantallas | “Reciben estado y emiten eventos.” |
| `ui/theme/Color.kt` | Paleta | “Un token repinta todo.” |
| `viewmodel/ExplorarViewModel.kt` | Búsqueda y filtros | “El corazón reactivo.” |
| `viewmodel/ContactoViewModel.kt` | Formulario | “Máquina de estados.” |
| `viewmodel/DetalleViewModel.kt` | Detalle | “Carga un proyecto por id.” |
| `viewmodel/ActividadesViewModel.kt` | Actividades | “Filtro por área.” |
| `validation/ContactoValidator.kt` | Reglas | “Lógica pura y testeable.” |
| `data/repository/CimRepository.kt` | Catálogo | “Traduce Entity a dominio.” |
| `data/repository/ContactoRepository.kt` | Solicitudes | “Guarda en Room.” |
| `data/local/AppDatabase.kt` | Room | “11 tablas, versión 2, seed.” |
| `data/local/dao/ProyectoDao.kt` | Consultas | “SQL validado al compilar.” |
| `data/local/entity/*` | Tablas | “Entidades y claves foráneas.” |
| `data/local/seed/CimSeedData.kt` | Datos demo | “11 proyectos y relaciones.” |
| `CimViewModelFactory.kt` | Inyección | “Factory manual.” |

### 10.2 Comandos

```bash
# Compilar
cd android
./gradlew assembleDebug

# Instalar en el emulador conectado
./gradlew installDebug

# Abrir la app
adb shell am start -n cl.duocuc.cimuc/.MainActivity

# Ejecutar tests unitarios
./gradlew testDebugUnitTest

# Borrar datos y dejar la base limpia (re-siembra al abrir)
adb shell pm clear cl.duocuc.cimuc

# Ver dispositivos conectados
adb devices

# Ver logs de la app
adb logcat | grep -i cimuc
```

El APK queda en:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

### 10.3 Checklist previo

- [ ] `adb devices` muestra el emulador.
- [ ] La app abre en Inicio.
- [ ] Los contadores muestran números (no 0).
- [ ] La búsqueda `sismico` encuentra un proyecto.
- [ ] El formulario muestra errores con correo inválido.
- [ ] El formulario envía y llega a Confirmación.
- [ ] `Color.kt` abierto en Android Studio.
- [ ] `ExplorarViewModel.kt` abierto como respaldo.
- [ ] Notificaciones silenciadas.
- [ ] APK de respaldo localizado.

### 10.4 Plan B

| Problema | Qué hacer |
|---|---|
| Live Edit no aplica | Seguir con la app ya compilada; explicar el código. |
| El emulador se cuelga | Reiniciarlo o usar otro AVD; instalar el APK de respaldo. |
| Los contadores están en 0 | Esperar 2 segundos; el seed es asíncrono. |
| El seed está incompleto | `pm clear` y reabrir; no hacerlo en medio de la demo. |
| El formulario no envía | Revisar los cinco campos; los errores indican cuál falta. |
| Se borró el código por accidente | `git checkout -- <archivo>`; no usar `reset --hard`. |

---

<a id="anexo-a-cómo-agregar-un-campo-nuevo"></a>
## Anexo A: cómo agregar un campo nuevo

Ejemplo: agregar `modalidad` a un proyecto. Es la mejor respuesta a “¿cómo lo modificarías en vivo?”.

1. **Entidad** (`ProyectoEntity.kt`):

```kotlin
@ColumnInfo(name = "modalidad", defaultValue = "''")
val modalidad: String = ""
```

2. **Versión y migración** (`AppDatabase.kt`):

```kotlin
version = 3
```

```kotlin
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE proyecto ADD COLUMN modalidad TEXT NOT NULL DEFAULT ''")
    }
}
```

y registrarla:

```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
```

3. **Modelo de dominio** (`dominio/model/Proyecto.kt`): agrega `val modalidad: String = ""`.

4. **Mapper** (`CimRepository.toDomain()`): `modalidad = proyecto.modalidad`.

5. **UI** (`DetalleScreen.kt` o `ProyectoCard.kt`): muestra `proyecto.modalidad`.

6. **Seed** (`CimSeedData.kt`): opcional, asigna valores a los proyectos.

7. **Prueba**: agrega un caso en `ExplorarViewModelTest` o en el test del seed.

Si olvidas el paso 2, la app falla al abrir. Si olvidas el paso 4, el campo existe en la base pero la UI lo ve vacío.

---

<a id="anexo-b-glosario"></a>
## Anexo B: glosario

| Término | Significado |
|---|---|
| **Entity** | Clase que representa una tabla. |
| **DAO** | Interfaz con consultas y escrituras; Room genera la implementación. |
| **Repository** | Capa que abstrae el origen de datos y traduce a dominio. |
| **ViewModel** | Guarda el estado de la pantalla y ejecuta la lógica; sobrevive a rotaciones. |
| **UiState** | Objeto inmutable con todo lo que la pantalla necesita. |
| **Composable** | Función que emite UI. |
| **Recomposición** | Volver a ejecutar un composable cuando cambia el estado que lee. |
| **State hoisting** | Subir el estado para que la pantalla sea tonta y reutilizable. |
| **Flow** | Flujo de datos asíncrono y frío; se ejecuta al recolectarse. |
| **StateFlow** | Flujo caliente con un valor actual; ideal para UI. |
| **Cold / hot** | Frío: no emite hasta que alguien lo escucha. Caliente: siempre tiene un valor y emite a los suscriptores. |
| **KSP** | Procesador de símbolos de Kotlin; Room genera código con él. |
| **Migración** | Cambio versionado del esquema de la base. |
| **Seed** | Datos iniciales de demostración. |
| **CrossRef** | Tabla puente para relaciones muchos-a-muchos. |
| **CASCADE** | Al borrar el padre, se borran los hijos. |
| **Singleton** | Una sola instancia de una clase. |
| **DI** | Inyección de dependencias: pasar objetos en vez de crearlos dentro. |
| **Live Edit** | Aplicación en caliente de cambios de Compose desde Android Studio. |

---

> Si algo de esta guía no coincide con el código, manda el archivo y lo actualizo. La versión de referencia es la rama `main` actual.
