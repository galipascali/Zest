package com.example.zest

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        val bottomBar = findViewById<LinearLayout>(R.id.bottomBar)
        val addRecipeButton = findViewById<FloatingActionButton>(R.id.addRecipeButton)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedFragment,
//                R.id.profileFragment
                    -> {
                    bottomBar.visibility = View.VISIBLE
                    addRecipeButton.visibility = View.VISIBLE
                }
                else -> {
                    bottomBar.visibility = View.GONE
                    addRecipeButton.visibility = View.GONE
                }
            }
        }

        val exploreBtn = findViewById<LinearLayout>(R.id.exploreBtn)
        val profileBtn = findViewById<LinearLayout>(R.id.profileBtn)
        val exploreIcon = exploreBtn.findViewById<ImageView>(R.id.exploreIcon)
        val exploreText = exploreBtn.findViewById<TextView>(R.id.exploreText)
        val profileIcon = profileBtn.findViewById<ImageView>(R.id.profileIcon)
        val profileText = profileBtn.findViewById<TextView>(R.id.profileText)
        val green = ContextCompat.getColor(this, R.color.app_green)
        val grey = ContextCompat.getColor(this, R.color.medium_grey)

        fun selectExplore() {
            ImageViewCompat.setImageTintList(exploreIcon, ColorStateList.valueOf(green))
            exploreText.setTextColor(green)
            ImageViewCompat.setImageTintList(profileIcon, ColorStateList.valueOf(grey))
            profileText.setTextColor(grey)
        }

        fun selectProfile() {
            ImageViewCompat.setImageTintList(profileIcon, ColorStateList.valueOf(green))
            profileText.setTextColor(green)
            ImageViewCompat.setImageTintList(exploreIcon, ColorStateList.valueOf(grey))
            exploreText.setTextColor(grey)
        }

        selectExplore()

        exploreBtn.setOnClickListener {
            selectExplore()
            navController.navigate(R.id.feedFragment)
        }

        profileBtn.setOnClickListener {
            selectProfile()
//            navController.navigate(R.id.profileFragment)
        }

        findViewById<FloatingActionButton>(R.id.addRecipeButton).setOnClickListener {
            navController.navigate(R.id.createRecipeFragment)
        }
    }
}
