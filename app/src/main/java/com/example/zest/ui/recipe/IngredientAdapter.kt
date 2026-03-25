package com.example.zest.ui.recipe

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.databinding.IngredientItemBinding
import com.example.zest.model.Ingredient

class IngredientAdapter(
    private val ingredients: MutableList<Ingredient>,
    private val onListChanged: () -> Unit
) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    class ViewHolder(val binding: IngredientItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var qtyWatcher: TextWatcher? = null
        var nameWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IngredientItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        val b = holder.binding

        b.etQty.removeTextChangedListener(holder.qtyWatcher)
        b.etName.removeTextChangedListener(holder.nameWatcher)
        b.etQty.setText(ingredient.quantity)
        b.etName.setText(ingredient.name)

        holder.qtyWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) ingredients[pos].quantity = text.toString()
            }
        }

        holder.nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) ingredients[pos].name = text.toString()
            }
        }

        b.etQty.addTextChangedListener(holder.qtyWatcher)
        b.etName.addTextChangedListener(holder.nameWatcher)
        b.deleteIngredient.setOnClickListener {
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