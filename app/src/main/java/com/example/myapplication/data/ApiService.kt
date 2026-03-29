package com.example.myapplication.data

import com.example.myapplication.BuildConfig

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

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

// ExerciseDB API Model
@Serializable
data class ExerciseDbModel(
    val bodyPart: String = "",
    val equipment: String = "",
    val gifUrl: String = "",
    val id: String = "",
    val name: String = "",
    val target: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList()
)

@Serializable
data class WorkoutSuggestion(
    val id: Int = 0,
    val name: String? = null,
    val description: String? = null,
    val images: List<WorkoutImage> = emptyList(),
    val category: Category? = null,
    val muscles: List<Muscle> = emptyList(),
    val muscles_secondary: List<Muscle> = emptyList(),
    val equipment: List<Equipment> = emptyList()
) {
    val displayName: String get() = name ?: "Daily Workout"
    
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

@Serializable
data class NinjaNutrition(
    val name: String = "",
    val calories: Float = 0f,
    val serving_size_g: Float = 0f,
    val fat_total_g: Float = 0f,
    val fat_saturated_g: Float = 0f,
    val protein_g: Float = 0f,
    val sodium_mg: Float = 0f,
    val potassium_mg: Float = 0f,
    val cholesterol_mg: Float = 0f,
    val carbohydrates_total_g: Float = 0f,
    val fiber_g: Float = 0f,
    val sugar_g: Float = 0f
)

@Serializable
data class HFRequest(
    val inputs: String,
    val options: HFOptions = HFOptions()
)

@Serializable
data class HFOptions(
    val wait_for_model: Boolean = true
)

@Serializable
data class HFResponse(val generated_text: String? = null)

interface ApiService {
    @GET("exerciseinfo/")
    suspend fun getWorkouts(
        @Query("language") language: Int = 2,
        @Query("status") status: Int = 2,
        @Query("limit") limit: Int = 50
    ): WorkoutResponse

    @GET("ingredient/?language=2")
    suspend fun getNutrients(): NutrientResponse
}

interface ExerciseDbApiService {
    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int = 50,
        @Header("x-rapidapi-host") host: String = "exercisedb.p.rapidapi.com",
        @Header("x-rapidapi-key") apiKey: String = BuildConfig.RAPID_API_KEY,
        @Header("Content-Type") contentType: String = "application/json"
    ): List<ExerciseDbModel>
}

interface NinjaApiService {
    @GET("nutrition")
    suspend fun getNutritionDetails(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String = BuildConfig.NINJA_API_KEY
    ): List<NinjaNutrition>
}

interface HuggingFaceApiService {
    @POST
    suspend fun chat(
        @Url url: String = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2",
        @Header("Authorization") token: String = "Bearer ${BuildConfig.HF_API_KEY}",
        @Body request: HFRequest
    ): List<HFResponse>
}
