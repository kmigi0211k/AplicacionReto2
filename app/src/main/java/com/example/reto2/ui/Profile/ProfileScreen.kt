
package com.example.reto2.ui.profile

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.reto2.ui.media.CloudinaryService
import com.example.reto2.ui.media.MediaPicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("Cargando...") }
    var email by remember {
        mutableStateOf(user?.email ?: "Correo no disponible")
    }
    var role by remember { mutableStateOf("Cargando...") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var loadingProfile by remember { mutableStateOf(true) }
    var uploading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf(false) }

    val initial = name.firstOrNull()?.uppercase() ?: "U"

    LaunchedEffect(user?.uid) {
        if (user == null) {
            name = "Usuario"
            role = "Sin sesión"
            loadingProfile = false
            return@LaunchedEffect
        }

        loadingProfile = true

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                Log.d("ProfileScreen", "UID: ${user.uid}")
                Log.d(
                    "ProfileScreen",
                    "Documento existe: ${document.exists()}"
                )
                Log.d(
                    "ProfileScreen",
                    "Datos: ${document.data}"
                )

                if (document.exists()) {
                    name = document.getString("fullName")
                        ?.takeIf { it.isNotBlank() }
                        ?: "Usuario"

                    email = document.getString("email")
                        ?: user.email
                                ?: "Correo no disponible"

                    // Acepta ambos nombres: role y rol.
                    val roleValue = document["role"]
                        ?: document["rol"]

                    role = when (roleValue) {
                        is String -> roleValue
                            .trim()
                            .takeIf { it.isNotEmpty() }
                            ?: "Sin rol"

                        null -> "Sin rol"

                        else -> "Rol con formato incorrecto"
                    }

                    photoUrl = document.getString("photoUrl")
                    message = ""
                    errorMessage = false

                    Log.d("ProfileScreen", "Rol leído: $role")
                } else {
                    name = "Perfil no encontrado"
                    role = "Sin perfil"
                    message = "No existe el perfil en Firestore."
                    errorMessage = true
                }

                loadingProfile = false
            }
            .addOnFailureListener { error ->
                Log.e(
                    "ProfileScreen",
                    "Error al cargar el perfil",
                    error
                )
                message = "Error al cargar perfil: ${error.message}"
                errorMessage = true
                loadingProfile = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    Button(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (loadingProfile) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información personal",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Nombre: $name")
                    Text("Correo: $email")
                    Text("Rol: $role")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (user == null) {
                Text(
                    text = "Debes iniciar sesión para consultar tu perfil.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                MediaPicker(
                    enabled = !uploading && !loadingProfile,
                    onImageSelected = { imageUri: Uri ->
                        scope.launch {
                            uploading = true
                            message = "Subiendo imagen a Cloudinary..."
                            errorMessage = false

                            try {
                                val result = CloudinaryService.uploadImage(
                                    context = context,
                                    imageUri = imageUri
                                )

                                result.fold(
                                    onSuccess = { url ->
                                        message =
                                            "Imagen subida. Guardando URL..."

                                        db.collection("users")
                                            .document(user.uid)
                                            .set(
                                                mapOf("photoUrl" to url),
                                                SetOptions.merge()
                                            )
                                            .addOnSuccessListener {
                                                photoUrl = url
                                                message =
                                                    "Foto de perfil guardada correctamente."
                                                errorMessage = false
                                                uploading = false
                                            }
                                            .addOnFailureListener { error ->
                                                message =
                                                    "Error Firestore: ${error.message}"
                                                errorMessage = true
                                                uploading = false
                                            }
                                    },
                                    onFailure = { error ->
                                        message =
                                            "Error Cloudinary: ${error.message}"
                                        errorMessage = true
                                        uploading = false
                                    }
                                )
                            } catch (error: Exception) {
                                message =
                                    "Error inesperado: ${error.message}"
                                errorMessage = true
                                uploading = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uploading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = if (errorMessage) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
