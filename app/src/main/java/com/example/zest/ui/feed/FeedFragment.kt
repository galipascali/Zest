package com.example.zest.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zest.databinding.FragmentFeedBinding
import com.example.zest.ui.recipe.FeedAdapter
import com.example.zest.utils.afterTextChanged
import com.example.zest.utils.showLoading
import com.google.android.material.chip.Chip

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedViewModel by viewModels()
    private val categoryChipLabels = listOf("All", "Breakfast", "Vegan", "Fast & Easy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)

        val adapter = FeedAdapter { recipe ->
            findNavController().navigate(FeedFragmentDirections.actionFeedToRecipeDetail(recipe.id))
        }
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter

        viewModel.filteredRecipes.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            showLoading(false)
        }

        binding.searchInput.afterTextChanged { text ->
            viewModel.setFilter(viewModel.filter.value!!.copy(searchText = text))
        }

        categoryChipLabels.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = label == "All"
                setChipBackgroundColorResource(com.example.zest.R.color.chip_background_selector)
                setTextColor(resources.getColorStateList(com.example.zest.R.color.chip_text_selector))
            }
            binding.categoryChips.addView(chip)
        }

        binding.categoryChips.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            viewModel.setFilter(viewModel.filter.value!!.copy(category = chip?.text?.toString() ?: "All"))
        }

        binding.filterButton.setOnClickListener {
            FilterBottomSheetFragment(viewModel.filter.value!!) { newFilter ->
                viewModel.setFilter(newFilter)
            }.show(parentFragmentManager, "filter")
        }
    }
}