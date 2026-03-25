package com.example.zest.utils

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.zest.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.tasks.await
import java.io.File

fun Fragment.showLoading(show: Boolean) {
    val visibility = if (show) View.VISIBLE else View.GONE
    requireActivity().findViewById<ProgressBar>(R.id.progressBar)?.visibility = visibility
    requireActivity().findViewById<View>(R.id.loadingOverlay)?.visibility = visibility
}

fun Fragment.showImagePickerDialog(
    fileName: String,
    takePhotoLauncher: ActivityResultLauncher<Uri>,
    pickImageLauncher: ActivityResultLauncher<String>,
    onUriReady: (Uri) -> Unit
) {
    MaterialAlertDialogBuilder(requireContext())
        .setItems(arrayOf("Take a photo", "Choose from gallery")) { _, which ->
            when (which) {
                0 -> {
                    val file = File(requireContext().cacheDir, fileName)
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        file
                    )
                    onUriReady(uri)
                    takePhotoLauncher.launch(uri)
                }
                1 -> pickImageLauncher.launch("image/*")
            }
        }
        .show()
}

fun ImageView.loadImage(url: String, placeholder: Int = 0) {
    if (url.isBlank()) return
    val creator = if (url.startsWith("http")) Picasso.get().load(url)
                  else Picasso.get().load(File(url))
    if (placeholder != 0) creator.placeholder(placeholder)
    creator.into(this)
}

fun ImageView.loadImageWithCallback(url: String, placeholder: Int = 0, onDone: () -> Unit) {
    if (url.isBlank()) { onDone(); return }
    val creator = if (url.startsWith("http")) Picasso.get().load(url)
                  else Picasso.get().load(File(url))
    if (placeholder != 0) creator.placeholder(placeholder)
    creator.into(this, object : com.squareup.picasso.Callback {
        override fun onSuccess() = onDone()
        override fun onError(e: Exception?) = onDone()
    })
}

suspend fun uploadImageToStorage(context: Context, uri: Uri, storagePath: String): String {
    val ref = FirebaseStorage.getInstance().reference.child(storagePath)
    val stream = context.contentResolver.openInputStream(uri) ?: return ""
    return try {
        ref.putStream(stream).await()
        stream.close()
        ref.downloadUrl.await().toString()
    } catch (e: Exception) {
        stream.close()
        ""
    }
}

fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}