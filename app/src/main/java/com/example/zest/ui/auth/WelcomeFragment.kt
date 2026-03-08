package com.example.zest.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            findNavController().navigate(R.id.feedFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnSignup = view.findViewById<MaterialButton>(R.id.btnSignup)

        btnLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_welcome_to_login
            )
        }

        btnSignup.setOnClickListener {
            findNavController().navigate(
                R.id.action_welcome_to_register
            )
        }
    }
}