package com.example.zest.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.zest.model.Recipe
import com.example.zest.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecipeRepository(application)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val recipes: LiveData<List<Recipe>> = repository.getUserRecipes(userId)

    init {
        viewModelScope.launch {
            repository.syncUserRecipesFromFirestore(userId)
        }
    }
}