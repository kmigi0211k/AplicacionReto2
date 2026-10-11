
package com.example.reto2.ui.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun MediaPicker(
    onImageSelected: (Uri) -> Unit,
    enabled: Boolean = true
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    Button(
        onClick = { launcher.launch("image/*") },
        enabled = enabled
    ) {
        Text("Seleccionar foto")
    }
}


