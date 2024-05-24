package com.example.pi_time11

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var storage: FirebaseStorage
    private lateinit var currentPhotoFile: File

    private var numPessoas = 1
    private var currentPerson = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraView = findViewById(R.id.camera_view)
        storage = FirebaseStorage.getInstance()

        numPessoas = intent.getIntExtra("numPessoas", 1)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_REQUEST)
        } else {
            setupCamera()
        }

        val captureButton: Button = findViewById(R.id.capture_button)
        captureButton.setOnClickListener { capturePhoto() }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(cameraView.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("Camera", "Erro ao configurar a câmera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val outputDirectory = cacheDir

        currentPhotoFile = File(outputDirectory, SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(currentPhotoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    if (currentPerson < numPessoas) {
                        currentPerson++
                        showNextPersonDialog()
                    } else {
                        showPhotoOptionsDialog()
                    }

                    Toast.makeText(this@CameraActivity, "Foto capturada", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Erro ao capturar a foto: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun showNextPersonDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Próxima Pessoa")
            .setMessage("Tire a foto da próxima pessoa")
            .setPositiveButton("OK") { _, _ ->
                restartCamera()
            }
            .create()
            .show()
    }

    private fun showPhotoOptionsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("O que deseja fazer?")
            .setMessage("Tirar novamente ou continuar?")
            .setPositiveButton("Continuar") { _, _ ->
                uploadImageToFirebase(currentPhotoFile)
            }
            .setNegativeButton("Tirar novamente") { _, _ ->
                restartCamera()
            }
            .create()
            .show()
    }

    private fun releaseCamera() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        } catch (e: Exception) {
            Log.e("Camera", "Erro ao liberar a câmera", e)
        }
    }

    private fun restartCamera() {
        releaseCamera()
        setupCamera()
    }

    private fun uploadImageToFirebase(photoFile: File) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images/${photoFile.name}")
        val fileUri = Uri.fromFile(photoFile)

        imagesRef.putFile(fileUri)
            .addOnSuccessListener {
                Toast.makeText(this@CameraActivity, "Upload bem-sucedido", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@CameraActivity, MostrarFotoActivity::class.java)
                intent.putExtra("photoUri", fileUri.toString())
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@CameraActivity, "Falha no upload: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                Toast.makeText(this, "Permissões necessárias não concedidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 123
    }

}