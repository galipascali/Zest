package com.example.zest.ui.feed

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Recipe
import com.example.zest.ui.recipe.FeedAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private val viewModel: FeedViewModel by viewModels()
    private var allRecipes: List<Recipe> = emptyList()
    private val categoryChipLabels = listOf("All", "Breakfast", "Vegan", "Fast & Easy")
    private var activeFilter = RecipeFilter()

    private fun applyFilters(filter: RecipeFilter): List<Recipe> {
        return allRecipes
            .asSequence()
            .filter { filter.searchText.isBlank() || it.title.contains(filter.searchText, ignoreCase = true) || it.tags.any { tag -> tag.contains(filter.searchText, ignoreCase = true) } }
            .filter { filter.category == "All" || it.tags.contains(filter.category) }
            .filter { filter.timeRange == null || it.time in filter.timeRange.minMinutes..filter.timeRange.maxMinutes }
            .filter { filter.servingsRange == null || it.servings in filter.servingsRange.first..filter.servingsRange.second }
            .filter { filter.difficulties.isEmpty() || it.difficulty in filter.difficulties }
            .toList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        val categoryChips = view.findViewById<ChipGroup>(R.id.categoryChips)
        val searchInput = view.findViewById<TextInputEditText>(R.id.searchInput)
        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)

        val adapter = FeedAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        fun render() {
            adapter.submitList(applyFilters(activeFilter))
        }

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            allRecipes = recipes
            render()
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