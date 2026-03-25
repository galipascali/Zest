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
import com.example.zest.utils.loadImage

class ProfileRecipeAdapter(
    private val onItemClick: (Recipe) -> Unit
) : ListAdapter<Recipe, ProfileRecipeAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
        }
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
        holder.img.loadImage(item.imageUrl, R.drawable.welcome_background)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }
}