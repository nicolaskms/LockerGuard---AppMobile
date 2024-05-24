package com.example.pi_time11

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MostrarFotoActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var buttonVoltar: Button
    private lateinit var btnContinuar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_foto)

        buttonVoltar = findViewById(R.id.btnVoltar_camera)
        btnContinuar = findViewById(R.id.btnContinuar_Camera)

        imageView = findViewById(R.id.image_views)
        val photoUri: String? = intent.getStringExtra("photoUri")

        photoUri?.let {
            imageView.setImageURI(Uri.parse(it))
        }

        buttonVoltar.setOnClickListener{
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnContinuar.setOnClickListener {
            // Iniciar a atividade de pagamento (CartaoActivity)
            ///TODO Mandar para NFC
           // val intent = Intent(this, NFC::class.java)
           // startActivity(intent)
           // finish()
        }
    }
}