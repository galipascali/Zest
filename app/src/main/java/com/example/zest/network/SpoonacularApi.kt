package com.example.zest.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val SPOONACULAR_API_KEY = "e63d37eb88ed4eb898a39c5c53e8298a"

interface SpoonacularApi {

    @GET("recipes/random")
    suspend fun getRandomRecipes(
        @Query("number") number: Int,
        @Query("apiKey") apiKey: String
    ): SpoonacularRandomResponse

    companion object {
        val instance: SpoonacularApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SpoonacularApi::class.java)
        }
    }
}