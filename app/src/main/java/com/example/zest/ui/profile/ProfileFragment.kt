package com.example.zest.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import android.widget.ImageButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogout = view.findViewById<ImageButton>(R.id.btnLogout)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipes)
        val adapter = ProfileRecipeAdapter{ recipe ->
            val action = ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id)
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
        }

        btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_profile_to_welcome)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}