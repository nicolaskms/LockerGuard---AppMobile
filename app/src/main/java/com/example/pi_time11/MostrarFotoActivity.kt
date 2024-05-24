package com.example.pi_time11

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_foto)

        imageView = findViewById(R.id.image_views)
        val photoUri: String? = intent.getStringExtra("photoUri")

        photoUri?.let {
            imageView.setImageURI(Uri.parse(it))
        }
    }
}