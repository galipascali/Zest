package com.example.zest.ui.recipe

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.model.Recipe
import com.example.zest.R
import com.google.android.material.imageview.ShapeableImageView

class FeedAdapter(private val onItemClick: (Recipe) -> Unit) :
    ListAdapter<Recipe, FeedAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
        }
    }

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.recipeTitle)
        val creator: TextView = view.findViewById(R.id.creatorName)
        val image: ImageView = view.findViewById(R.id.recipeImage)
        val avatar: ShapeableImageView = view.findViewById(R.id.userAvatar)
        val apiTag: TextView = view.findViewById(R.id.recipeApiTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_feed_item, parent, false)

        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = getItem(position)

        holder.title.text = recipe.title
        holder.creator.text = recipe.creatorEmail.substringBefore("@")

        if (recipe.imageUrl.isNotBlank()) {
            try {
                val bytes = Base64.decode(recipe.imageUrl, Base64.DEFAULT)
                holder.image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } catch (_: Exception) {}
        }

        if (recipe.creatorPhoto.isNotBlank()) {
            try {
                val bytes = Base64.decode(recipe.creatorPhoto, Base64.DEFAULT)
                holder.avatar.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } catch (_: Exception) {}
        }

        val apiTagTitle = holder.itemView.context.getString(R.string.api_tag_title)
        holder.apiTag.isVisible = recipe.creatorEmail.substringBefore("@") == apiTagTitle
        holder.itemView.setOnClickListener { onItemClick(recipe) }
    }
}