package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Ingredient

class IngredientDetailAdapter(private val ingredients: List<Ingredient>) :
    RecyclerView.Adapter<IngredientDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val qty: TextView = view.findViewById(R.id.ingredientsQty)
        val name: TextView = view.findViewById(R.id.ingredientsTitle)
        val number: TextView = view.findViewById(R.id.ingredientNumber)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ingredient_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.qty.text = ingredient.quantity.ifBlank { "-" }
        holder.name.text = ingredient.name
        holder.number.text = (position + 1).toString()
    }
}
