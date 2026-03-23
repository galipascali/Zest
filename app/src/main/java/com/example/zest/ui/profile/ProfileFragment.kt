package com.example.zest.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }
}