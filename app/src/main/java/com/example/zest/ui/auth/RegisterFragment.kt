package com.example.zest.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.google.android.material.button.MaterialButton

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnSignup = view.findViewById<MaterialButton>(R.id.btnSignup)
        val backArrow = view.findViewById<MaterialButton>(R.id.back_arrow)

        //TODO: authenticate user
        btnSignup.setOnClickListener {
            //          findNavController().navigate(
            //              R.id.action_login_to_feed
            //          )
        }
        btnLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_register_to_login
            )
        }
        backArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}