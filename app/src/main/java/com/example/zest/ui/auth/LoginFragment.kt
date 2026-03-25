package com.example.zest.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zest.R
import com.example.zest.databinding.FragmentLoginBinding
import com.example.zest.utils.afterTextChanged
import com.example.zest.utils.showLoading
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        var emailValidated = false
        var passwordValidated = false
        fun isFormValid() = emailValidated && passwordValidated

        fun showLoading(show: Boolean) {
            this.showLoading(show)
            binding.btnLogin.isEnabled = !show
        }

        binding.btnLogin.setOnClickListener {
            showLoading(true)
            auth.signInWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            ).addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Snackbar.make(view, "Login Success", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.GREEN).setTextColor(Color.WHITE).show()
                    findNavController().navigate(R.id.action_login_to_feed)
                } else {
                    Snackbar.make(view, "Login Failed", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show()
                }
            }
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.btnLogin.isEnabled) {
                binding.btnLogin.performClick(); true
            } else false
        }

        binding.btnSignup.setOnClickListener { findNavController().navigate(R.id.action_login_to_register) }
        binding.backArrow.setOnClickListener { findNavController().popBackStack() }

        binding.etEmail.afterTextChanged { email ->
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailTextField.error = "Invalid email address"
                emailValidated = false
            } else {
                binding.emailTextField.error = null
                emailValidated = true
            }
            binding.btnLogin.isEnabled = isFormValid()
        }

        binding.etPassword.afterTextChanged { password ->
            if (password.length < 6) {
                binding.passwordTextField.error = "Password must be at least 6 characters"
                passwordValidated = false
            } else {
                binding.passwordTextField.error = null
                passwordValidated = true
            }
            binding.btnLogin.isEnabled = isFormValid()
        }
    }
}