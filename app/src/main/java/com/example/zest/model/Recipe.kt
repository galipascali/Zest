package com.example.zest.model

data class Recipe(
    val id: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val imageUrl: String,
    val title: String,
    val time: Number,
    val servings: Number,
    val difficulty: String,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val tags: List<String>
)