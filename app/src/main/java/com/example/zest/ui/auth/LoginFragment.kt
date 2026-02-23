package com.example.zest.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnSignup = view.findViewById<MaterialButton>(R.id.btnSignup)
        val backArrow = view.findViewById<MaterialButton>(R.id.back_arrow)

        //TODO: authenticate user
        btnLogin.setOnClickListener {
        //          findNavController().navigate(
        //              R.id.action_login_to_feed
        //          )
        }
        btnSignup.setOnClickListener {
            findNavController().navigate(
                R.id.action_login_to_register
            )
        }
        backArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}