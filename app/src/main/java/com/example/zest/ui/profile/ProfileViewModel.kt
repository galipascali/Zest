package com.example.zest.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zest.model.Recipe
import com.example.zest.model.User
import com.example.zest.repository.RecipeRepository
import com.example.zest.utils.uploadImageToStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecipeRepository(application)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    val recipes: LiveData<List<Recipe>> = repository.getUserRecipes(userId)
    val user = MutableLiveData<User>()
    val updateProfileResult = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch { repository.syncUserRecipesFromFirestore(userId) }

        auth.currentUser?.let { currentUser ->
            val email = currentUser.email ?: ""
            val displayName = currentUser.displayName?.ifBlank { null } ?: email.substringBefore("@")
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    user.value = User(
                        email = email,
                        displayName = displayName,
                        bio = doc.getString("bio") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: ""
                    )
                }
                .addOnFailureListener {
                    user.value = User(email = email, displayName = displayName)
                }
        }
    }

    fun updateProfile(name: String, bio: String, photoUri: Uri? = null) {
        viewModelScope.launch {
            try {
                val photoUrl = photoUri?.let {
                    uploadImageToStorage(getApplication(), it, "profile_photos/$userId.jpg")
                }

                val updates = mutableMapOf<String, Any>("bio" to bio)
                photoUrl?.let { updates["photoUrl"] = it }
                firestore.collection("users").document(userId).set(updates, SetOptions.merge()).await()

                val currentUser = auth.currentUser ?: return@launch
                if (name.isNotEmpty()) {
                    val request = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    currentUser.updateProfile(request).await()
                }

                val current = user.value ?: User()
                user.value = current.copy(
                    displayName = name.ifEmpty { current.displayName },
                    bio = bio,
                    photoUrl = photoUrl ?: current.photoUrl
                )
                updateProfileResult.value = true
            } catch (e: Exception) {
                updateProfileResult.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}