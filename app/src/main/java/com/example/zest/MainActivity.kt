package com.example.zest

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focused = currentFocus
            if (focused is EditText) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focused.windowToken, 0)
                focused.clearFocus()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        val bottomBar = findViewById<LinearLayout>(R.id.bottomBar)
        val addRecipeButton = findViewById<FloatingActionButton>(R.id.addRecipeButton)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedFragment,
                R.id.profileFragment
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
            navController.navigate(R.id.profileFragment)
        }

        findViewById<FloatingActionButton>(R.id.addRecipeButton).setOnClickListener {
            navController.navigate(R.id.createRecipeFragment)
        }
    }
}
