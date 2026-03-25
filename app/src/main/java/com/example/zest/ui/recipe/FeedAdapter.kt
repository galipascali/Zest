package com.example.zest.ui.recipe

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
import com.example.zest.utils.loadImage
import com.example.zest.utils.loadImageWithCallback
import com.google.android.material.imageview.ShapeableImageView

class FeedAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onImagesLoaded: (() -> Unit)? = null
) : ListAdapter<Recipe, FeedAdapter.RecipeViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
        }
    }

    private var expectedImages = 0
    private var loadedImages = 0
    private var callbackFired = false

    override fun submitList(list: List<Recipe>?) {
        if (!callbackFired && !list.isNullOrEmpty()) {
            expectedImages = list.count { it.imageUrl.isNotBlank() }
            loadedImages = 0
            if (expectedImages == 0) {
                callbackFired = true
                onImagesLoaded?.invoke()
            }
        }
        super.submitList(list)
    }

    private fun onImageResult() {
        if (callbackFired) return
        loadedImages++
        if (loadedImages >= expectedImages) {
            callbackFired = true
            onImagesLoaded?.invoke()
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
            holder.image.loadImageWithCallback(recipe.imageUrl, R.drawable.welcome_background) { onImageResult() }
        } else {
            holder.image.setImageResource(R.drawable.welcome_background)
        }
        holder.avatar.loadImage(recipe.creatorPhoto)

        val apiTagTitle = holder.itemView.context.getString(R.string.api_tag_title)
        holder.apiTag.isVisible = recipe.creatorEmail.substringBefore("@") == apiTagTitle
        holder.itemView.setOnClickListener { onItemClick(recipe) }
    }
}