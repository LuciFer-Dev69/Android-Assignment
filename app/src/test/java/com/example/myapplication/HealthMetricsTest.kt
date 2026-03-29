package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*

class HealthMetricsTest {

    @Test
    fun testBMICalculation() {
        val weightKg = 70.0
        val heightCm = 175.0
        val heightM = heightCm / 100
        val expectedBmi = 22.85
        
        val actualBmi = weightKg / (heightM * heightM)
        
        // Assert with a delta of 0.1 for rounding
        assertEquals(expectedBmi, actualBmi, 0.1)
    }

    @Test
    fun testWaterIntakeProgress() {
        val drunkMl = 1500
        val goalMl = 2000
        val expectedProgress = 0.75f // 75%
        
        val actualProgress = drunkMl.toFloat() / goalMl
        
        assertEquals(expectedProgress, actualProgress, 0.01f)
    }
}
