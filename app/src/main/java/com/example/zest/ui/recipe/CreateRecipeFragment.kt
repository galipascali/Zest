package com.example.zest.ui.recipe

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Ingredient
import com.example.zest.model.Recipe
import com.example.zest.model.Step
import com.example.zest.repository.RecipeRepository
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class CreateRecipeFragment : Fragment(R.layout.fragment_create_recipe) {
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
        val imageView = view?.findViewById<ImageView>(R.id.recipePhoto) ?: return
        val placeholder = view?.findViewById<LinearLayout>(R.id.photoPlaceholder) ?: return
        imageView.setImageURI(uri)
        imageView.visibility = View.VISIBLE
        placeholder.visibility = View.GONE
    }

    private fun showImagePickerDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setItems(arrayOf("Take a photo", "Choose from gallery")) { _, which ->
                when (which) {
                    0 -> {
                        val file = File(requireContext().cacheDir, "recipe_photo.jpg")
                        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
                        takePhotoLauncher.launch(photoUri)
                    }
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }
    private val ingredients = mutableListOf<Ingredient>()
    private lateinit var ingredientRecyclerAdapter: IngredientAdapter
    private lateinit var ingredientsCountText: TextView
    private val steps = mutableListOf<Step>()
    private lateinit var stepsRecyclerAdapter: StepAdapter
    private lateinit var tagsGroup: FlexboxLayout
    private lateinit var publishButton: MaterialButton

    private var titleValidated = false
    private var timeValidated = false
    private var servingsValidated = false
    private var difficultyValidated = false

    private fun isFormValid() =
        titleValidated && timeValidated && servingsValidated && difficultyValidated &&
                ingredients.isNotEmpty() && steps.isNotEmpty()

    private fun updateButtonState() {
        publishButton.isEnabled = isFormValid()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        publishButton = view.findViewById(R.id.btnLogin)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val overlay = view.findViewById<View>(R.id.loadingOverlay)
        val backArrow = view.findViewById<MaterialButton>(R.id.back_arrow)

        fun showLoading(show: Boolean) {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            overlay.visibility = if (show) View.VISIBLE else View.GONE
            publishButton.isEnabled = !show
        }

        backArrow.setOnClickListener { findNavController().popBackStack() }

        view.findViewById<MaterialCardView>(R.id.photoContainer).setOnClickListener {
            showImagePickerDialog()
        }

        val items = resources.getStringArray(R.array.difficulty_levels)
        val difficultyDropdown = view.findViewById<MaterialAutoCompleteTextView>(R.id.etDifficulty)
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        difficultyDropdown.setAdapter(difficultyAdapter)
        difficultyDropdown.setOnItemClickListener { _, _, _, _ ->
            difficultyValidated = true
            updateButtonState()
        }

        val titleLayout = view.findViewById<TextInputLayout>(R.id.titleTextField)
        val titleField = view.findViewById<TextInputEditText>(R.id.etTitle)
        titleField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrBlank()) {
                    titleLayout.error = "Title is required"
                    titleValidated = false
                } else {
                    titleLayout.error = null
                    titleValidated = true
                }
                updateButtonState()
            }
        })

        val timeLayout = view.findViewById<TextInputLayout>(R.id.timeTextField)
        val timeField = view.findViewById<TextInputEditText>(R.id.etTime)
        timeField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val value = text?.toString()?.toIntOrNull()
                if (value == null || value <= 0) {
                    timeLayout.error = "Enter a valid time"
                    timeValidated = false
                } else {
                    timeLayout.error = null
                    timeValidated = true
                }
                updateButtonState()
            }
        })

        val servingsLayout = view.findViewById<TextInputLayout>(R.id.servingsTextField)
        val servingsField = view.findViewById<TextInputEditText>(R.id.etServings)
        servingsField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val value = text?.toString()?.toIntOrNull()
                if (value == null || value <= 0) {
                    servingsLayout.error = "Enter a valid servings amount"
                    servingsValidated = false
                } else {
                    servingsLayout.error = null
                    servingsValidated = true
                }
                updateButtonState()
            }
        })

        val ingredientsRecyclerView = view.findViewById<RecyclerView>(R.id.ingredientsRecyclerView)
        val emptyIngredient = view.findViewById<LinearLayout>(R.id.emptyIngredient)
        ingredientsCountText = view.findViewById(R.id.ingredientsCount)

        fun updateIngredientsCount() {
            val count = ingredients.size
            if (count > 0) {
                emptyIngredient.visibility = View.GONE
                ingredientsRecyclerView.visibility = View.VISIBLE
            } else {
                emptyIngredient.visibility = View.VISIBLE
                ingredientsRecyclerView.visibility = View.GONE
            }
            ingredientsCountText.text = "$count items"
            updateButtonState()
        }

        ingredientRecyclerAdapter = IngredientAdapter(ingredients) { updateIngredientsCount() }
        ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ingredientsRecyclerView.adapter = ingredientRecyclerAdapter

        view.findViewById<LinearLayout>(R.id.addIngredientButton).setOnClickListener {
            ingredients.add(Ingredient())
            ingredientRecyclerAdapter.notifyItemInserted(ingredients.size - 1)
            updateIngredientsCount()
        }

        val ingredientTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){
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

        ingredientTouchHelper.attachToRecyclerView(ingredientsRecyclerView)

        val stepsRecyclerView = view.findViewById<RecyclerView>(R.id.stepsRecyclerView)
        val emptyStep = view.findViewById<LinearLayout>(R.id.emptyStep)

        fun updateStepsCount() {
            if (steps.size > 0) {
                emptyStep.visibility = View.GONE
                stepsRecyclerView.visibility = View.VISIBLE
            } else {
                emptyStep.visibility = View.VISIBLE
                stepsRecyclerView.visibility = View.GONE
            }
            updateButtonState()
        }

        stepsRecyclerAdapter = StepAdapter(steps) { updateStepsCount() }
        stepsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        stepsRecyclerView.adapter = stepsRecyclerAdapter

        view.findViewById<LinearLayout>(R.id.addStepButton).setOnClickListener {
            steps.add(Step())
            stepsRecyclerAdapter.notifyItemInserted(steps.size - 1)
            updateStepsCount()
        }

        tagsGroup = view.findViewById(R.id.tagsGroup)
        listOf("Easy Meal", "Vegan", "Under 30m", "Breakfast").forEach {
            tagsGroup.addView(createTag(it))
        }

        val tagInput = view.findViewById<EditText>(R.id.tagInput)
        tagInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                val minWidth = (44 * resources.displayMetrics.density).toInt()
                val newWidth = maxOf(minWidth, tagInput.paint.measureText(text).toInt() + (32 * resources.displayMetrics.density).toInt())
                tagInput.layoutParams = tagInput.layoutParams.also { it.width = newWidth }
            }
        })

        tagInput.setOnFocusChangeListener { _, hasFocus ->
            tagInput.hint = if (!hasFocus && tagInput.text.isNullOrEmpty()) "+" else ""
        }

        tagInput.setOnEditorActionListener { _, _, _ ->
            val text = tagInput.text.toString().trim()
            if (text.isNotEmpty()) {
                tagsGroup.addView(createTag(text, true), tagsGroup.childCount)
                tagInput.text.clear()
            }
            true
        }

        publishButton.setOnClickListener {
            val title = titleField.text.toString().trim()
            val time = timeField.text.toString().toInt()
            val servings = servingsField.text.toString().toInt()
            val difficulty = difficultyDropdown.text.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            val tags = mutableListOf<String>()
            for (tagIndex in 0 until tagsGroup.childCount) {
                val tag = tagsGroup.getChildAt(tagIndex)
                if (tag is Chip && tag.isChecked) {
                    tags.add(tag.text.toString().removePrefix("# "))
                }
            }

            val repository = RecipeRepository(requireContext())

            showLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                val recipe = Recipe(
                    userId = userId,
                    creatorEmail = FirebaseAuth.getInstance().currentUser?.email ?: "",
                    imageUrl = "", //TODO
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
