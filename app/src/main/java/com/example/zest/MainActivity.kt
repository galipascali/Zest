package com.example.zest

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.zest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedFragment,
                R.id.profileFragment -> {
                    binding.bottomBar.visibility = android.view.View.VISIBLE
                    binding.addRecipeButton.visibility = android.view.View.VISIBLE
                }
                else -> {
                    binding.bottomBar.visibility = android.view.View.GONE
                    binding.addRecipeButton.visibility = android.view.View.GONE
                }
            }
        }

        val green = ContextCompat.getColor(this, R.color.app_green)
        val grey = ContextCompat.getColor(this, R.color.medium_grey)

        fun selectExplore() {
            ImageViewCompat.setImageTintList(binding.exploreIcon, ColorStateList.valueOf(green))
            binding.exploreText.setTextColor(green)
            ImageViewCompat.setImageTintList(binding.profileIcon, ColorStateList.valueOf(grey))
            binding.profileText.setTextColor(grey)
        }

        fun selectProfile() {
            ImageViewCompat.setImageTintList(binding.profileIcon, ColorStateList.valueOf(green))
            binding.profileText.setTextColor(green)
            ImageViewCompat.setImageTintList(binding.exploreIcon, ColorStateList.valueOf(grey))
            binding.exploreText.setTextColor(grey)
        }

        selectExplore()

        binding.exploreBtn.setOnClickListener {
            selectExplore()
            navController.navigate(R.id.feedFragment)
        }

        binding.profileBtn.setOnClickListener {
            selectProfile()
            navController.navigate(R.id.profileFragment)
        }

        binding.addRecipeButton.setOnClickListener {
            navController.navigate(R.id.createRecipeFragment)
        }
    }
}