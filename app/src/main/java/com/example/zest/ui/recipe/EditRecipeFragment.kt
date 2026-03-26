package com.example.zest.ui.recipe

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.databinding.FragmentCreateRecipeBinding
import com.example.zest.model.Ingredient
import com.example.zest.model.Step
import com.example.zest.utils.loadImage
import com.example.zest.utils.setSmallIcon
import com.example.zest.utils.showImagePickerDialog
import com.example.zest.utils.showLoading
import com.example.zest.utils.uploadImageToStorage
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class EditRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!

    private val args: EditRecipeFragmentArgs by navArgs()
    private val viewModel: EditRecipeViewModel by viewModels {
        EditRecipeViewModel.Factory(requireActivity().application, args.recipeId)
    }

    private var existingImageUrl = ""
    private var selectedImageUri: Uri? = null
    private lateinit var photoUri: Uri
    private var initialized = false

    private val ingredients = mutableListOf<Ingredient>()
    private lateinit var ingredientAdapter: IngredientAdapter
    private val steps = mutableListOf<Step>()
    private lateinit var stepsAdapter: StepAdapter

    private var titleValidated = false
    private var timeValidated = false
    private var servingsValidated = false
    private var difficultyValidated = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { setPhoto(it) }
    }
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) setPhoto(photoUri)
    }

    private fun setPhoto(uri: Uri) {
        selectedImageUri = uri
        _binding?.recipePhoto?.setImageURI(uri)
        _binding?.recipePhoto?.visibility = View.VISIBLE
        _binding?.photoPlaceholder?.visibility = View.GONE
    }

    private fun showPickerDialog() {
        showImagePickerDialog("recipe_photo.jpg", takePhotoLauncher, pickImageLauncher) { uri -> photoUri = uri }
    }

    private fun isFormValid() =
        titleValidated && timeValidated && servingsValidated && difficultyValidated &&
                ingredients.isNotEmpty() && steps.isNotEmpty()

    private fun updateButtonState() {
        _binding?.btnLogin?.isEnabled = isFormValid()
    }

    private fun createTag(text: String, isChecked: Boolean = false): Chip {
        val chip = Chip(requireContext())
        chip.text = "# $text"
        chip.isCheckable = true
        chip.chipStrokeWidth = 2f
        chip.setChipStrokeColorResource(R.color.light_grey)
        chip.setChipBackgroundColorResource(R.color.chip_background_selector)
        chip.setTextColor(resources.getColorStateList(R.color.chip_text_selector))
        chip.isChecked = isChecked
        val params = FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val margin = (10 * resources.displayMetrics.density).toInt()
            setMargins(margin, 0, 0, 0)
        }
        chip.layoutParams = params
        return chip
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recipeFormTitle.text = "Edit Recipe"
        binding.btnLogin.text = "Save Changes  →"

        fun showLoading(show: Boolean) {
            this.showLoading(show)
            binding.btnLogin.isEnabled = !show
        }

        binding.backArrow.setOnClickListener { findNavController().popBackStack() }
        binding.photoContainer.setOnClickListener { showPickerDialog() }

        binding.timeTextField.setSmallIcon(R.drawable.time)
        binding.servingsTextField.setSmallIcon(R.drawable.servings)
        binding.difficultyDropdown.setSmallIcon(R.drawable.difficulty)

        val difficultyItems = resources.getStringArray(R.array.difficulty_levels)
        binding.etDifficulty.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, difficultyItems))
        binding.etDifficulty.setOnItemClickListener { _, _, _, _ ->
            difficultyValidated = true
            updateButtonState()
        }

        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrBlank()) { binding.titleTextField.error = "Title is required"; titleValidated = false }
                else { binding.titleTextField.error = null; titleValidated = true }
                updateButtonState()
            }
        })

        binding.etTime.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val value = text?.toString()?.toIntOrNull()
                if (value == null || value <= 0) { binding.timeTextField.error = "Enter a valid time"; timeValidated = false }
                else { binding.timeTextField.error = null; timeValidated = true }
                updateButtonState()
            }
        })

        binding.etServings.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val value = text?.toString()?.toIntOrNull()
                if (value == null || value <= 0) { binding.servingsTextField.error = "Enter a valid servings amount"; servingsValidated = false }
                else { binding.servingsTextField.error = null; servingsValidated = true }
                updateButtonState()
            }
        })

        fun updateIngredientsCount() {
            val count = ingredients.size
            binding.emptyIngredient.visibility = if (count > 0) View.GONE else View.VISIBLE
            binding.ingredientsRecyclerView.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.ingredientsCount.text = "$count items"
            updateButtonState()
        }

        ingredientAdapter = IngredientAdapter(ingredients) { updateIngredientsCount() }
        binding.ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientsRecyclerView.adapter = ingredientAdapter

        binding.addIngredientButton.setOnClickListener {
            ingredients.add(Ingredient())
            ingredientAdapter.notifyItemInserted(ingredients.size - 1)
            updateIngredientsCount()
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                ingredientAdapter.moveItem(vh.adapterPosition, target.adapterPosition); return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {}
        }).attachToRecyclerView(binding.ingredientsRecyclerView)

        fun updateStepsCount() {
            binding.emptyStep.visibility = if (steps.size > 0) View.GONE else View.VISIBLE
            binding.stepsRecyclerView.visibility = if (steps.size > 0) View.VISIBLE else View.GONE
            updateButtonState()
        }

        stepsAdapter = StepAdapter(steps) { updateStepsCount() }
        binding.stepsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.stepsRecyclerView.adapter = stepsAdapter

        binding.addStepButton.setOnClickListener {
            steps.add(Step())
            stepsAdapter.notifyItemInserted(steps.size - 1)
            updateStepsCount()
        }

        val predefinedTags = listOf("Easy Meal", "Vegan", "Under 30m", "Breakfast")
        predefinedTags.forEach { binding.tagsGroup.addView(createTag(it)) }

        binding.tagInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                val minWidth = (44 * resources.displayMetrics.density).toInt()
                val newWidth = maxOf(minWidth, binding.tagInput.paint.measureText(text).toInt() + (32 * resources.displayMetrics.density).toInt())
                binding.tagInput.layoutParams = binding.tagInput.layoutParams.also { it.width = newWidth }
            }
        })
        binding.tagInput.setOnFocusChangeListener { _, hasFocus ->
            binding.tagInput.hint = if (!hasFocus && binding.tagInput.text.isNullOrEmpty()) "+" else ""
        }
        binding.tagInput.setOnEditorActionListener { _, _, _ ->
            val text = binding.tagInput.text.toString().trim()
            if (text.isNotEmpty()) {
                binding.tagsGroup.addView(createTag(text, true), binding.tagsGroup.childCount)
                binding.tagInput.text.clear()
            }
            true
        }

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            if (initialized || recipe == null) return@observe
            initialized = true
            existingImageUrl = recipe.imageUrl

            binding.etTitle.setText(recipe.title)
            binding.etTime.setText(recipe.time.toString())
            binding.etServings.setText(recipe.servings.toString())
            binding.etDifficulty.setText(recipe.difficulty, false)
            difficultyValidated = recipe.difficulty.isNotBlank()

            if (recipe.imageUrl.isNotBlank()) {
                binding.recipePhoto.loadImage(recipe.imageUrl)
                binding.recipePhoto.visibility = View.VISIBLE
                binding.photoPlaceholder.visibility = View.GONE
            }

            ingredients.addAll(recipe.ingredients)
            ingredientAdapter.notifyDataSetChanged()
            updateIngredientsCount()

            steps.addAll(recipe.steps)
            stepsAdapter.notifyDataSetChanged()
            updateStepsCount()

            for (i in 0 until binding.tagsGroup.childCount) {
                val chip = binding.tagsGroup.getChildAt(i) as? Chip ?: continue
                chip.isChecked = recipe.tags.contains(chip.text.toString().removePrefix("# "))
            }
            recipe.tags.filter { it !in predefinedTags }.forEach { tag ->
                binding.tagsGroup.addView(createTag(tag, true), binding.tagsGroup.childCount)
            }

            updateButtonState()
        }

        binding.btnLogin.setOnClickListener {
            val tags = mutableListOf<String>()
            for (i in 0 until binding.tagsGroup.childCount) {
                val chip = binding.tagsGroup.getChildAt(i) as? Chip ?: continue
                if (chip.isChecked) tags.add(chip.text.toString().removePrefix("# "))
            }

            showLoading(true)
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
                    tags = tags,
                    imageUrl = imageUrl
                )

                val success = viewModel.updateRecipe(updated)
                showLoading(false)
                if (success) {
                    findNavController().popBackStack()
                } else {
                    Snackbar.make(view, "Failed to save changes", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show()
                }
            }
        }
    }
}