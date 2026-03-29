package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class FitnessAppUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLoginAndNavigation() {
        // We use try-catch or conditional check to handle if we are already logged in
        val loginButton = composeTestRule.onAllNodesWithText("Login")
        
        if (loginButton.fetchSemanticsNodes().isNotEmpty()) {
            // We are on login screen, perform login
            loginButton[0].performClick()
        }

        // Now we should be on Home Screen
        composeTestRule.onNodeWithText("Daily Progress").assertIsDisplayed()
        
        // Test navigation to Add Activity
        composeTestRule.onNodeWithText("Add Activity").performClick()
        composeTestRule.onNodeWithText("Log Activity").assertIsDisplayed()
        
        // Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Test navigation to Water Tracker
        composeTestRule.onNodeWithText("Hydration").performClick()
        composeTestRule.onNodeWithText("Water Tracker").assertIsDisplayed()
        
        // Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Test navigation to Calories (Food Tracker)
        composeTestRule.onNodeWithText("Calories").performClick()
        composeTestRule.onNodeWithText("Food Tracker").assertIsDisplayed()
    }

    @Test
    fun testAddActivityFlow() {
        // Ensure we are on Home (skip login if needed)
        val loginButton = composeTestRule.onAllNodesWithText("Login")
        if (loginButton.fetchSemanticsNodes().isNotEmpty()) {
            loginButton[0].performClick()
        }

        // Navigate to Add Activity
        composeTestRule.onNodeWithText("Add Activity").performClick()
        
        // Fill details
        composeTestRule.onNodeWithText("Activity Title").performTextIntegument("Morning Run")
        // Note: performTextReplacement is often better
        // composeTestRule.onNodeWithText("Activity Title").performTextReplacement("Morning Run")
        
        // Steps and Calories
        composeTestRule.onNodeWithText("Steps").performTextReplacement("5000")
        composeTestRule.onNodeWithText("Calories").performTextReplacement("350")
        
        // Save
        composeTestRule.onNodeWithText("Log Activity").performClick()
        
        // Verify we are back on home and maybe check if logs updated (hard to check exact values without stable state)
        composeTestRule.onNodeWithText("Daily Progress").assertIsDisplayed()
    }
}

// Extension to avoid confusion if performTextIntegument was a typo in my head, using performTextReplacement
fun SemanticsNodeInteraction.performTextIntegument(text: String) = performTextReplacement(text)
