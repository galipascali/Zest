package com.example.zest.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zest.R
import com.example.zest.databinding.FragmentFiltersBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

data class RecipeFilter(
    val searchText: String = "",
    val category: String = "All",
    val timeRange: TimeRange? = null,
    val servingsRange: Pair<Int, Int>? = null,
    val difficulties: Set<String> = emptySet()
)

enum class TimeRange(val label: String, val minMinutes: Int, val maxMinutes: Int) {
    UNDER_30("0 – 30 min", 0, 30),
    THIRTY_TO_60("30min – 1hr", 30, 60),
    ONE_TO_TWO("1 – 2 hr", 60, 120),
    TWO_PLUS("2+ hr", 120, Int.MAX_VALUE)
}

class FilterBottomSheetFragment(
    private val currentFilter: RecipeFilter,
    private val onApply: (RecipeFilter) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTimeChips()
        setupServingsSlider()
        setupDifficultyChips()
        setupButtons()
    }

    private fun setupTimeChips() {
        TimeRange.entries.forEach { range ->
            val chip = Chip(requireContext()).apply {
                text = range.label
                isCheckable = true
                isChecked = currentFilter.timeRange == range
                tag = range
                setChipBackgroundColorResource(R.color.chip_background_selector)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector))
            }
            binding.timeChipGroup.addView(chip)
        }
    }

    private fun setupServingsSlider() {
        val (from, to) = currentFilter.servingsRange ?: Pair(1, 10)
        binding.servingsSlider.values = listOf(from.toFloat(), to.toFloat())
        updateServingsLabel(from, to)

        binding.servingsSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            updateServingsLabel(values[0].toInt(), values[1].toInt())
        }
    }

    private fun updateServingsLabel(from: Int, to: Int) {
        binding.servingsLabel.text = "$from – $to servings"
    }

    private fun setupDifficultyChips() {
        listOf("Easy", "Medium", "Hard").forEach { level ->
            val chip = Chip(requireContext()).apply {
                text = level
                isCheckable = true
                isChecked = currentFilter.difficulties.contains(level)
                setChipBackgroundColorResource(R.color.chip_background_selector)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector))
            }
            binding.difficultyChipGroup.addView(chip)
        }
    }

    private fun setupButtons() {
        binding.btnReset.setOnClickListener {
            onApply(RecipeFilter(searchText = currentFilter.searchText, category = currentFilter.category))
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            val selectedTime = binding.timeChipGroup.checkedChipIds
                .mapNotNull { id -> binding.timeChipGroup.findViewById<Chip>(id)?.tag as? TimeRange }
                .firstOrNull()

            val sliderValues = binding.servingsSlider.values
            val servings = Pair(sliderValues[0].toInt(), sliderValues[1].toInt())

            val difficulties = binding.difficultyChipGroup.checkedChipIds
                .mapNotNull { id -> binding.difficultyChipGroup.findViewById<Chip>(id)?.text?.toString() }
                .toSet()

            onApply(
                currentFilter.copy(
                    timeRange = selectedTime,
                    servingsRange = servings,
                    difficulties = difficulties
                )
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}