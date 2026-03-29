package com.example.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Int,
    val title: String,
    val mainIngredient: String,
    val timeToCook: String,
    val difficulty: String,
    val calories: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val imageUrl: String? = null,
    val dominantMacro: String = "Carbs" // Protein, Carbs, Fats
)

object RecipeProvider {
    val recipes = listOf(
        Recipe(
            1, "Grilled Lemon Chicken", "Chicken", "25 mins", "Easy", 350,
            listOf("2 Chicken breasts", "1 Lemon", "2 tbsp Olive oil", "Salt & Pepper", "Fresh Parsley"),
            listOf(
                "Marinate chicken with lemon juice, oil, salt, and pepper for 10 mins.",
                "Heat a grill pan over medium-high heat.",
                "Grill chicken for 6-8 minutes per side until cooked through.",
                "Garnish with parsley and serve."
            ),
            "https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?q=80&w=500",
            "Protein"
        ),
        Recipe(
            2, "Avocado Toast with Egg", "Avocado", "10 mins", "Easy", 280,
            listOf("1 Ripe Avocado", "2 Slices of whole grain bread", "1 Egg", "Red pepper flakes"),
            listOf(
                "Toast the bread until golden brown.",
                "Mash the avocado in a bowl with a pinch of salt.",
                "Fry or poach the egg to your liking.",
                "Spread avocado on toast, top with egg and red pepper flakes."
            ),
            "https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=500",
            "Fats"
        ),
        Recipe(
            3, "Banana Oatmeal Pancakes", "Banana", "15 mins", "Medium", 320,
            listOf("1 Ripe Banana", "1/2 cup Oats", "1 Egg", "Cinnamon"),
            listOf(
                "Mash the banana in a bowl until smooth.",
                "Mix in the egg and oats until well combined.",
                "Heat a non-stick pan and pour small circles of batter.",
                "Cook for 2-3 mins each side. Serve with honey."
            ),
            "https://images.unsplash.com/photo-1528207776546-365bb710ee93?q=80&w=500",
            "Carbs"
        ),
        Recipe(
            4, "Greek Yogurt Parfait", "Yogurt", "5 mins", "Easy", 210,
            listOf("1 cup Greek Yogurt", "1/4 cup Granola", "Handful of Berries", "1 tsp Honey"),
            listOf(
                "Layer yogurt in a glass or bowl.",
                "Add a layer of berries.",
                "Top with granola and a drizzle of honey."
            ),
            "https://images.unsplash.com/photo-1488477181946-6428a0291777?q=80&w=500",
            "Protein"
        ),
        Recipe(
            5, "Quinoa Salad", "Quinoa", "20 mins", "Medium", 310,
            listOf("1 cup Cooked Quinoa", "Cucumber", "Cherry Tomatoes", "Feta Cheese", "Lemon dressing"),
            listOf(
                "In a large bowl, combine cooked quinoa and chopped vegetables.",
                "Toss with lemon juice and olive oil.",
                "Top with crumbled feta cheese.",
                "Serve chilled."
            ),
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=500",
            "Carbs"
        )
    )

    fun getRecipesForIngredient(ingredientName: String): List<Recipe> {
        return recipes.filter { 
            it.mainIngredient.contains(ingredientName, ignoreCase = true) ||
            ingredientName.contains(it.mainIngredient, ignoreCase = true)
        }
    }
}
