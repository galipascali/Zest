package com.example.zest.network

data class SpoonacularRandomResponse(
    val recipes: List<SpoonacularRecipe>
)

data class SpoonacularRecipe(
    val id: Int,
    val title: String,
    val image: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val extendedIngredients: List<SpoonacularIngredient>?,
    val analyzedInstructions: List<SpoonacularInstruction>?,
    val dishTypes: List<String>?
)

data class SpoonacularIngredient(
    val amount: Double,
    val unit: String,
    val name: String
)

data class SpoonacularInstruction(
    val steps: List<SpoonacularStep>?
)

data class SpoonacularStep(
    val number: Int,
    val step: String
)