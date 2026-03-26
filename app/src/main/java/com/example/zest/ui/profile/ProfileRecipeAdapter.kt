package com.example.zest.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.databinding.RecipeProfileItemBinding
import com.example.zest.model.Recipe
import com.example.zest.utils.loadImage

class ProfileRecipeAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onEditClick: (Recipe) -> Unit
) : ListAdapter<Recipe, ProfileRecipeAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
        }
    }

    class ViewHolder(val binding: RecipeProfileItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecipeProfileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.recipeTitle.text = item.title
        holder.binding.imgRecipe.loadImage(item.imageUrl, R.drawable.welcome_background)
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.binding.editButton.setOnClickListener { onEditClick(item) }
    }
}