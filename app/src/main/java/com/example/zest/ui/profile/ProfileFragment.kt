package com.example.zest.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.utils.loadImage
import com.example.zest.utils.loadImageWithCallback
import com.example.zest.utils.showImagePickerDialog
import com.example.zest.utils.showLoading
import com.example.zest.model.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    private var currentUser: User? = null
    private var pendingPhotoUri: Uri? = null
    private var dialogAvatarView: ShapeableImageView? = null
    private lateinit var cameraUri: Uri

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            pendingPhotoUri = it
            dialogAvatarView?.let { av -> Picasso.get().load(it).into(av) }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingPhotoUri = cameraUri
            dialogAvatarView?.let { av -> Picasso.get().load(cameraUri).into(av) }
        }
    }

    private fun showImagePickerDialog() {
        showImagePickerDialog("profile_photo.jpg", takePhotoLauncher, pickImageLauncher) { uri ->
            cameraUri = uri
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)

        val userAvatar = view.findViewById<ShapeableImageView>(R.id.userAvatar)
        val usernameView = view.findViewById<TextView>(R.id.username)
        val emailView = view.findViewById<TextView>(R.id.email)
        val bioView = view.findViewById<TextView>(R.id.bio)
        val btnEditProfile = view.findViewById<ImageButton>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<ImageButton>(R.id.btnLogout)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipes)

        var userReady = false
        var recipesReady = false
        fun checkAllReady() { if (userReady && recipesReady) showLoading(false) }

        val adapter = ProfileRecipeAdapter(
            onItemClick = { recipe ->
                val action = ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id)
                findNavController().navigate(action)
            },
            onImagesLoaded = { recipesReady = true; checkAllReady() }
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewModel.recipes.observe(viewLifecycleOwner) { adapter.submitList(it) }

        viewModel.user.observe(viewLifecycleOwner) { profile ->
            currentUser = profile
            emailView.text = profile.email
            usernameView.text = profile.displayName
            bioView.text = profile.bio
            bioView.visibility = if (profile.bio.isBlank()) View.GONE else View.VISIBLE
            userAvatar.loadImageWithCallback(profile.photoUrl) {
                userReady = true
                checkAllReady()
            }
        }

        btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    viewModel.signOut()
                    findNavController().navigate(R.id.action_profile_to_welcome)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnEditProfile.setOnClickListener {
            pendingPhotoUri = null
            currentUser?.let { profile ->
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
                val nameInput = dialogView.findViewById<TextInputEditText>(R.id.etDisplayName)
                val bioInput = dialogView.findViewById<TextInputEditText>(R.id.etBio)
                val avatarView = dialogView.findViewById<ShapeableImageView>(R.id.dialogAvatar)
                dialogAvatarView = avatarView

                nameInput.setText(profile.displayName)
                bioInput.setText(profile.bio)
                avatarView.loadImage(profile.photoUrl)
                avatarView.setOnClickListener { showImagePickerDialog() }

                MaterialAlertDialogBuilder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Save") { _, _ ->
                        viewModel.updateProfile(
                            name = nameInput.text.toString().trim(),
                            bio = bioInput.text.toString().trim(),
                            photoUri = pendingPhotoUri
                        )
                        dialogAvatarView = null
                        pendingPhotoUri = null
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        dialogAvatarView = null
                        pendingPhotoUri = null
                    }
                    .show()
            }
        }
    }
}