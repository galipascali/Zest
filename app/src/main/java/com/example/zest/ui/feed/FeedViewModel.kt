package com.example.zest.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.zest.model.Recipe
import com.example.zest.repository.RecipeRepository
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)
    val recipes: LiveData<List<Recipe>> = repository.getAllRecipes()

    init {
        viewModelScope.launch {
            repository.syncFromFirestore()
        }
    }
}
