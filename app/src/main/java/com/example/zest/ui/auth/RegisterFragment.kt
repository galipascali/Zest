package com.example.zest.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.example.zest.databinding.FragmentRegisterBinding
import com.example.zest.utils.afterTextChanged
import com.example.zest.utils.showLoading
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var emailValidated = false
        var passwordValidated = false
        var confirmPasswordValidated = false
        fun isFormValid() = emailValidated && passwordValidated && confirmPasswordValidated

        fun showLoading(show: Boolean) {
            this.showLoading(show)
            binding.btnLogin.isEnabled = !show
        }

        suspend fun saveUserToFirestore(userId: String, email: String): Boolean {
            return try {
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .set(hashMapOf("uid" to userId, "email" to email, "createdAt" to System.currentTimeMillis()))
                    .await()
                true
            } catch (e: Exception) { false }
        }

        suspend fun registerUser(): Pair<String?, Exception?> {
            return try {
                val uid = FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                    .await().user?.uid
                Pair(uid, null)
            } catch (e: Exception) { Pair(null, e) }
        }

        fun confirmPasswordValidate() {
            if (binding.etConfirmPassword.text.toString() != binding.etPassword.text.toString()) {
                binding.confirmPasswordTextField.error = "Passwords do not match"
                confirmPasswordValidated = false
            } else {
                binding.confirmPasswordTextField.error = null
                confirmPasswordValidated = true
            }
        }

        binding.btnSignup.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                showLoading(true)
                binding.btnSignup.isEnabled = false
                val (uid, error) = registerUser()
                if (uid != null) {
                    val saved = saveUserToFirestore(uid, binding.etEmail.text.toString())
                    showLoading(false)
                    binding.btnSignup.isEnabled = true
                    if (saved) {
                        Snackbar.make(view, "Registration Success", Snackbar.LENGTH_SHORT).setBackgroundTint(requireContext().getColor(R.color.success)).setTextColor(Color.WHITE).show()
                        findNavController().navigate(R.id.action_register_to_feed)
                    } else {
                        Snackbar.make(view, "Account created but failed to save profile", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(requireContext().getColor(R.color.error)).setTextColor(Color.WHITE).show()
                    }
                } else {
                    showLoading(false)
                    binding.btnSignup.isEnabled = true
                    val message = when ((error as? FirebaseAuthException)?.errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists"
                        "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                        "ERROR_INVALID_EMAIL" -> "Invalid email address"
                        "ERROR_NETWORK_REQUEST_FAILED" -> "Network error, check your connection"
                        else -> "Registration failed"
                    }
                    Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(requireContext().getColor(R.color.error))
                        .setTextColor(Color.WHITE).show()
                }
            }
        }

        binding.btnLogin.setOnClickListener { findNavController().navigate(R.id.action_register_to_login) }
        binding.backArrow.setOnClickListener { findNavController().popBackStack() }

        binding.etEmail.afterTextChanged { email ->
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailTextField.error = "Invalid email address"
                emailValidated = false
            } else {
                binding.emailTextField.error = null
                emailValidated = true
            }
            binding.btnSignup.isEnabled = isFormValid()
        }

        binding.etPassword.afterTextChanged { password ->
            if (password.length < 6) {
                binding.passwordTextField.error = "Password must be at least 6 characters"
                passwordValidated = false
            } else {
                binding.passwordTextField.error = null
                passwordValidated = true
            }
            confirmPasswordValidate()
            binding.btnSignup.isEnabled = isFormValid()
        }

        binding.etConfirmPassword.afterTextChanged {
            confirmPasswordValidate()
            binding.btnSignup.isEnabled = isFormValid()
        }

        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.btnSignup.isEnabled) {
                binding.btnSignup.performClick(); true
            } else false
        }
    }
}