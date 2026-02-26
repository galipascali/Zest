package com.example.zest.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        val confirmPasswordLayout =
            view.findViewById<TextInputLayout>(R.id.confirmPasswordTextField)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val overlay = view.findViewById<View>(R.id.loadingOverlay)

        fun showLoading(show: Boolean) {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            overlay.visibility = if (show) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !show
        }

        var emailValidated = false
        var passwordValidated = false
        var confirmPasswordValidated = false

        fun isFormValid(): Boolean {
            return emailValidated && passwordValidated && confirmPasswordValidated
        }

        fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
            this.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    afterTextChanged.invoke(editable.toString())
                    btnSignup.isEnabled = isFormValid()
                }
            })
        }

        suspend fun registerUser(): Boolean {
            val email = emailField.text.toString()
            val pass = passwordField.text.toString()

            return try {
                val authResult =
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass).await()

                authResult.user != null

            } catch (e: Exception) {
                false
            }
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
            showLoading(true)
            btnLogin.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {

                showLoading(true)
                btnSignup.isEnabled = false

                val success = registerUser()

                showLoading(false)
                btnSignup.isEnabled = true

                if (success) {
                    val snackBar = Snackbar.make(view, "Register Success", Snackbar.LENGTH_SHORT)
                    snackBar.setBackgroundTint(Color.GREEN)
                    snackBar.setTextColor(Color.WHITE)
                    snackBar.show()

                    findNavController().navigate(
                        R.id.action_register_to_feed
                    )
                } else {
                    val snackBar = Snackbar.make(view, "Register Failed", Snackbar.LENGTH_SHORT)
                    snackBar.setBackgroundTint(Color.GREEN)
                    snackBar.setTextColor(Color.WHITE)
                    snackBar.show()
                }
            }
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
            } else {
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