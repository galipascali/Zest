package com.example.zest.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.zest.model.Recipe

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserRecipes(userId: String): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: String): LiveData<Recipe?>

    @Upsert
    suspend fun upsertAll(recipes: List<Recipe>)

    @Upsert
    suspend fun upsert(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)
}