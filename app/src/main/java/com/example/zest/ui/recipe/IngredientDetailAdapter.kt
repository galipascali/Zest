package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.databinding.IngredientDetailItemBinding
import com.example.zest.model.Ingredient

class IngredientDetailAdapter(private val ingredients: List<Ingredient>) :
    RecyclerView.Adapter<IngredientDetailAdapter.ViewHolder>() {

    class ViewHolder(val binding: IngredientDetailItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IngredientDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.binding.ingredientsQty.text = ingredient.quantity.ifBlank { "-" }
        holder.binding.ingredientsTitle.text = ingredient.name
        holder.binding.ingredientNumber.text = (position + 1).toString()
    }
}