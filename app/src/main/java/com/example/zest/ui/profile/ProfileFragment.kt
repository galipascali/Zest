package com.example.zest.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zest.R
import com.example.zest.databinding.DialogEditProfileBinding
import com.example.zest.databinding.FragmentProfileBinding
import com.example.zest.model.User
import com.example.zest.utils.loadImage
import com.example.zest.utils.showImagePickerDialog
import com.example.zest.utils.showLoading
import android.graphics.Color
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private var currentUser: User? = null
    private var pendingPhotoUri: Uri? = null
    private var dialogBinding: DialogEditProfileBinding? = null
    private lateinit var cameraUri: Uri

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            pendingPhotoUri = it
            dialogBinding?.dialogAvatar?.let { av -> Picasso.get().load(it).into(av) }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingPhotoUri = cameraUri
            dialogBinding?.dialogAvatar?.let { av -> Picasso.get().load(cameraUri).into(av) }
        }
    }

    private fun showImagePickerDialog() {
        showImagePickerDialog("profile_photo.jpg", takePhotoLauncher, pickImageLauncher) { uri ->
            cameraUri = uri
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialogBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)

        val adapter = ProfileRecipeAdapter(
            onItemClick = { recipe ->
                findNavController().navigate(ProfileFragmentDirections.actionProfileToRecipeDetail(recipe.id))
            },
            onEditClick = { recipe ->
                findNavController().navigate(ProfileFragmentDirections.actionProfileToEditRecipe(recipe.id))
            }
        )
        binding.recipes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipes.adapter = adapter

        viewModel.recipes.observe(viewLifecycleOwner) { adapter.submitList(it) }

        viewModel.updateProfileResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Snackbar.make(requireView(), "Profile updated", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().getColor(R.color.success)).setTextColor(Color.WHITE).show()
            } else {
                Snackbar.make(requireView(), "Failed to update profile", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().getColor(R.color.error)).setTextColor(Color.WHITE).show()
            }
        }

        viewModel.user.observe(viewLifecycleOwner) { profile ->
            currentUser = profile
            binding.email.text = profile.email
            binding.username.text = profile.displayName
            binding.bio.text = profile.bio
            binding.bio.visibility = if (profile.bio.isBlank()) View.GONE else View.VISIBLE
            binding.userAvatar.loadImage(profile.photoUrl)
            showLoading(false)
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    viewModel.signOut()
                    findNavController().navigate(R.id.action_profile_to_welcome)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnEditProfile.setOnClickListener {
            pendingPhotoUri = null
            currentUser?.let { profile ->
                val db = DialogEditProfileBinding.inflate(layoutInflater)
                dialogBinding = db

                db.etDisplayName.setText(profile.displayName)
                db.etBio.setText(profile.bio)
                db.dialogAvatar.loadImage(profile.photoUrl)
                db.dialogAvatar.setOnClickListener { showImagePickerDialog() }

                MaterialAlertDialogBuilder(requireContext())
                    .setView(db.root)
                    .setPositiveButton("Save") { _, _ ->
                        viewModel.updateProfile(
                            name = db.etDisplayName.text.toString().trim(),
                            bio = db.etBio.text.toString().trim(),
                            photoUri = pendingPhotoUri
                        )
                        dialogBinding = null
                        pendingPhotoUri = null
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        dialogBinding = null
                        pendingPhotoUri = null
                    }
                    .show()
            }
        }
    }
}