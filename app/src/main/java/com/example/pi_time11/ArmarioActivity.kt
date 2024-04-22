package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class ArmarioActivity : AppCompatActivity() {

    private lateinit var buttonVoltar: Button
    private lateinit var btnContinuar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_armario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recebendo dados da Intent
        val localizacao = intent.getStringExtra("localizacao")
        val id = intent.getStringExtra("id")

        buttonVoltar = findViewById(R.id.btnVoltar)
        btnContinuar = findViewById(R.id.btn_compra)

        // Exibindo os dados nos TextViews
        findViewById<TextView>(R.id.valor_endereco).text = localizacao
        findViewById<TextView>(R.id.id_armario).text = id

        // Consulta ao Firestore para obter outros dados do armário, se necessário
        val db = FirebaseFirestore.getInstance()
        val armariosRef = db.collection("armarios").document("01") // Substitua pelo seu ID de documento

        buttonVoltar.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnContinuar.setOnClickListener {
            // Iniciar a atividade de pagamento (CartaoActivity)
            val intent = Intent(this, OpcoesActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }

        armariosRef.get() //  inicia a consulta ao Firestore para obter o documento da coleção "armarios"
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val armario = document.getString("armario")
                    val valorArmario = document.getString("valor_armario")
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
        private const val TAG = "ArmarioActivity"
    }
}
