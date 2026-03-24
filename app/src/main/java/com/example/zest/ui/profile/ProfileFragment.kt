package com.example.zest.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

   private var pendingPhotoBase64: String? = null
    private var dialogAvatarView: ImageView? = null
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
        val scaled = Bitmap.createScaledBitmap(original, 200, 200, true)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
        pendingPhotoBase64 = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        dialogAvatarView?.setImageBitmap(scaled)
    }

    private fun showImagePickerDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setItems(arrayOf("Take a photo", "Choose from gallery")) { _, which ->
                when (which) {
                    0 -> {
                        val file = File(requireContext().cacheDir, "profile_photo.jpg")
                        cameraUri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.provider",
                            file
                        )
                        takePhotoLauncher.launch(cameraUri)
                    }
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userAvatar = view.findViewById<ImageView>(R.id.userAvatar)
        val usernameView = view.findViewById<TextView>(R.id.username)
        val emailView = view.findViewById<TextView>(R.id.email)
        val bioView = view.findViewById<TextView>(R.id.bio)
        val btnEditProfile = view.findViewById<ImageButton>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<ImageButton>(R.id.btnLogout)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipes)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()
        val email = currentUser?.email ?: ""

        val adapter = ProfileRecipeAdapter { recipe ->
            val action = ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id)
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
        }

        btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_profile_to_welcome)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnEditProfile.setOnClickListener {
            pendingPhotoBase64 = null

            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
            val nameInput = dialogView.findViewById<TextInputEditText>(R.id.etDisplayName)
            val bioInput = dialogView.findViewById<TextInputEditText>(R.id.etBio)
            val avatarView = dialogView.findViewById<ImageView>(R.id.dialogAvatar)
            dialogAvatarView = avatarView

            nameInput.setText(currentUser?.displayName ?: "")
            bioInput.setText(bioView.text)

            val currentBitmap = (userAvatar.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            currentBitmap?.let { avatarView.setImageBitmap(it) }

            avatarView.setOnClickListener { showImagePickerDialog() }

            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val newName = nameInput.text.toString().trim()
                    val newBio = bioInput.text.toString().trim()

                    if (newName.isNotEmpty()) {
                        val request = UserProfileChangeRequest.Builder()
                            .setDisplayName(newName).build()
                        currentUser?.updateProfile(request)
                        usernameView.text = newName
                    }

                    val updates = mutableMapOf<String, Any>("bio" to newBio)
                    pendingPhotoBase64?.let { updates["photoBase64"] = it }

                    currentUser?.uid?.let { uid ->
                        firestore.collection("users").document(uid)
                            .set(updates, SetOptions.merge())
                    }

                    bioView.text = newBio
                    bioView.visibility = if (newBio.isBlank()) View.GONE else View.VISIBLE
                    pendingPhotoBase64?.let { b64 ->
                        decodeBase64ToBitmap(b64)?.let { userAvatar.setImageBitmap(it) }
                    }

                    dialogAvatarView = null
                }
                .setNegativeButton("Cancel") { _, _ -> dialogAvatarView = null }
                .show()
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