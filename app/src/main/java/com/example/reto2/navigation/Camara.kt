package com.example.reto2.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = pendingUri
    }

    fun launchCamera() {
        val file = File.createTempFile("foto_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        pendingUri = uri
        takePicture.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else permissionDenied = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        photoUri?.let {
            AsyncImage(
                model = it,
                contentDescription = "Foto tomada",
                modifier = Modifier.fillMaxWidth().height(400.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(16.dp))
        }

        if (permissionDenied) {
            Text("Necesitas conceder el permiso de cámara para tomar fotos.")
            Spacer(Modifier.height(8.dp))
        }

        Button(onClick = {
            permissionDenied = false
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) launchCamera()
            else permissionLauncher.launch(Manifest.permission.CAMERA)
        }) {
            Text(if (photoUri == null) "Tomar foto" else "Tomar otra foto")
        }

        TextButton(onClick = onBack) { Text("Volver") }
    }
}