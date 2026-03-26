package com.example.zest.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.zest.database.AppDatabase
import com.example.zest.model.Ingredient
import com.example.zest.model.Recipe
import com.example.zest.model.Step
import com.example.zest.network.SPOONACULAR_API_KEY
import com.example.zest.network.SpoonacularApi
import com.example.zest.network.SpoonacularRecipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RecipeRepository(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val dao = AppDatabase.getInstance(context).recipeDao()

    fun getAllRecipes(): LiveData<List<Recipe>> = dao.getAllRecipes()
    fun getUserRecipes(userId: String): LiveData<List<Recipe>> = dao.getUserRecipes(userId)
    fun getRecipeById(id: String): LiveData<Recipe?> = dao.getRecipeById(id)

    suspend fun syncFromFirestore() {
        try {
            val snapshot = firestore.collection("recipes").get().await()
            val recipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
            dao.upsertAll(recipes)
        } catch (e: Exception) {}
    }

    suspend fun syncUserRecipesFromFirestore(userId: String) {
        try {
            val snapshot = firestore.collection("recipes")
                .whereEqualTo("userId", userId)
                .get().await()
            val recipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
            dao.upsertAll(recipes)
        } catch (e: Exception) {}
    }

    suspend fun syncFromSpoonacular() {
        try {
            val response = SpoonacularApi.instance.getRandomRecipes(10, SPOONACULAR_API_KEY).recipes
            Log.d("RecipeRepository", "syncFromSpoonacular: ${response.size}")
            val recipes = response.map { it.toRecipe() }
            dao.deleteByUserId("spoonacular")
            dao.upsertAll(recipes)
        } catch (_: Exception) {}
    }

    private fun SpoonacularRecipe.toRecipe() = Recipe(
        id = "spoonacular_$id",
        userId = "spoonacular",
        creatorEmail = "Spoonacular@api",
        title = title,
        imageUrl = image ?: "",
        time = readyInMinutes,
        servings = servings,
        difficulty = "",
        ingredients = extendedIngredients?.map { ingredient ->
            val amount = if (ingredient.amount % 1 == 0.0) ingredient.amount.toInt().toString()
                         else ingredient.amount.toString()
            Ingredient(quantity = "$amount ${ingredient.unit}".trim(), name = ingredient.name)
        } ?: emptyList(),
        steps = analyzedInstructions?.firstOrNull()?.steps?.map { Step(number = it.number, text = it.step) } ?: emptyList(),
        tags = dishTypes ?: emptyList()
    )

    suspend fun addRecipe(recipe: Recipe): Boolean {
        return try {
            val userDoc = firestore.collection("users").document(recipe.userId).get().await()
            val creatorPhotoUrl = userDoc.getString("photoUrl") ?: ""
            val docRef = firestore.collection("recipes").document()
            val recipeWithId = recipe.copy(id = docRef.id, creatorPhoto = creatorPhotoUrl)
            docRef.set(recipeWithId).await()
            try { dao.upsert(recipeWithId) } catch (_: Exception) {}
            true
        } catch (e: Exception) {
            false
        }
    }
}