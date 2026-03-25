package com.example.zest.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.utils.showImagePickerDialog
import com.example.zest.model.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    private var currentUser: User? = null
    private var pendingPhotoBase64: String? = null
    private var dialogAvatarView: ShapeableImageView? = null
    private lateinit var cameraUri: Uri

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processImage(it) }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) processImage(cameraUri)
    }

    private fun processImage(uri: Uri) {
        val stream = requireContext().contentResolver.openInputStream(uri) ?: return
        val original = BitmapFactory.decodeStream(stream)
        stream.close()
        val scaled = original.scale(200, 200)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
        pendingPhotoBase64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        dialogAvatarView?.setImageBitmap(scaled)
    }

    private fun showImagePickerDialog() {
        showImagePickerDialog("profile_photo.jpg", takePhotoLauncher, pickImageLauncher) { uri ->
            cameraUri = uri
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userAvatar = view.findViewById<ShapeableImageView>(R.id.userAvatar)
        val usernameView = view.findViewById<TextView>(R.id.username)
        val emailView = view.findViewById<TextView>(R.id.email)
        val bioView = view.findViewById<TextView>(R.id.bio)
        val btnEditProfile = view.findViewById<ImageButton>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<ImageButton>(R.id.btnLogout)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipes)

        val adapter = ProfileRecipeAdapter { recipe ->
            val action = ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id)
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewModel.recipes.observe(viewLifecycleOwner) { adapter.submitList(it) }

        viewModel.user.observe(viewLifecycleOwner) { profile ->
            currentUser = profile
            emailView.text = profile.email
            usernameView.text = profile.displayName
            bioView.text = profile.bio
            bioView.visibility = if (profile.bio.isBlank()) View.GONE else View.VISIBLE
            profile.photoBase64?.let { b64 ->
                decodeBase64ToBitmap(b64)?.let { userAvatar.setImageBitmap(it) }
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
            pendingPhotoBase64 = null
            currentUser?.let { profile ->
                val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
                val nameInput = dialogView.findViewById<TextInputEditText>(R.id.etDisplayName)
                val bioInput = dialogView.findViewById<TextInputEditText>(R.id.etBio)
                val avatarView = dialogView.findViewById<ShapeableImageView>(R.id.dialogAvatar)
                dialogAvatarView = avatarView

                nameInput.setText(profile.displayName)
                bioInput.setText(profile.bio)

                val currentBitmap = (userAvatar.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                currentBitmap?.let { avatarView.setImageBitmap(it) }

                avatarView.setOnClickListener { showImagePickerDialog() }

                MaterialAlertDialogBuilder(requireContext())
                    .setView(dialogView)
                    .setPositiveButton("Save") { _, _ ->
                        viewModel.updateProfile(
                            name = nameInput.text.toString().trim(),
                            bio = bioInput.text.toString().trim(),
                            photoBase64 = pendingPhotoBase64
                        )
                        dialogAvatarView = null
                    }
                    .setNegativeButton("Cancel") { _, _ -> dialogAvatarView = null }
                    .show()
            }
        }
    }

    private fun decodeBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}