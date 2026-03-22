package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.model.Ingredient
import com.example.zest.R
import android.text.Editable
import android.text.TextWatcher

class IngredientAdapter(
    private val ingredients: MutableList<Ingredient>,
    private val onListChanged: () -> Unit
) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val qty = view.findViewById<EditText>(R.id.etQty)
        val name = view.findViewById<EditText>(R.id.etName)
        val delete = view.findViewById<ImageView>(R.id.deleteIngredient)
        var qtyWatcher: TextWatcher? = null
        var nameWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ingredient_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = ingredients.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]

        holder.qty.removeTextChangedListener(holder.qtyWatcher)
        holder.name.removeTextChangedListener(holder.nameWatcher)
        holder.qty.setText(ingredient.quantity)
        holder.name.setText(ingredient.name)

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

        holder.qty.addTextChangedListener(holder.qtyWatcher)
        holder.name.addTextChangedListener(holder.nameWatcher)
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