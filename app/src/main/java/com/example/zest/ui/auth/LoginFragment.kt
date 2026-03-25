package com.example.zest.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import com.example.zest.utils.afterTextChanged
import com.example.zest.utils.showLoading
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnSignup = view.findViewById<MaterialButton>(R.id.btnSignup)
        val backArrow = view.findViewById<MaterialButton>(R.id.back_arrow)
        val emailField = view.findViewById<TextInputEditText>(R.id.etEmail)
        val passwordField = view.findViewById<TextInputEditText>(R.id.etPassword)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailTextField)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordTextField)
        fun showLoading(show: Boolean) {
            this.showLoading(show)
            btnLogin.isEnabled = !show
        }

        var emailValidated = false
        var passwordValidated = false

        fun isFormValid(): Boolean {
            return emailValidated &&
                    passwordValidated
        }


        btnLogin.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            showLoading(true)
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                showLoading(false)
                btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    val snackBar = Snackbar.make(view, "Login Success", Snackbar.LENGTH_SHORT)
                    snackBar.setBackgroundTint(Color.GREEN)
                    snackBar.setTextColor(Color.WHITE)
                    snackBar.show()
                    findNavController().navigate(R.id.action_login_to_feed)
                } else {
                   val snackBar = Snackbar.make(view, "Login Failed", Snackbar.LENGTH_SHORT)
                    snackBar.setBackgroundTint(Color.RED)
                    snackBar.setTextColor(Color.WHITE)
                    snackBar.show()
                }
            }
        }

        passwordField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && btnLogin.isEnabled) {
                btnLogin.performClick()
                true
            } else false
        }

        btnSignup.setOnClickListener {
            findNavController().navigate(
                R.id.action_login_to_register
            )
        }

        backArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        emailField.afterTextChanged { email ->
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Invalid email address"
                emailValidated = false
            } else {
                emailLayout.error = null
                emailValidated = true
            }
            btnLogin.isEnabled = isFormValid()
        }

        passwordField.afterTextChanged { password ->
            if (password.length < 6) {
                passwordLayout.error = "Password must be at least 6 characters"
                passwordValidated = false
            } else {
                passwordLayout.error = null
                passwordValidated = true
            }
            btnLogin.isEnabled = isFormValid()
        }
    }
}