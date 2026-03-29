package com.example.myapplication.utils

import kotlin.math.pow
import kotlin.math.roundToInt

object HealthMathUtils {
    /**
     * Calculates the sum of water intake logs.
     */
    fun calculateTotalWater(amounts: List<Int>): Int {
        return amounts.sum()
    }

    /**
     * Calculates BMI given weight in kg and height in cm.
     */
    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        val heightM = heightCm / 100.0
        val bmi = weightKg / heightM.pow(2)
        // Round to 1 decimal place
        return (bmi * 10.0).roundToInt() / 10.0
    }

    /**
     * Returns the BMI category string.
     */
    fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    /**
     * Calculates percentage of step goal achieved.
     */
    fun calculateStepProgress(currentSteps: Int, goal: Int): Int {
        if (goal <= 0) return 0
        return ((currentSteps.toFloat() / goal) * 100).toInt()
    }

    /**
     * Returns the latest weight from a list of weight entries.
     */
    fun getLatestWeightEntry(weights: List<Double>): Double? {
        return weights.lastOrNull()
    }
}
