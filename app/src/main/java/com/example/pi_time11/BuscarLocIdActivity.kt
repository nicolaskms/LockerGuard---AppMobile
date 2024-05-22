package com.example.pi_time11

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class BuscarLocIdActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_loc_id)

        // Inicializar o Firestore
        firestore = FirebaseFirestore.getInstance()

        // Recuperar o ID passado pela intenção
        val id = intent.getStringExtra("id")
        if (id != null) {
            buscarPedidoPorId(id)
        } else {
            Toast.makeText(this, "ID da Pulseira não encontrado", Toast.LENGTH_SHORT).show()
        }
    }
    private fun buscarPedidoPorId(id: String) {
        val textView = findViewById<TextView>(R.id.textViewId)

        // Buscar na coleção "pedidos" onde o campo "pulseira" é igual ao ID fornecido
        firestore.collection("pedidos")
            .whereEqualTo("pulseira", id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val pedido = documents.first()
                    textView.text = "Pedido encontrado: ${pedido.id}\nDetalhes: ${pedido.data}"
                } else {
                    textView.text = "Nenhum pedido encontrado para o ID da pulseira: $id"
                }
            }
            .addOnFailureListener { exception ->
                textView.text = "Erro ao buscar pedido: ${exception.message}"
            }
    }
}
