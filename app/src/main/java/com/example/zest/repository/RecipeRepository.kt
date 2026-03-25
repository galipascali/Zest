package com.example.zest.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.zest.database.AppDatabase
import com.example.zest.model.Recipe
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
            recipes.forEach { dao.upsert(it) }
        } catch (e: Exception) {}
    }

    suspend fun syncUserRecipesFromFirestore(userId: String) {
        try {
            val snapshot = firestore.collection("recipes")
                .whereEqualTo("userId", userId)
                .get().await()
            val recipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
            recipes.forEach { dao.upsert(it) }
        } catch (e: Exception) {}
    }

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