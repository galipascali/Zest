package com.example.zest.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zest.model.Recipe
import com.example.zest.model.User
import com.example.zest.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecipeRepository(application)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    val recipes: LiveData<List<Recipe>> = repository.getUserRecipes(userId)
    val user = MutableLiveData<User>()

    init {
        viewModelScope.launch { repository.syncUserRecipesFromFirestore(userId) }
        loadProfile()
    }

    fun loadProfile() {
        val currentUser = auth.currentUser ?: return
        val email = currentUser.email ?: ""
        val displayName = currentUser.displayName?.ifBlank { null } ?: email.substringBefore("@")

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                user.value = User(
                    email = email,
                    displayName = displayName,
                    bio = doc.getString("bio") ?: "",
                    photoBase64 = doc.getString("photoBase64")
                )
            }
    }

    fun updateProfile(name: String, bio: String, photoBase64: String?) {
        val currentUser = auth.currentUser ?: return

        if (name.isNotEmpty()) {
            val request = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            currentUser.updateProfile(request)
        }

        val updates = mutableMapOf<String, Any>("bio" to bio)
        photoBase64?.let { updates["photoBase64"] = it }
        firestore.collection("users").document(userId).set(updates, SetOptions.merge())

        val current = user.value ?: User()
        user.value = current.copy(
            displayName = name.ifEmpty { current.displayName },
            bio = bio,
            photoBase64 = photoBase64 ?: current.photoBase64
        )
    }

    fun signOut() {
        auth.signOut()
    }
}