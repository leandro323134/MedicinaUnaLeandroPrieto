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
        compose.waitUntil(20000) { !model.loading && model.cards.value.any { it.id.startsWith("programa27-") } }
        compose.onNodeWithText("Ajustes").performClick()
        compose.onNodeWithText("Colores · Verde bosque").performClick()
        compose.onNodeWithText("Azul océano").performClick()
        compose.onNodeWithText("Colores · Azul océano").assertIsDisplayed()
        compose.onNodeWithText("Materias").performClick()
        compose.onNodeWithText("Buscar pregunta, respuesta o tema").performTextInput("pírrica")
        compose.onNodeWithText("1 tarjetas · 1 disponibles").assertExists()
        compose.onNodeWithText("Repasar selección").performScrollTo().performClick()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Mostrar respuesta"))
        compose.onNodeWithText("Mostrar respuesta").performClick()
        compose.onNodeWithText("Pírrica").assertExists()
        compose.onNodeWithText("El resultado nominal es favorable, pero las pérdidas lo vuelven poco provechoso.").assertExists()
        compose.onNodeWithText("Progreso").performClick()
        compose.onNodeWithText("Tu evolución").assertIsDisplayed()
    }

    @Test fun homeOpensErrorPracticeAndExposesEditableGoal() {
        val model = ViewModelProvider(compose.activity)[StudyModel::class.java]
        compose.waitUntil(20000) { !model.loading && model.bank.isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Ajustar meta diaria", substring = true))
        compose.onNodeWithText("Ajustar meta diaria", substring = true).assertExists()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Repasar errores"))
        compose.onNodeWithText("Repasar errores").performClick()
        compose.onNodeWithText("Repaso de errores").assertExists()
        compose.onNodeWithText("Todavía no hay errores para esta selección.").assertExists()
    }
}
