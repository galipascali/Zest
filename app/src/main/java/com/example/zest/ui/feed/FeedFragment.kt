package com.example.zest.ui.feed

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.ui.recipe.FeedAdapter
import com.example.zest.utils.afterTextChanged
import com.example.zest.utils.showLoading
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private val viewModel: FeedViewModel by viewModels()
    private val categoryChipLabels = listOf("All", "Breakfast", "Vegan", "Fast & Easy")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)

        val recycler = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        val categoryChips = view.findViewById<ChipGroup>(R.id.categoryChips)
        val searchInput = view.findViewById<TextInputEditText>(R.id.searchInput)
        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)

        val adapter = FeedAdapter { recipe ->
            val action = FeedFragmentDirections.actionFeedToRecipeDetail(recipe.id)
            findNavController().navigate(action)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewModel.filteredRecipes.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            showLoading(false)
        }

        searchInput.afterTextChanged { text ->
            viewModel.setFilter(viewModel.filter.value!!.copy(searchText = text))
        }

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
            viewModel.setFilter(viewModel.filter.value!!.copy(category = chip?.text?.toString() ?: "All"))
        }

        filterButton.setOnClickListener {
            FilterBottomSheetFragment(viewModel.filter.value!!) { newFilter ->
                viewModel.setFilter(newFilter)
            }.show(parentFragmentManager, "filter")
        }
    }
}