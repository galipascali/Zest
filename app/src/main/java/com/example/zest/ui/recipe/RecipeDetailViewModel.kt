package com.example.zest.ui.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.zest.model.Recipe
import com.example.zest.repository.RecipeRepository

class RecipeDetailViewModel(application: Application, recipeId: String) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)

    val recipe: LiveData<Recipe?> = repository.getRecipeById(recipeId)

    class Factory(
        private val application: Application,
        private val recipeId: String
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(application, recipeId) as T
        }
    }
}
