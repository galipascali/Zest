package com.example.zest.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Recipe
import com.example.zest.utils.loadImageWithCallback

class ProfileRecipeAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onImagesLoaded: (() -> Unit)? = null
) : ListAdapter<Recipe, ProfileRecipeAdapter.ViewHolder>(DIFF_CALLBACK) {

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
        callbackFired = false
        loadedImages = 0
        expectedImages = list?.count { it.imageUrl.isNotBlank() } ?: 0
        if (expectedImages == 0) { callbackFired = true; onImagesLoaded?.invoke() }
        super.submitList(list)
    }

    private fun onImageResult() {
        if (callbackFired) return
        if (++loadedImages >= expectedImages) { callbackFired = true; onImagesLoaded?.invoke() }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgRecipe)
        val title: TextView = view.findViewById(R.id.recipeTitle)
        val editButton: ImageView = view.findViewById(R.id.editButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_profile_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.title.text = item.title
        holder.img.loadImageWithCallback(item.imageUrl, R.drawable.welcome_background) { onImageResult() }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }
}