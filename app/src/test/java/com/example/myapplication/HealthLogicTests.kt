package com.example.myapplication

import com.example.myapplication.utils.HealthMathUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthLogicTests {

    // For Nikhil: WaterTrackerTest
    // Logic: 500ml + 250ml = 750ml
    @Test
    fun testWaterTracker_Addition() {
        val waterLogs = listOf(500, 250)
        val expectedTotal = 750
        val actualTotal = HealthMathUtils.calculateTotalWater(waterLogs)
        assertEquals("Water sum calculation is incorrect", expectedTotal, actualTotal)
    }

    // For Nikhil: BMICalculatorTest
    // Logic: weight 70kg, height 170cm -> BMI 24.2 (Normal)
    @Test
    fun testBMICalculator_NormalCategory() {
        val height = 170.0
        val weight = 70.0
        val expectedBMI = 24.2
        val expectedCategory = "Normal"
        
        val actualBMI = HealthMathUtils.calculateBMI(weight, height)
        val actualCategory = HealthMathUtils.getBmiCategory(actualBMI)
        
        assertEquals("BMI value is incorrect", expectedBMI, actualBMI, 0.1)
        assertEquals("BMI category is incorrect", expectedCategory, actualCategory)
    }

    // For Nikhil: WeightTrackerTest
    // Logic: Add 3 weights, assert the last one is correct
    @Test
    fun testWeightTracker_LatestEntry() {
        val weights = listOf(72.5, 71.8, 70.5)
        val expectedLatest = 70.5
        val actualLatest = HealthMathUtils.getLatestWeightEntry(weights)
        assertEquals("Latest weight entry is incorrect", expectedLatest, actualLatest)
    }

    // For Aaditya: StepGoalTest
    // Logic: 7,500 steps of 10,000 goal = 75%
    @Test
    fun testStepGoal_ProgressPercentage() {
        val goal = 10000
        val currentSteps = 7500
        val expectedProgress = 75
        
        val actualProgress = HealthMathUtils.calculateStepProgress(currentSteps, goal)
        
        assertEquals("Step progress percentage is incorrect", expectedProgress, actualProgress)
    }
}
