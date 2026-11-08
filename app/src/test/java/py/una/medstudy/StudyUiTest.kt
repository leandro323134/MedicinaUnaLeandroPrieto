package py.una.medstudy

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class StudyUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun navigationThemeAndReviewEntryPointsWork() {
        val model = ViewModelProvider(compose.activity)[StudyModel::class.java]
        compose.waitUntil(20000) { !model.loading && model.cards.value.size == 233 }
        compose.onNodeWithContentDescription("Ajustes").performClick()
        compose.onNodeWithText("Colores · Verde bosque").performClick()
        compose.onNodeWithText("Azul océano").performClick()
        compose.onNodeWithText("Colores · Azul océano").assertIsDisplayed()
        compose.onNodeWithContentDescription("Materias").performClick()
        compose.onNodeWithText("Buscar pregunta, respuesta o tema").performTextInput("pírrica")
        compose.onNodeWithText("1 tarjetas · 1 disponibles").assertExists()
        compose.onNodeWithText("Repasar selección").performClick()
        compose.onNodeWithText("Mostrar respuesta").performScrollTo().performClick()
        compose.onNodeWithText("Pírrica").assertExists()
        compose.onNodeWithText("El resultado nominal es favorable, pero las pérdidas lo vuelven poco provechoso.").assertExists()
        compose.onNodeWithContentDescription("Progreso").performClick()
        compose.onNodeWithText("Tu evolución").assertIsDisplayed()
    }
}
