package com.example.zest.ui.recipe

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.text.TextWatcher
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Ingredient
import com.example.zest.model.Step
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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
    private lateinit var IngredientRecyclerAdapter: IngredientAdapter
    private lateinit var ingredientsCountText: TextView
    private val steps = mutableListOf<Step>()
    private lateinit var stepsRecyclerAdapter: StepAdapter

    lateinit var tagsGroup: FlexboxLayout

    private fun createTag(text: String, isChecked: Boolean = false ): Chip {
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
            setMargins(margin, 0, 0, 0)        }

        chip.layoutParams = params

        return chip
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialCardView>(R.id.photoContainer).setOnClickListener {
            showImagePickerDialog()
        }

        val items = resources.getStringArray(R.array.difficulty_levels)
        val difficultyDropdown  = view.findViewById<MaterialAutoCompleteTextView>(R.id.etDifficulty)
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        difficultyDropdown.setAdapter(difficultyAdapter)

        val ingredientsRecyclerView = view.findViewById<RecyclerView>(R.id.ingredientsRecyclerView)
        val emptyIngredient =  view.findViewById<LinearLayout>(R.id.emptyIngredient)

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
        }

        IngredientRecyclerAdapter = IngredientAdapter(ingredients) {
            updateIngredientsCount()
        }
        ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ingredientsRecyclerView.adapter = IngredientRecyclerAdapter

        view.findViewById<LinearLayout>(R.id.addIngredientButton).setOnClickListener {
            ingredients.add(Ingredient())
            IngredientRecyclerAdapter.notifyItemInserted(ingredients.size - 1)
            updateIngredientsCount()
        }

        ingredientsCountText = view.findViewById(R.id.ingredientsCount)

        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                IngredientRecyclerAdapter.moveItem(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })

        touchHelper.attachToRecyclerView(ingredientsRecyclerView)


        val stepsRecyclerView = view.findViewById<RecyclerView>(R.id.stepsRecyclerView)
        val emptyStep =  view.findViewById<LinearLayout>(R.id.emptyStep)

        fun updateStepsCount() {
            val count = steps.size
            if (count > 0) {
                emptyStep.visibility = View.GONE
                stepsRecyclerView.visibility = View.VISIBLE
            } else {
                emptyStep.visibility = View.VISIBLE
                stepsRecyclerView.visibility = View.GONE

            }
        }

        stepsRecyclerAdapter = StepAdapter(steps) {
            updateStepsCount()
        }
        stepsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        stepsRecyclerView.adapter = stepsRecyclerAdapter

        view.findViewById<LinearLayout>(R.id.addStepButton).setOnClickListener {
            steps.add(Step())
            stepsRecyclerAdapter.notifyItemInserted(steps.size - 1)
            updateStepsCount()
        }

        tagsGroup = view.findViewById(R.id.tagsGroup)
        val tags = listOf("Easy Meal", "Vegan", "Under 30m", "Breakfast")

        tags.forEach {
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
    }
}
