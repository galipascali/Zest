package com.example.zest.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val creatorEmail: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val imageUrl: String = "",
    val title: String = "",
    val time: Int = 0,
    val servings: Int = 0,
    val difficulty: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<Step> = emptyList(),
    val tags: List<String> = emptyList()
)