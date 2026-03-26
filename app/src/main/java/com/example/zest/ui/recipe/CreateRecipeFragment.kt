package com.example.zest.ui.recipe

import android.graphics.Color
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.zest.model.Recipe
import com.example.zest.repository.RecipeRepository
import com.example.zest.utils.uploadImageToStorage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CreateRecipeFragment : RecipeFormFragment() {

    override val formTitle = "Create Recipe"
    override val saveButtonText = "Publish Recipe  →"

    override fun onSaveClicked(view: View) {
        val title = binding.etTitle.text.toString().trim()
        val time = binding.etTime.text.toString().toInt()
        val servings = binding.etServings.text.toString().toInt()
        val difficulty = binding.etDifficulty.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val repository = RecipeRepository(requireContext())

        prepareForSave()
        showFormLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val imageUrl = selectedImageUri?.let {
                uploadImageToStorage(requireContext(), it, "recipe_images/${userId}_${System.currentTimeMillis()}.jpg")
            } ?: ""

            val recipe = Recipe(
                userId = userId,
                creatorEmail = FirebaseAuth.getInstance().currentUser?.email ?: "",
                imageUrl = imageUrl,
                title = title,
                time = time,
                servings = servings,
                difficulty = difficulty,
                ingredients = ingredients.toList(),
                steps = steps.toList(),
                tags = collectTags()
            )

            val success = repository.addRecipe(recipe)
            showFormLoading(false)

            if (success) {
                Snackbar.make(view, "Recipe published!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#4CAF50")).setTextColor(Color.WHITE).show()
                findNavController().popBackStack()
            } else {
                Snackbar.make(view, "Failed to publish recipe", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show()
            }
        }
    }
}
