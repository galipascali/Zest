package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.databinding.RecipeFeedItemBinding
import com.example.zest.model.Recipe
import com.example.zest.utils.loadImage

class FeedAdapter(
    private val onItemClick: (Recipe) -> Unit
) : ListAdapter<Recipe, FeedAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
        }
    }

    class RecipeViewHolder(val binding: RecipeFeedItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeFeedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)
        val b = holder.binding

        b.recipeTitle.text = recipe.title
        b.creatorName.text = recipe.creatorEmail.substringBefore("@")
        b.recipeImage.loadImage(recipe.imageUrl, R.drawable.welcome_background)
        b.userAvatar.loadImage(recipe.creatorPhoto)

        val apiTagTitle = holder.itemView.context.getString(R.string.api_tag_title)
        b.recipeApiTag.isVisible = recipe.creatorEmail.substringBefore("@") == apiTagTitle
        holder.itemView.setOnClickListener { onItemClick(recipe) }
    }
}
