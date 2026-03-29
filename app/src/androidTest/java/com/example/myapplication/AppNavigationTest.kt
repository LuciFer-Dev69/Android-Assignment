package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun handleLoginIfPresent() {
        val loginButton = composeTestRule.onAllNodesWithText("Login")
        if (loginButton.fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Email").performTextInput("admin@gmail.com")
            composeTestRule.onNodeWithText("Password").performTextInput("admin123")
            composeTestRule.onNodeWithText("Login").performClick()
        }
        
        // Wait for the Home screen to load
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testNavigateToActivityHistory() {
        handleLoginIfPresent()

        // 1. Click on the Activities tab
        composeTestRule.onAllNodesWithText("Activities")
            .filterToOne(hasClickAction())
            .performClick()
            
        // 2. Check if the History header appears
        composeTestRule.onNodeWithText("Activity History").assertIsDisplayed()
    }

    @Test
    fun testNavigateToProfileAndVerifySettings() {
        handleLoginIfPresent()

        // 1. Click the "Profile" tab
        composeTestRule.onAllNodesWithText("Profile")
            .filterToOne(hasClickAction())
            .performClick()
        
        // 2. Verify that Settings are visible
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark Mode").assertIsDisplayed()
    }
}
