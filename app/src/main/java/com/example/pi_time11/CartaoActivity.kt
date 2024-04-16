package com.example.pi_time11

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartaoActivity : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrar: Button
    private lateinit var etCard: TextInputLayout
    private lateinit var etCvv: TextInputLayout
    private lateinit var etDataval: TextInputLayout
    private lateinit var etCPF: TextInputLayout


    private var db = FirebaseFirestore.getInstance()

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cartao)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnVoltar = findViewById(R.id.btnVoltar)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        etCard = findViewById(R.id.etCard)
        etCvv = findViewById(R.id.etCvv)
        etDataval = findViewById(R.id.etDataval)
        etCPF = findViewById(R.id.etCPF)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCadastrar.setOnClickListener {

            val sCard = etCard.editText?.text.toString()
            val sCvv = etCvv.editText?.text.toString()
            val sDataval = etDataval.editText?.text.toString()
            val sCPF = etCPF.editText?.text.toString()

            if (sCard.isNotEmpty() && sCvv.isNotEmpty() && sDataval.isNotEmpty() &&
                sCPF.isNotEmpty()) {

                // Cria um novo cartão
                val cartao = hashMapOf(
                    "card" to sCard,
                    "cvv" to sCvv,
                    "dataval" to sDataval,
                    "cpf" to sCPF
                )

                // Adiciona os dados do cartão ao Firestore
                db.collection("cartoes")
                    .add(cartao)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

                        // Navega para a próxima tela após o cadastro
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }

            } else {
                // Exibe mensagem se algum campo estiver vazio
                Toast.makeText(baseContext, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
