package com.example.zest.ui.feed

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Ingredient
import com.example.zest.model.Recipe
import com.example.zest.model.Step
import com.example.zest.ui.recipe.FeedAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private val mockRecipes = listOf(
        Recipe(
            id = "1", userId = "user_1",
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
            title = "Avocado Toast", time = 10, servings = 2, difficulty = "Easy",
            ingredients = listOf(Ingredient(2, "slices sourdough bread"), Ingredient(1, "ripe avocado")),
            steps = listOf(Step(1, "Toast the sourdough slices until golden and crispy.")),
            tags = listOf("Easy Meal", "Vegan", "Breakfast", "Under 30m")
        ),
        Recipe(
            id = "2", userId = "user_1",
            imageUrl = "https://images.unsplash.com/photo-1563379926898-05f4575a45d8",
            title = "Spaghetti Carbonara", time = 25, servings = 4, difficulty = "Medium",
            ingredients = listOf(Ingredient(400, "g spaghetti")),
            steps = listOf(Step(1, "Cook spaghetti in salted boiling water until al dente.")),
            tags = listOf("Dinner", "Italian", "Classic")
        ),
        Recipe(
            id = "3", userId = "user_2",
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd",
            title = "Greek Salad", time = 15, servings = 3, difficulty = "Easy",
            ingredients = listOf(Ingredient(3, "tomatoes, chopped")),
            steps = listOf(Step(1, "Chop tomatoes, cucumber and red onion into bite-sized pieces.")),
            tags = listOf("Salad", "Vegan", "Under 30m", "Healthy")
        ),
        Recipe(
            id = "4", userId = "user_2",
            imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38",
            title = "Margherita Pizza", time = 45, servings = 4, difficulty = "Medium",
            ingredients = listOf(Ingredient(500, "g pizza dough")),
            steps = listOf(Step(1, "Preheat oven to 250°C with a pizza stone or tray inside.")),
            tags = listOf("Dinner", "Italian", "Weekend")
        )
    )

    private val categoryChipLabels = listOf("All", "Breakfast", "Vegan", "Fast & Easy")
    private var activeFilter = RecipeFilter()

    private fun applyFilters(filter: RecipeFilter): List<Recipe> {
        return mockRecipes
            .filter { filter.searchText.isBlank() || it.title.contains(filter.searchText, ignoreCase = true) }
            .filter { filter.category == "All" || it.tags.contains(filter.category) }
            .filter { filter.timeRange == null || it.time in filter.timeRange.minMinutes..filter.timeRange.maxMinutes }
            .filter { filter.servingsRange == null || it.servings in filter.servingsRange.first..filter.servingsRange.second }
            .filter { filter.difficulties.isEmpty() || it.difficulty in filter.difficulties }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        val categoryChips = view.findViewById<ChipGroup>(R.id.categoryChips)
        val searchInput = view.findViewById<TextInputEditText>(R.id.searchInput)
        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)

        val adapter = FeedAdapter(emptyList())
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        fun render() {
            adapter.submitList(applyFilters(activeFilter))
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                activeFilter = activeFilter.copy(searchText = editable.toString())
                render()
            }
        })

        categoryChipLabels.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = label == "All"
                setChipBackgroundColorResource(R.color.chip_background_selector)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector))
            }
            categoryChips.addView(chip)
        }

        categoryChips.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            activeFilter = activeFilter.copy(category = chip?.text?.toString() ?: "All")
            render()
        }

        filterButton.setOnClickListener {
            FilterBottomSheetFragment(activeFilter) { newFilter ->
                activeFilter = newFilter
                render()
            }.show(parentFragmentManager, "filter")
        }

        render()
    }
}