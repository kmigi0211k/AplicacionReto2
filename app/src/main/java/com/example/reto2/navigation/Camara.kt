
package com.example.reto2.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

@Composable
fun CamaraScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = pendingUri
            cameraError = null
        } else {
            cameraError = "No se tomó la foto. Inténtalo de nuevo."
        }
    }

    fun launchCamera() {
        try {
            val cacheDir = File(context.cacheDir, "camera_photos").apply {
                if (!exists()) mkdirs()
            }
            val file = File.createTempFile(
                "foto_",
                ".jpg",
                cacheDir
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            pendingUri = uri
            takePicture.launch(uri)
        } catch (e: Exception) {
            cameraError = "No se pudo abrir la cámara: ${
                e.localizedMessage ?: "error desconocido"
            }"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            permissionDenied = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Cámara")

        Spacer(Modifier.height(16.dp))

        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Fotografía capturada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(12.dp))
            Text("Fotografía tomada correctamente.")
        } else {
            Text("Pulsa el botón para tomar una fotografía.")
        }

        Spacer(Modifier.height(16.dp))

        if (permissionDenied) {
            Text("Debes conceder el permiso de cámara.")
            Spacer(Modifier.height(8.dp))
        }

        cameraError?.let {
            Text(it)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                permissionDenied = false
                cameraError = null

                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    launchCamera()
                } else {
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }
        ) {
            Text(
                if (photoUri == null) "Tomar foto"
                else "Tomar otra foto"
            )
        }

        TextButton(onClick = onBack) {
            Text("Volver")
        }
    }
}
