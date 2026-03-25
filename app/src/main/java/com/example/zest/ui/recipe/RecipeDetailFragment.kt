package com.example.zest.ui.recipe

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.utils.loadImageWithCallback
import com.example.zest.utils.showLoading

class RecipeDetailFragment : Fragment(R.layout.fragment_recipe_detail) {

    private val args: RecipeDetailFragmentArgs by navArgs()
    private val viewModel: RecipeDetailViewModel by viewModels {
        RecipeDetailViewModel.Factory(requireActivity().application, args.recipeId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val image = view.findViewById<ImageView>(R.id.detailImage)
        val title = view.findViewById<TextView>(R.id.detailTitle)
        val creator = view.findViewById<TextView>(R.id.detailCreator)
        val time = view.findViewById<TextView>(R.id.detailsTime)
        val difficulty = view.findViewById<TextView>(R.id.detailsDifficulty)
        val servings = view.findViewById<TextView>(R.id.detailsServings)
        val tabIngredients = view.findViewById<TextView>(R.id.tabIngredients)
        val tabInstructions = view.findViewById<TextView>(R.id.tabInstructions)
        val ingredientsRecycler = view.findViewById<RecyclerView>(R.id.ingredientsRecycler)
        val stepsRecycler = view.findViewById<RecyclerView>(R.id.stepsRecycler)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)

        ingredientsRecycler.layoutManager = LinearLayoutManager(requireContext())
        stepsRecycler.layoutManager = LinearLayoutManager(requireContext())

        backButton.setOnClickListener { findNavController().navigateUp() }

        fun selectTab(showIngredients: Boolean) {
            tabIngredients.isSelected = showIngredients
            tabInstructions.isSelected = !showIngredients
            ingredientsRecycler.visibility = if (showIngredients) View.VISIBLE else View.GONE
            stepsRecycler.visibility = if (showIngredients) View.GONE else View.VISIBLE
        }
        selectTab(true)
        tabIngredients.setOnClickListener { selectTab(true) }
        tabInstructions.setOnClickListener { selectTab(false) }

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe ?: return@observe

            title.text = recipe.title
            creator.text = "@${recipe.creatorEmail.substringBefore("@")}"
            time.text = "${recipe.time} min"
            difficulty.text = recipe.difficulty
            servings.text = "${recipe.servings} ppl"

            image.loadImageWithCallback(recipe.imageUrl, R.drawable.welcome_background) {
                showLoading(false)
            }

            ingredientsRecycler.adapter = IngredientDetailAdapter(recipe.ingredients)
            stepsRecycler.adapter = StepDetailAdapter(recipe.steps)
        }
    }
}
