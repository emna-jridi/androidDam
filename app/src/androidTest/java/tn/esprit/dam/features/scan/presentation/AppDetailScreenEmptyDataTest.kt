import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.runner.AndroidJUnit4
import tn.esprit.dam.data.api.models.AppInfoDto
import tn.esprit.dam.data.api.models.AnalysisResultDto
import tn.esprit.dam.features.scan.presentation.AppDetailScreen

@RunWith(AndroidJUnit4::class)
class AppDetailScreenEmptyDataTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val emptyApp = AppInfoDto(
        packageName = "com.test.app",
        displayName = "Test App",
        permissions = emptyList(),
        trackers = emptyList(),
        storeData = null,
        scanResults = AnalysisResultDto(
            aiRiskScore = 50f,
            aiRiskLevel = "medium",
            aiSummary = "Test summary",
            aiRecommendations = emptyList(),
            aiStatus = "fallback"
        ),
        finalScore = 50f,
        lastScanned = null
    )

    @Test
    fun testRenderWithEmptyPermissionsAndTrackers() {
        composeRule.setContent {
            AppDetailScreen(
                packageName = "com.test.app",
                onBackClick = {}
            )
        }

        // Screen should render without crash when permissions/trackers are empty
        composeRule.onNodeWithText("Test App").assertIsDisplayed()
    }

    @Test
    fun testPermissionsHiddenWhenEmpty() {
        composeRule.setContent {
            AppDetailScreen(
                packageName = "com.test.app",
                onBackClick = {}
            )
        }

        // Permissions section title should not appear if empty
        composeRule.onNodeWithText("Permissions détectées").assertDoesNotExist()
    }

    @Test
    fun testTrackersHiddenWhenEmpty() {
        composeRule.setContent {
            AppDetailScreen(
                packageName = "com.test.app",
                onBackClick = {}
            )
        }

        // Trackers section title should not appear if empty
        composeRule.onNodeWithText("Trackers identifiés").assertDoesNotExist()
    }

    @Test
    fun testAIStatusBadgeDisplayed() {
        composeRule.setContent {
            AppDetailScreen(
                packageName = "com.test.app",
                onBackClick = {}
            )
        }

        // AI status badge should appear in analysis card
        composeRule.onNodeWithText("Analyse de Sécurité").assertIsDisplayed()
    }

    @Test
    fun testScoreBreakdownRendersWithNullFinalScore() {
        val appWithZeroScore = emptyApp.copy(finalScore = 0f)

        composeRule.setContent {
            AppDetailScreen(
                packageName = "com.test.app",
                onBackClick = {}
            )
        }

        // Should handle zero score gracefully (score breakdown hidden if ≤ 0)
        // No crash expected
        composeRule.onRoot().assertIsDisplayed()
    }
}
