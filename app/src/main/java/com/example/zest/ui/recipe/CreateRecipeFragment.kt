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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.databinding.FragmentCreateRecipeBinding
import com.example.zest.model.Ingredient
import com.example.zest.model.Recipe
import com.example.zest.model.Step
import com.example.zest.repository.RecipeRepository
import com.example.zest.utils.showImagePickerDialog
import com.example.zest.utils.showLoading
import com.example.zest.utils.uploadImageToStorage
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoUri: Uri
    private var selectedImageUri: Uri? = null

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

    private fun showImagePickerDialog() {
        showImagePickerDialog("recipe_photo.jpg", takePhotoLauncher, pickImageLauncher) { uri ->
            photoUri = uri
        }
    }

    private val ingredients = mutableListOf<Ingredient>()
    private lateinit var ingredientRecyclerAdapter: IngredientAdapter
    private val steps = mutableListOf<Step>()
    private lateinit var stepsRecyclerAdapter: StepAdapter

    private var titleValidated = false
    private var timeValidated = false
    private var servingsValidated = false
    private var difficultyValidated = false

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

        fun showLoading(show: Boolean) {
            this.showLoading(show)
            binding.btnLogin.isEnabled = !show
        }

        binding.backArrow.setOnClickListener { findNavController().popBackStack() }

        binding.photoContainer.setOnClickListener { showImagePickerDialog() }

        val items = resources.getStringArray(R.array.difficulty_levels)
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        binding.etDifficulty.setAdapter(difficultyAdapter)
        binding.etDifficulty.setOnItemClickListener { _, _, _, _ ->
            difficultyValidated = true
            updateButtonState()
        }

        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrBlank()) {
                    binding.titleTextField.error = "Title is required"
                    titleValidated = false
                } else {
                    binding.titleTextField.error = null
                    titleValidated = true
                }
                updateButtonState()
            }
        })

        binding.etTime.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val value = text?.toString()?.toIntOrNull()
                if (value == null || value <= 0) {
                    binding.timeTextField.error = "Enter a valid time"
                    timeValidated = false
                } else {
                    binding.timeTextField.error = null
                    timeValidated = true
                }
                updateButtonState()
            }
        })

        binding.etServings.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val value = text?.toString()?.toIntOrNull()
                if (value == null || value <= 0) {
                    binding.servingsTextField.error = "Enter a valid servings amount"
                    servingsValidated = false
                } else {
                    binding.servingsTextField.error = null
                    servingsValidated = true
                }
                updateButtonState()
            }
        })

        fun updateIngredientsCount() {
            val count = ingredients.size
            if (count > 0) {
                binding.emptyIngredient.visibility = View.GONE
                binding.ingredientsRecyclerView.visibility = View.VISIBLE
            } else {
                binding.emptyIngredient.visibility = View.VISIBLE
                binding.ingredientsRecyclerView.visibility = View.GONE
            }
            binding.ingredientsCount.text = "$count items"
            updateButtonState()
        }

        ingredientRecyclerAdapter = IngredientAdapter(ingredients) { updateIngredientsCount() }
        binding.ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientsRecyclerView.adapter = ingredientRecyclerAdapter

        binding.addIngredientButton.setOnClickListener {
            ingredients.add(Ingredient())
            ingredientRecyclerAdapter.notifyItemInserted(ingredients.size - 1)
            updateIngredientsCount()
        }

        val ingredientTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                ingredientRecyclerAdapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        ingredientTouchHelper.attachToRecyclerView(binding.ingredientsRecyclerView)

        fun updateStepsCount() {
            if (steps.size > 0) {
                binding.emptyStep.visibility = View.GONE
                binding.stepsRecyclerView.visibility = View.VISIBLE
            } else {
                binding.emptyStep.visibility = View.VISIBLE
                binding.stepsRecyclerView.visibility = View.GONE
            }
            updateButtonState()
        }

        stepsRecyclerAdapter = StepAdapter(steps) { updateStepsCount() }
        binding.stepsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.stepsRecyclerView.adapter = stepsRecyclerAdapter

        binding.addStepButton.setOnClickListener {
            steps.add(Step())
            stepsRecyclerAdapter.notifyItemInserted(steps.size - 1)
            updateStepsCount()
        }

        listOf("Easy Meal", "Vegan", "Under 30m", "Breakfast").forEach {
            binding.tagsGroup.addView(createTag(it))
        }

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

        binding.btnLogin.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val time = binding.etTime.text.toString().toInt()
            val servings = binding.etServings.text.toString().toInt()
            val difficulty = binding.etDifficulty.text.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            val tags = mutableListOf<String>()
            for (tagIndex in 0 until binding.tagsGroup.childCount) {
                val tag = binding.tagsGroup.getChildAt(tagIndex)
                if (tag is Chip && tag.isChecked) {
                    tags.add(tag.text.toString().removePrefix("# "))
                }
            }

            val repository = RecipeRepository(requireContext())

            showLoading(true)
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
                    tags = tags
                )

                val success = repository.addRecipe(recipe)
                showLoading(false)

                if (success) {
                    Snackbar.make(view, "Recipe published!", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.parseColor("#4CAF50"))
                        .setTextColor(Color.WHITE)
                        .show()
                    findNavController().popBackStack()
                } else {
                    Snackbar.make(view, "Failed to publish recipe", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.RED)
                        .setTextColor(Color.WHITE)
                        .show()
                }
            }
        }
    }
}
