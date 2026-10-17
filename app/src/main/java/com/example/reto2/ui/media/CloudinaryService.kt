
package com.example.reto2.ui.media

import android.content.Context
import android.net.Uri
import com.example.reto2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

object CloudinaryService {

    private val client = OkHttpClient()

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
            val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET

            require(cloudName.isNotBlank()) {
                "Falta configurar CLOUDINARY_CLOUD_NAME."
            }
            require(uploadPreset.isNotBlank()) {
                "Falta configurar CLOUDINARY_UPLOAD_PRESET."
            }

            val tempFile = File.createTempFile(
                "cloudinary_upload_",
                ".jpg",
                context.cacheDir
            )

            try {
                val input = context.contentResolver
                    .openInputStream(imageUri)
                    ?: throw IOException("No se pudo abrir la imagen.")

                input.use { source ->
                    tempFile.outputStream().use { destination ->
                        source.copyTo(destination)
                    }
                }

                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", tempFile.name,
                        tempFile.asRequestBody("application/octet-stream".toMediaType())
                    )
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseText = response.body?.string().orEmpty()

                    if (!response.isSuccessful) {
                        throw IOException(
                            "Cloudinary rechazó la imagen (${response.code}): $responseText"
                        )
                    }

                    val secureUrl = JSONObject(responseText)
                        .optString("secure_url")

                    if (secureUrl.isBlank()) {
                        throw IOException("Cloudinary no devolvió la URL de la imagen.")
                    }

                    Result.success(secureUrl)
                }
            } finally {
                tempFile.delete()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
