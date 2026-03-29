package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*

class CalorieLogicTest {
    @Test
    fun testBurnedCaloriesCalculation() {
        val steps = 1000
        val factor = 0.04
        val expected = 40
        val actual = (steps * factor).toInt()
        assertEquals("The calorie calculation is incorrect!", expected, actual)
    }
}
