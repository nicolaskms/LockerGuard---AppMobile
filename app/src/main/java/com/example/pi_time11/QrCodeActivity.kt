package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class QrCodeActivity : AppCompatActivity() {

    private lateinit var buttonVoltar: ImageButton
    private lateinit var armarioIdTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        // Inicialização dos elementos da UI
        buttonVoltar = findViewById(R.id.btnVoltar)
        armarioIdTextView = findViewById(R.id.armario_id) // Inicializando o TextView aqui

        // Recebendo o ID do armário enviado pela intent
        val armarioId = intent.getStringExtra("id")

        // Exibindo o ID do armário no TextView
        armarioId?.let {
            armarioIdTextView.text = it
        }

        // Ação do botão "Voltar"
        buttonVoltar.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Consulta ao Firestore para obter outros dados do armário, se necessário
        val db = FirebaseFirestore.getInstance()
        val armariosRef = db.collection("armarios").document(armarioId ?: "01") // Usando o ID recebido

        armariosRef.get() //  inicia a consulta ao Firestore para obter o documento da coleção "armarios"
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val armario = document.getString("armario")
                    // Exibir outros dados do armário, se necessário
                } else {
                    Log.d(TAG, "Documento não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Falha ao obter documento: ", exception)
            }
    }

    companion object {
        private const val TAG = "QrCodeActivity"
    }
}
