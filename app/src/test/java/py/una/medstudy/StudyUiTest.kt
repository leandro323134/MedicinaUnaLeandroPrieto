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
        val model = compose.runOnIdle { ViewModelProvider(compose.activity)[StudyModel::class.java] }
        // La importación termina en IO y publica su resultado en el looper principal.
        // runOnIdle permite procesar esa publicación antes de leer el estado.
        compose.waitUntil(20000) { compose.runOnIdle { !model.loading } }
        org.junit.Assert.assertEquals("", model.notice)
        compose.waitUntil(20000) {
            compose.runOnIdle { model.cards.value.any { it.id.startsWith("programa27-") } }
        }
        org.junit.Assert.assertEquals(model.notice, 311, model.bank.size)
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
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Ajustar meta diaria", substring = true))
        compose.onNodeWithText("Ajustar meta diaria", substring = true).assertExists()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Repasar errores"))
        compose.onNodeWithText("Repasar errores").performClick()
        compose.onNodeWithText("Repaso de errores").assertExists()
        compose.onNodeWithText("Todavía no hay errores para esta selección.").assertExists()
    }
}
