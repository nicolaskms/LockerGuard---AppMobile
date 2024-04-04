package com.example.pi_time11


import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class CadastroActivity : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrar: Button
    private lateinit var etNome: TextInputLayout
    private lateinit var etEmail: TextInputLayout
    private lateinit var etIdade: TextInputLayout
    private lateinit var etCPF: TextInputLayout
    private lateinit var etCelular: TextInputLayout
    private lateinit var etSenha: TextInputLayout

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)


        btnVoltar = findViewById(R.id.btnVoltar)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        etNome = findViewById(R.id.etNome)
        etEmail = findViewById(R.id.etEmail)
        etIdade = findViewById(R.id.etIdade)
        etCPF = findViewById(R.id.etCPF)
        etCelular = findViewById(R.id.etCelular)
        etSenha = findViewById(R.id.etSenha)


        btnCadastrar.setOnClickListener {

            val sNome = etNome.editText?.text.toString()
            val sEmail = etEmail.editText?.text.toString()
            val sIdade = etIdade.editText?.text.toString()
            val sCPF = etCPF.editText?.text.toString()
            val sCelular = etCelular.editText?.text.toString()
            val sSenha = etSenha.editText?.text.toString()

            val user = hashMapOf(
                "name" to sNome,
                "email" to sEmail,
                "idade" to sIdade,
                "cpf" to sCPF,
                "celular" to sCelular,
                "senha" to sSenha
            )


            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }

        btnVoltar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            //proximo finish() estudar se é realmente necessario
            finish()
        }
    }
}