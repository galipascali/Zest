package com.example.zest.database

import androidx.room.TypeConverter
import com.example.zest.model.Ingredient
import com.example.zest.model.Step
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String = gson.toJson(value)

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> {
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStepList(value: List<Step>): String = gson.toJson(value)

    @TypeConverter
    fun toStepList(value: String): List<Step> {
        val type = object : TypeToken<List<Step>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}