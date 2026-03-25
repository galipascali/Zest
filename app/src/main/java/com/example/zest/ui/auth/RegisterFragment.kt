package com.example.zest.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import com.example.zest.utils.afterTextChanged
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
import com.google.firebase.firestore.FirebaseFirestore
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


        suspend fun saveUserToFirestore(userId: String, email: String): Boolean {
            return try {

                val user = hashMapOf(
                    "uid" to userId,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                FirebaseFirestore
                    .getInstance()
                    .collection("users")
                    .document(userId)
                    .set(user)
                    .await()

                true

            } catch (e: Exception) {
                false
            }
        }

        suspend fun registerUser(): String? {
            val email = emailField.text.toString()
            val pass = passwordField.text.toString()

            return try {
                val result = FirebaseAuth
                    .getInstance()
                    .createUserWithEmailAndPassword(email, pass)
                    .await()

                result.user?.uid

            } catch (e: Exception) {
                null
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
            viewLifecycleOwner.lifecycleScope.launch {
                showLoading(true)
                btnSignup.isEnabled = false

                val uid = registerUser()

                if (uid != null) {
                    val email = emailField.text.toString()
                    val saved = saveUserToFirestore(uid, email)

                    showLoading(false)
                    btnSignup.isEnabled = true

                    if (saved) {
                        val snackBar = Snackbar.make(view, "Registration Success", Snackbar.LENGTH_SHORT)
                        snackBar.setBackgroundTint(Color.GREEN)
                        snackBar.setTextColor(Color.WHITE)
                        snackBar.show()

                        findNavController().navigate(
                            R.id.action_register_to_feed
                        )
                    } else {
                        val snackBar = Snackbar.make(view, "Registration failed", Snackbar.LENGTH_SHORT)
                        snackBar.setBackgroundTint(Color.RED)
                        snackBar.setTextColor(Color.WHITE)
                        snackBar.show()
                    }
                } else {
                    showLoading(false)
                    btnSignup.isEnabled = true

                    val snackBar = Snackbar.make(view, "Registration failed", Snackbar.LENGTH_SHORT)
                    snackBar.setBackgroundTint(Color.RED)
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
            btnSignup.isEnabled = isFormValid()
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
            btnSignup.isEnabled = isFormValid()
        }

        confirmPasswordField.afterTextChanged { _ ->
            confirmPasswordValidate()
            btnSignup.isEnabled = isFormValid()
        }

        confirmPasswordField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && btnSignup.isEnabled) {
                btnSignup.performClick()
                true
            } else false
        }

    }
}