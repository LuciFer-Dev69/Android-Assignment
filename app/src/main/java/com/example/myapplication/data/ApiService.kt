package com.example.myapplication.data

import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class WorkoutResponse(
    val results: List<WorkoutSuggestion> = emptyList()
)

@Serializable
data class WorkoutImage(
    val image: String = ""
)

@Serializable
data class Category(
    val id: Int = 0,
    val name: String = "Full Body"
)

@Serializable
data class Muscle(
    val id: Int = 0,
    val name: String = ""
)

@Serializable
data class Equipment(
    val id: Int = 0,
    val name: String = ""
)

@Serializable
data class WorkoutSuggestion(
    val id: Int = 0,
    val name: String = "Untitled Exercise",
    val description: String = "",
    val images: List<WorkoutImage> = emptyList(),
    val category: Category? = null,
    val muscles: List<Muscle> = emptyList(),
    val muscles_secondary: List<Muscle> = emptyList(),
    val equipment: List<Equipment> = emptyList()
) {
    val imageUrl: String? get() {
        val path = images.firstOrNull { it.image.isNotBlank() }?.image
        return if (path != null && !path.startsWith("http")) {
            "https://wger.de$path"
        } else {
            path
        }
    }
    val muscleGroup: String get() = category?.name ?: "Full Body"
    val caloriesBurned: Int get() = (50..150).random()
}

@Serializable
data class NutrientResponse(
    val results: List<Nutrient> = emptyList()
)

@Serializable
data class Nutrient(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val unit: String = "",
    val image: String? = null
) {
    val imageUrl: String? get() {
        return if (image != null && !image.startsWith("http")) {
            "https://wger.de$image"
        } else {
            image
        }
    }
}

interface ApiService {
    @GET("exerciseinfo/?language=2")
    suspend fun getWorkouts(): WorkoutResponse

    @GET("ingredient/?language=2")
    suspend fun getNutrients(): NutrientResponse
}
