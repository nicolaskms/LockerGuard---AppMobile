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

class FotoActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var buttonVoltar: Button
    private lateinit var btnContinuar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto)

        buttonVoltar = findViewById(R.id.back_button)
        btnContinuar = findViewById(R.id.continue_button)

        imageView = findViewById(R.id.image_view)
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
             val intent = Intent(this, LiberarLocActivity::class.java)
             startActivity(intent)
             finish()
        }
    }
}