package com.example.rmas.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

class ImageUtils(private val context: Context) {
    var currentPhotoPath: String? = null

    private fun createImageFile(): File {
        val timestamp = System.currentTimeMillis()
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun getImageCaptureIntent(): Intent? { // Vraća nullable za slučaj da nema kamere
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Provera da li postoji aplikacija koja može da obradi nameru
        if (takePictureIntent.resolveActivity(context.packageManager) == null) {
            return null
        }

        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            // Greška pri kreiranju fajla
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        return takePictureIntent
    }

    private fun getGalleryIntent() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }

    fun getIntent(): Intent {
        val captureIntent = getImageCaptureIntent()
        val galleryIntent = getGalleryIntent()

        val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, galleryIntent)
            putExtra(Intent.EXTRA_TITLE, "Izaberite sliku iz:")
            // Dodajemo intent za kameru samo ako postoji
            captureIntent?.let {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(it))
            }
        }

        return chooserIntent
    }
}