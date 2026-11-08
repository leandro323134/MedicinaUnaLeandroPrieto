package py.una.medstudy

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class StudyPalette(val label: String, val light: Long, val dark: Long, val container: Long) {
    Forest("Verde bosque", 0xFF146550, 0xFF84D7BB, 0xFFAEF2D9),
    Ocean("Azul océano", 0xFF245C9D, 0xFFA5C8FF, 0xFFD4E4FF),
    Plum("Violeta", 0xFF6B4994, 0xFFDDB9FF, 0xFFEFDCFF);
}

enum class StudyAppearance(val label: String) {
    System("Sistema"), Light("Claro"), Dark("Oscuro");
}

@Composable
fun StudyTheme(palette: StudyPalette, appearance: StudyAppearance, content: @Composable () -> Unit) {
    val dark = when (appearance) {
        StudyAppearance.System -> isSystemInDarkTheme()
        StudyAppearance.Light -> false
        StudyAppearance.Dark -> true
    }
    val scheme = if (dark) {
        darkColorScheme(
            primary = Color(palette.dark),
            onPrimary = Color(0xFF141418),
            primaryContainer = Color(palette.light),
            onPrimaryContainer = Color.White,
            secondaryContainer = Color(palette.light),
            onSecondaryContainer = Color.White,
        )
    } else {
        lightColorScheme(
            primary = Color(palette.light),
            onPrimary = Color.White,
            primaryContainer = Color(palette.container),
            onPrimaryContainer = Color(0xFF141418),
            secondaryContainer = Color(palette.container),
            onSecondaryContainer = Color(0xFF141418),
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
