package com.example.zest.ui.recipe

import android.graphics.Color
import android.view.View
import com.example.zest.R
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zest.utils.loadImage
import com.example.zest.utils.uploadImageToStorage
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class EditRecipeFragment : RecipeFormFragment() {

    override val formTitle = "Edit Recipe"
    override val saveButtonText = "Save Changes  →"

    private val args: EditRecipeFragmentArgs by navArgs()
    private val viewModel: EditRecipeViewModel by viewModels {
        EditRecipeViewModel.Factory(requireActivity().application, args.recipeId)
    }

    private var initialized = false

    override fun onFormReady(predefinedTags: List<String>) {
        binding.deleteButton.visibility = View.VISIBLE
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Are you sure you want to delete this recipe? This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    val recipe = viewModel.recipe.value ?: return@setPositiveButton
                    showFormLoading(true)
                    viewLifecycleOwner.lifecycleScope.launch {
                        val success = viewModel.deleteRecipe(recipe)
                        showFormLoading(false)
                        if (success) {
                            findNavController().popBackStack()
                        } else {
                            Snackbar.make(requireView(), "Failed to delete recipe", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(requireContext().getColor(R.color.error)).setTextColor(Color.WHITE).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            if (initialized || recipe == null) return@observe
            initialized = true
            existingImageUrl = recipe.imageUrl

            binding.etTitle.setText(recipe.title)
            binding.etTime.setText(recipe.time.toString())
            binding.etServings.setText(recipe.servings.toString())
            binding.etDifficulty.setText(recipe.difficulty, false)

            if (recipe.imageUrl.isNotBlank()) {
                binding.recipePhoto.loadImage(recipe.imageUrl)
                binding.recipePhoto.visibility = View.VISIBLE
                binding.photoPlaceholder.visibility = View.GONE
            }

            ingredients.addAll(recipe.ingredients)
            ingredientAdapter.notifyDataSetChanged()

            steps.addAll(recipe.steps)
            stepsAdapter.notifyDataSetChanged()

            for (i in 0 until binding.tagsGroup.childCount) {
                val chip = binding.tagsGroup.getChildAt(i) as? Chip ?: continue
                chip.isChecked = recipe.tags.contains(chip.text.toString().removePrefix("# "))
            }
            recipe.tags.filter { it !in predefinedTags }.forEach { tag ->
                binding.tagsGroup.addView(createTag(tag, true), binding.tagsGroup.childCount)
            }

            updateButtonState()
        }
    }

    override fun onSaveClicked(view: View) {
        prepareForSave()
        showFormLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val imageUrl = selectedImageUri?.let {
                uploadImageToStorage(requireContext(), it, "recipe_images/${System.currentTimeMillis()}.jpg")
            } ?: existingImageUrl

            val current = viewModel.recipe.value ?: return@launch
            val updated = current.copy(
                title = binding.etTitle.text.toString().trim(),
                time = binding.etTime.text.toString().toIntOrNull() ?: current.time,
                servings = binding.etServings.text.toString().toIntOrNull() ?: current.servings,
                difficulty = binding.etDifficulty.text.toString(),
                ingredients = ingredients.toList(),
                steps = steps.toList(),
                tags = collectTags(),
                imageUrl = imageUrl
            )

            val success = viewModel.updateRecipe(updated)
            showFormLoading(false)
            if (success) {
                Snackbar.make(view, "Changes saved", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().getColor(R.color.success)).setTextColor(Color.WHITE).show()
                findNavController().popBackStack()
            } else {
                Snackbar.make(view, "Failed to save changes", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().getColor(R.color.error)).setTextColor(Color.WHITE).show()
            }
        }
    }
}