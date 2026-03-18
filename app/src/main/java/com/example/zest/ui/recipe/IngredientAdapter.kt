package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.model.Ingredient
import com.example.zest.R

class IngredientAdapter(
    private val ingredients: MutableList<Ingredient>,
    private val onListChanged: () -> Unit
) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val qty = view.findViewById<EditText>(R.id.etQty)
        val name = view.findViewById<EditText>(R.id.etName)
        val delete = view.findViewById<ImageView>(R.id.deleteIngredient)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ingredient_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]

        holder.qty.setText(ingredient.quantity)
        holder.name.setText(ingredient.name)
        holder.delete.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                ingredients.removeAt(pos)
                notifyItemRemoved(pos)
                onListChanged()
            }
        }
    }

    fun moveItem(from: Int, to: Int) {
        val item = ingredients.removeAt(from)
        ingredients.add(to, item)
        notifyItemMoved(from, to)
    }
}