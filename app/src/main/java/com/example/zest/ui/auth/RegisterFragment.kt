package com.example.zest.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnSignup = view.findViewById<MaterialButton>(R.id.btnSignup)
        val backArrow = view.findViewById<MaterialButton>(R.id.back_arrow)
        val emailField = view.findViewById<TextInputEditText>(R.id.etEmail)
        val passwordField = view.findViewById<TextInputEditText>(R.id.etPassword)
        val confirmPasswordField = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailTextField)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordTextField)
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(R.id.confirmPasswordTextField)
        var emailValidated = false
        var passwordValidated = false
        var confirmPasswordValidated = false

        fun isFormValid(): Boolean {
            return emailValidated &&
                    passwordValidated &&
                    confirmPasswordValidated
        }

        fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
            this.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    afterTextChanged.invoke(editable.toString())
                    btnSignup.isEnabled = isFormValid()
                }
            })
        }

        fun confirmPasswordValidate() {
            if (confirmPasswordField.text.toString() != passwordField.text.toString()) {
                confirmPasswordLayout.error = "Passwords do not match"
                confirmPasswordValidated = false
            } else {
                confirmPasswordLayout.error = null
                confirmPasswordValidated = true
            }
        }

        btnSignup.setOnClickListener {
                //TODO: authenticate user
                findNavController().navigate(
                    R.id.action_register_to_feed
                )
        }

        btnLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_register_to_login
            )
        }

        backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        emailField.afterTextChanged { email ->
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Invalid email address"
                emailValidated = false
            }else {
                emailLayout.error = null
                emailValidated = true
            }
        }

        passwordField.afterTextChanged { password ->
            if (password.length < 6) {
                passwordLayout.error = "Password must be at least 6 characters"
                passwordValidated = false
            } else {
                passwordLayout.error = null
                passwordValidated = true
            }

            confirmPasswordValidate()
        }

        confirmPasswordField.afterTextChanged { _ -> confirmPasswordValidate() }
    }
}