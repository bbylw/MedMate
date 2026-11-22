package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.data.DrugInteractionChecker
import com.example.data.Medication
import com.example.ui.components.EvidenceHeroBanner
import com.example.ui.components.RegimenInteractionAlertBanner
import com.example.ui.theme.MedMateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun heroBannerAndInteractionAlertRenderCorrectly() {
    val conflictingMeds = listOf(
      Medication(name = "阿司匹林肠溶片", genericName = "Aspirin", dosage = "100 mg (1片)", time = "07:30"),
      Medication(name = "布洛芬缓释胶囊", genericName = "Ibuprofen", dosage = "300 mg (1粒)", time = "12:30")
    )
    val alerts = DrugInteractionChecker.analyzeRegimen(conflictingMeds)

    composeTestRule.setContent {
      MedMateTheme {
        androidx.compose.foundation.layout.Column {
          EvidenceHeroBanner(
            medications = conflictingMeds,
            currentMinutesOfDay = 8 * 60,
            onShowEbmManifesto = {},
            onMarkAllTaken = {}
          )
          RegimenInteractionAlertBanner(alerts = alerts)
        }
      }
    }

    composeTestRule.onNodeWithTag("evidence_hero_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("interaction_alert_banner").assertIsDisplayed()
  }
}
