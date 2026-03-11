package com.example.zest.ui.recipe

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Ingredient
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class CreateRecipeFragment : Fragment(R.layout.fragment_create_recipe) {
    private val ingredients = mutableListOf<Ingredient>()
    private lateinit var recyclerAdapter: IngredientAdapter
    private lateinit var ingredientsCountText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = resources.getStringArray(R.array.difficulty_levels)
        val difficultyDropdown  = view.findViewById<MaterialAutoCompleteTextView>(R.id.etDifficulty)
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        val recycler = view.findViewById<RecyclerView>(R.id.ingredientsRecyclerView)
        val ingredientsRecyclerView = view.findViewById<RecyclerView>(R.id.ingredientsRecyclerView)
        val emptyIngredient =  view.findViewById<LinearLayout>(R.id.emptyIngredient)
        difficultyDropdown.setAdapter(difficultyAdapter)

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

        recyclerAdapter = IngredientAdapter(ingredients) {
            updateIngredientsCount()
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = recyclerAdapter

        view.findViewById<LinearLayout>(R.id.addIngredientButton).setOnClickListener {
            ingredients.add(Ingredient())
            recyclerAdapter.notifyItemInserted(ingredients.size - 1)
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

                recyclerAdapter.moveItem(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })

        touchHelper.attachToRecyclerView(recycler)
    }
}