package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: TextInputLayout
    private lateinit var etSenha: TextInputLayout
    private lateinit var buttonLogin: Button
    private lateinit var buttonSair: Button
    private lateinit var buttonRecuperar: Button
    private lateinit var buttonCadastrar: Button
    private lateinit var buttonContinuarSLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etSenha = findViewById(R.id.etSenha)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonSair = findViewById(R.id.buttonSair)
        buttonRecuperar = findViewById(R.id.buttonRecuperar)
        buttonCadastrar = findViewById(R.id.buttonCadastrar)
        buttonContinuarSLogin = findViewById(R.id.buttonContinuarSLogin)

        auth = Firebase.auth

        buttonLogin.setOnClickListener {
            val email = etEmail.editText?.text.toString()
            val password = etSenha.editText?.text.toString()

            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Password: $password")

            signIn(email, password)
        }

        buttonCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        buttonContinuarSLogin.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        buttonRecuperar.setOnClickListener {
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
        }

        buttonSair.setOnClickListener {
            auth.signOut()
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email
            if (email != null && email.contains("@admin.com")) {
                val intent = Intent(this, GerenteActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                if (currentUser.isEmailVerified) {
                    // se usuário estiver logado, automaticmente irá para a próxima tela
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                    Log.d(TAG, "Logado!")
                } else {
                    Log.d(TAG, "Não logado!!")
                }
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (email.contains("@admin.com")) {
                            val intent = Intent(this, GerenteActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (user.isEmailVerified) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val intent = Intent(this, MapsActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Por favor, verifique seu e-mail antes de fazer login.",
                                Toast.LENGTH_LONG
                            ).show()
                            auth.signOut()
                        }
                    }
                } else {
                    // Sign in failed
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Falha na autenticação.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        private const val TAG = "Login"
    }
}
