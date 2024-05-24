package com.example.pi_time11

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
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
    private lateinit var captureButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraView = findViewById(R.id.camera_view)
        progressBar = findViewById(R.id.progressbar_camera)
        storage = FirebaseStorage.getInstance()

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_REQUEST)
        } else {
            setupCamera()
        }

        captureButton = findViewById(R.id.capture_button)

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

        // Desativa o botão de captura
        captureButton.isEnabled = false

        // Mostra o indicador de progresso
        progressBar.visibility = View.VISIBLE

        val outputDirectory = cacheDir
        val userid = intent.getStringExtra("userid")
        val photoFileName = "$userid.jpg" // Nome do arquivo é apenas o userid

        val photoFile = File(outputDirectory, photoFileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Upload da imagem para o Firebase
                    uploadImageToFirebase(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Erro ao capturar a foto: ${exception.message}", Toast.LENGTH_SHORT).show()

                    captureButton.isEnabled = true
                    progressBar.visibility = View.GONE
                }
            })
    }

    private fun uploadImageToFirebase(photoFile: File) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images/${photoFile.name}")
        val fileUri = Uri.fromFile(photoFile)

        imagesRef.putFile(fileUri)
            .addOnSuccessListener {
                Toast.makeText(this@CameraActivity, "Upload bem-sucedido", Toast.LENGTH_SHORT).show()
                val idPedido = intent.getStringExtra("idPedido")

                val intent = Intent(this@CameraActivity, FirstScanTagActivity::class.java).apply {
                    putExtra("idPedido",idPedido)
                    putExtra("photoUri", fileUri.toString())
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@CameraActivity, "Falha no upload: ${e.message}", Toast.LENGTH_SHORT).show()
                captureButton.isEnabled = true
                progressBar.visibility = View.GONE
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