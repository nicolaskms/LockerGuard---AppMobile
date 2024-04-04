package com.example.pi_time11

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private var db = Firebase.firestore

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

            if (sNome.isNotEmpty() && sEmail.isNotEmpty() && sIdade.isNotEmpty() &&
                sCPF.isNotEmpty() && sCelular.isNotEmpty() && sSenha.isNotEmpty()) {

                // Cria o usuário no Firebase Authentication
                auth.createUserWithEmailAndPassword(sEmail, sSenha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Cadastro bem-sucedido, obtém o ID do usuário
                            val userId = auth.currentUser?.uid

                            // Cria um mapa com os dados do usuário
                            val user = hashMapOf(
                                "name" to sNome,
                                "email" to sEmail,
                                "idade" to sIdade,
                                "cpf" to sCPF,
                                "celular" to sCelular
                            )

                            // Adiciona os dados do usuário ao Firestore
                            if (userId != null) {
                                db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "DocumentSnapshot added with ID: $userId")

                                        // Navega para a próxima tela após o cadastro
                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error adding document", e)
                                    }
                            }
                        } else {
                            // Exibe mensagem em caso de falha no cadastro
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            // Exemplo de mensagem de erro
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Exibe mensagem se algum campo estiver vazio
                Toast.makeText(baseContext, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}