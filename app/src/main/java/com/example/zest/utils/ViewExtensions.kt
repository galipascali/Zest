package com.example.zest.utils

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.io.File

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

fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}