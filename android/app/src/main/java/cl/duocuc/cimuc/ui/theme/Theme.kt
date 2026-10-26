package cl.duocuc.cimuc.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CimColorScheme = lightColorScheme(
    primary = RojoUC,
    onPrimary = BlancoPuro,
    primaryContainer = Arena,
    onPrimaryContainer = Grafito,
    secondary = Madera,
    onSecondary = BlancoPuro,
    secondaryContainer = MaderaClara,
    onSecondaryContainer = Grafito,
    tertiary = ExitoVerde,
    onTertiary = BlancoPuro,
    background = GrisFondo,
    onBackground = Grafito,
    surface = BlancoPuro,
    onSurface = Grafito,
    surfaceVariant = Arena,
    onSurfaceVariant = GrisNeutro,
    error = ErrorRojo,
    onError = BlancoPuro,
    outline = GrisNeutro
)

// Espaciados en múltiplos de 8dp (RNF-08)
private val CimShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun CimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CimColorScheme,
        typography = CimTypography,
        shapes = CimShapes,
        content = content
    )
}
