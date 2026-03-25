package com.example.zest.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zest.R
import com.example.zest.databinding.FragmentRecipeDetailBinding
import com.example.zest.utils.loadImageWithCallback
import com.example.zest.utils.showLoading

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RecipeDetailFragmentArgs by navArgs()
    private val viewModel: RecipeDetailViewModel by viewModels {
        RecipeDetailViewModel.Factory(requireActivity().application, args.recipeId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ingredientsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.stepsRecycler.layoutManager = LinearLayoutManager(requireContext())

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        fun selectTab(showIngredients: Boolean) {
            binding.tabIngredients.isSelected = showIngredients
            binding.tabInstructions.isSelected = !showIngredients
            binding.ingredientsRecycler.visibility = if (showIngredients) View.VISIBLE else View.GONE
            binding.stepsRecycler.visibility = if (showIngredients) View.GONE else View.VISIBLE
        }
        selectTab(true)
        binding.tabIngredients.setOnClickListener { selectTab(true) }
        binding.tabInstructions.setOnClickListener { selectTab(false) }

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe ?: return@observe

            binding.detailTitle.text = recipe.title
            binding.detailCreator.text = "@${recipe.creatorEmail.substringBefore("@")}"
            binding.detailsTime.text = "${recipe.time} min"
            binding.detailsDifficulty.text = recipe.difficulty
            binding.detailsServings.text = "${recipe.servings} ppl"

            binding.detailImage.loadImageWithCallback(recipe.imageUrl, R.drawable.welcome_background) {
                showLoading(false)
            }

            binding.ingredientsRecycler.adapter = IngredientDetailAdapter(recipe.ingredients)
            binding.stepsRecycler.adapter = StepDetailAdapter(recipe.steps)
        }
    }
}