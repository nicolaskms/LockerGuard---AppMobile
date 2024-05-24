package com.example.pi_time11

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class FotoActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var buttonVoltar: Button
    private lateinit var btnContinuar: Button
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto)

        buttonVoltar = findViewById(R.id.back_button)
        btnContinuar = findViewById(R.id.continue_button)
        firestore = FirebaseFirestore.getInstance()

        val idPedido = intent.getStringExtra("idpedido") // Aqui você deve obter o ID do pedido da Intent.

        if (idPedido != null) {
            fetchUserIdFromPedido(idPedido)
        } else {
            Toast.makeText(this, "ID do Pedido não fornecido", Toast.LENGTH_SHORT).show()
        }

        buttonVoltar.setOnClickListener {
            val intent = Intent(this, GerenteActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnContinuar.setOnClickListener {
            val intent = Intent(this, LiberarLocActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserIdFromPedido(pedidoId: String) {
        firestore.collection("pedidos").document(pedidoId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userId = document.getString("userId")
                    if (userId != null) {
                        setupImageView(userId!!)
                    } else {
                        Toast.makeText(this, "UserID não encontrado.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Pedido não encontrado.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar o pedido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupImageView(userId: String) {
        val photoFileName = "$userId.jpg"
        val photoFile = File(cacheDir, photoFileName)
        imageView = findViewById(R.id.image_view)

        if (photoFile.exists()) {
            val photoUri = Uri.fromFile(photoFile)
            imageView.setImageURI(photoUri)
        } else {
            downloadPhotoFirebase(userId, photoFile)
        }
    }

    private fun downloadPhotoFirebase(userId: String, localFile: File) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val userPhotoRef: StorageReference = storageReference.child("images/$userId.jpg")

        userPhotoRef.getFile(localFile)
            .addOnSuccessListener {
                val photoUri = Uri.fromFile(localFile)
                imageView.setImageURI(photoUri)
                Toast.makeText(this, "Foto carregada com sucesso", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar a foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
