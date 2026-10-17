
package com.example.reto2.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.reto2.ui.media.CloudinaryService
import com.example.reto2.data.model.CurrencyOperation
import com.example.reto2.data.repository.OperationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val repository = remember { OperationRepository() }

    var operations by remember {
        mutableStateOf<List<CurrencyOperation>>(emptyList())
    }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(value = true) }
    var uploadingPhoto by remember { mutableStateOf(value = false) }
    var processingOperationId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    suspend fun loadPhoto() {
        val user = auth.currentUser ?: return

        val document = firestore.collection("users")
            .document(user.uid)
            .get()
            .await()

        photoUrl = document.getString("photoUrl")
    }

    suspend fun loadOperations() {
        operations = repository.getPendingAuditOperations().getOrThrow()
    }

    fun refreshData() {
        scope.launch {
            loading = true
            errorMessage = null

            try {
                loadPhoto()
                loadOperations()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
                    ?: "No se pudieron cargar los datos."
            } finally {
                loading = false
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            scope.launch {
                uploadingPhoto = true
                errorMessage = null
                infoMessage = null

                try {
                    val user = auth.currentUser
                        ?: throw IllegalStateException(
                            "Debes iniciar sesión para cambiar la foto."
                        )

                    // Cloudinary devuelve Result<String>, no String directamente.
                    val uploadResult = CloudinaryService.uploadImage(
                        context = context,
                        imageUri = imageUri
                    )

                    // Obtiene la URL o lanza una excepción que capturamos abajo.
                    val url = uploadResult.getOrThrow()

                    // Guarda únicamente la URL de la foto en el perfil.
                    firestore.collection("users")
                        .document(user.uid)
                        .set(
                            mapOf("photoUrl" to url),
                            SetOptions.merge()
                        )
                        .await()

                    photoUrl = url
                    infoMessage = "Foto de perfil actualizada correctamente."
                } catch (e: Exception) {
                    errorMessage = e.localizedMessage
                        ?: "No se pudo actualizar la foto."
                } finally {
                    uploadingPhoto = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Panel de administrador",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(
                        onClick = { refreshData() },
                        enabled = !loading
                    ) {
                        Text("Actualizar")
                    }
                    TextButton(
                        onClick = onLogout
                    ) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Perfil del administrador
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        .clickable(
                            enabled = !uploadingPhoto
                        ) {
                            imagePicker.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Foto del administrador",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Administrador",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (uploadingPhoto) {
                            "Subiendo foto..."
                        } else {
                            "Toca la imagen para cambiar la foto"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (uploadingPhoto) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }

            infoMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Auditoría de operaciones pendientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                operations.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay operaciones pendientes de auditoría.")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = operations,
                            key = { operation -> operation.id }
                        ) { operation ->
                            OperationAuditCard(
                                operation = operation,
                                processing = processingOperationId == operation.id,
                                onApprove = {
                                    scope.launch {
                                        processingOperationId = operation.id
                                        errorMessage = null
                                        infoMessage = null

                                        try {
                                            repository.updateOperationStatus(operation.id, "APPROVED").getOrThrow()
                                            loadOperations()
                                            infoMessage =
                                                "Operación aprobada correctamente."
                                        } catch (e: Exception) {
                                            errorMessage = e.localizedMessage
                                                ?: "No se pudo aprobar la operación."
                                        } finally {
                                            processingOperationId = null
                                        }
                                    }
                                },
                                onReject = {
                                    scope.launch {
                                        processingOperationId = operation.id
                                        errorMessage = null
                                        infoMessage = null

                                        try {
                                            repository.updateOperationStatus(operation.id, "REJECTED").getOrThrow()
                                            loadOperations()
                                            infoMessage =
                                                "Operación rechazada correctamente."
                                        } catch (e: Exception) {
                                            errorMessage = e.localizedMessage
                                                ?: "No se pudo rechazar la operación."
                                        } finally {
                                            processingOperationId = null
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationAuditCard(
    operation: CurrencyOperation,
    processing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${operation.type} · ${operation.status}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Usuario: ${operation.userId}")
            Text("Monto: ${operation.amount}")
            Text(
                "Conversión: ${operation.baseCurrency} → ${operation.targetCurrency}"
            )
            Text("Tasa de referencia: ${operation.referenceRate}")
            Text("Tasa aplicada: ${operation.appliedRate}")
            Text("Total: ${operation.total}")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fecha de registro: ${formatAuditDate(operation.createdAt)}"
            )

            if (operation.approvedAt > 0L) {
                Text(
                    text = "Fecha de aprobación: ${formatAuditDate(operation.approvedAt)}"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !processing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (processing) "Procesando..." else "Aprobar")
                }

                OutlinedButton(
                    onClick = onReject,
                    enabled = !processing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rechazar")
                }
            }
        }
    }
}

private fun formatAuditDate(timestamp: Long): String {
    if (timestamp <= 0L) return "No registrada"

    return try {
        val formatter = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.forLanguageTag("es-CO"),
        )
        formatter.timeZone = TimeZone.getDefault()
        formatter.format(Date(timestamp))
    } catch (_: Exception) {
        "Fecha no disponible"
    }
}
