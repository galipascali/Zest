package com.example.zest.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zest.model.Recipe
import com.example.zest.repository.RecipeRepository
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)
    private val allRecipes = MutableLiveData<List<Recipe>>(emptyList())

    val filter = MutableLiveData(RecipeFilter())
    val filteredRecipes = MutableLiveData<List<Recipe>>(emptyList())

    init {
        viewModelScope.launch { repository.syncFromFirestore() }

        repository.getAllRecipes().observeForever { recipes ->
            allRecipes.value = recipes
            applyFilters()
        }
        filter.observeForever { applyFilters() }
    }

    fun setFilter(newFilter: RecipeFilter) {
        filter.value = newFilter
    }

    private fun applyFilters() {
        val f = filter.value ?: RecipeFilter()
        filteredRecipes.value = (allRecipes.value ?: emptyList())
            .asSequence()
            .filter { f.searchText.isBlank() || it.title.contains(f.searchText, ignoreCase = true) || it.tags.any { tag -> tag.contains(f.searchText, ignoreCase = true) } }
            .filter { f.category == "All" || it.tags.contains(f.category) }
            .filter { f.timeRange == null || it.time in f.timeRange.minMinutes..f.timeRange.maxMinutes }
            .filter { f.servingsRange == null || it.servings in f.servingsRange.first..f.servingsRange.second }
            .filter { f.difficulties.isEmpty() || it.difficulty in f.difficulties }
            .toList()
    }
}
