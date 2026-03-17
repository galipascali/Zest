package com.example.zest.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.model.Recipe
import com.example.zest.R
import com.example.zest.model.Ingredient
import com.example.zest.model.Step
import com.example.zest.ui.recipe.FeedAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class FeedFragment : Fragment(R.layout.fragment_feed) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        val categoryChips = view.findViewById<ChipGroup>(R.id.categoryChips)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val chips = listOf("All","Breakfast","Vegan","Fast & Easy")

        chips.forEach {

            val chip = Chip(requireContext())

            chip.text = it
            chip.isCheckable = true

            chip.setChipBackgroundColorResource(R.color.chip_background_selector)
            chip.setTextColor(resources.getColorStateList(R.color.chip_text_selector))

            categoryChips.addView(chip)
        }
        val mockRecipes = listOf(
            Recipe(
                id = "1",
                userId = "user_1",
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
                title = "Avocado Toast",
                time = 10,
                servings = 2,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient(2, "slices sourdough bread"),
                    Ingredient(1, "ripe avocado"),
                    Ingredient(1, "lemon"),
                    Ingredient(2, "eggs"),
                    Ingredient(1, "pinch of chili flakes")
                ),
                steps = listOf(
                    Step(1, "Toast the sourdough slices until golden and crispy."),
                    Step(2, "Mash the avocado with lemon juice, salt and pepper."),
                    Step(3, "Fry or poach the eggs to your liking."),
                    Step(4, "Spread avocado on toast, top with egg and chili flakes.")
                ),
                tags = listOf("Easy Meal", "Vegan", "Breakfast", "Under 30m")
            ),
            Recipe(
                id = "2",
                userId = "user_1",
                imageUrl = "https://images.unsplash.com/photo-1563379926898-05f4575a45d8",
                title = "Spaghetti Carbonara",
                time = 25,
                servings = 4,
                difficulty = "Medium",
                ingredients = listOf(
                    Ingredient(400, "g spaghetti"),
                    Ingredient(150, "g pancetta or bacon"),
                    Ingredient(4, "egg yolks"),
                    Ingredient(100, "g parmesan, grated"),
                    Ingredient(2, "cloves garlic")
                ),
                steps = listOf(
                    Step(1, "Cook spaghetti in salted boiling water until al dente."),
                    Step(2, "Fry pancetta with garlic until crispy."),
                    Step(3, "Whisk egg yolks with parmesan and black pepper."),
                    Step(4, "Toss hot pasta with pancetta, remove from heat."),
                    Step(5, "Add egg mixture quickly, tossing to create a creamy sauce.")
                ),
                tags = listOf("Dinner", "Italian", "Classic")
            ),
            Recipe(
                id = "3",
                userId = "user_2",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd",
                title = "Greek Salad",
                time = 15,
                servings = 3,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient(3, "tomatoes, chopped"),
                    Ingredient(1, "cucumber, sliced"),
                    Ingredient(1, "red onion, thinly sliced"),
                    Ingredient(200, "g feta cheese"),
                    Ingredient(80, "g kalamata olives"),
                    Ingredient(3, "tbsp olive oil")
                ),
                steps = listOf(
                    Step(1, "Chop tomatoes, cucumber and red onion into bite-sized pieces."),
                    Step(2, "Combine vegetables in a large bowl with olives."),
                    Step(3, "Top with feta cheese and drizzle with olive oil."),
                    Step(4, "Season with salt, oregano and serve immediately.")
                ),
                tags = listOf("Salad", "Vegan", "Under 30m", "Healthy")
            ),
            Recipe(
                id = "4",
                userId = "user_2",
                imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38",
                title = "Margherita Pizza",
                time = 45,
                servings = 4,
                difficulty = "Medium",
                ingredients = listOf(
                    Ingredient(500, "g pizza dough"),
                    Ingredient(200, "ml tomato sauce"),
                    Ingredient(250, "g fresh mozzarella"),
                    Ingredient(10, "fresh basil leaves"),
                    Ingredient(2, "tbsp olive oil")
                ),
                steps = listOf(
                    Step(1, "Preheat oven to 250°C with a pizza stone or tray inside."),
                    Step(2, "Stretch dough into a thin round on a floured surface."),
                    Step(3, "Spread tomato sauce evenly, leaving a border for the crust."),
                    Step(4, "Tear mozzarella and distribute over the sauce."),
                    Step(5, "Bake for 10-12 minutes until crust is golden and cheese is bubbling."),
                    Step(6, "Top with fresh basil and a drizzle of olive oil before serving.")
                ),
                tags = listOf("Dinner", "Italian", "Weekend")
            )
        )

        recycler.adapter = FeedAdapter(mockRecipes)
    }
}