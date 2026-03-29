package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class AuthUITest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testSignupAndLoginFlow() {
        val randomNum = Random.nextInt(1000, 9999)
        val testUser = "user$randomNum"
        val testEmail = "test$randomNum@gmail.com"
        val testPass = "password123"

        // 1. Navigate from Login to Signup
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // 2. Fill Signup Form
        composeTestRule.onNodeWithText("Username").performTextInput(testUser)
        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPass)
        composeTestRule.onNodeWithText("Confirm Password").performTextInput(testPass)

        // 3. Click Sign Up Button
        // Use a more specific matcher to find the button
        composeTestRule.onNode(hasText("Sign Up") and hasClickAction()).performClick()

        // 4. Verify we are on Home Screen
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }

        // 5. Logout
        // Use content descriptions which are more reliable for icons
        composeTestRule.onNodeWithContentDescription("Profile").performClick()
        composeTestRule.onNodeWithContentDescription("Logout").performClick()

        // 6. Test Login with newly created user
        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPass)
        composeTestRule.onNodeWithText("Login").performClick()

        // 7. Final Verification
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }

    @Test
    fun testAdminLoginDirectly() {
        // Test hardcoded admin account
        composeTestRule.onNodeWithText("Email").performTextInput("admin@gmail.com")
        composeTestRule.onNodeWithText("Password").performTextInput("admin123")
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Admin").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Admin").assertIsDisplayed()
    }
}
