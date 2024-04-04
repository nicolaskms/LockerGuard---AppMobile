package com.example.pi_time11

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
//import android.widget.EditText
import android.widget.Toast
//import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class LoginActivity : AppCompatActivity() {
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private lateinit var etEmail: TextInputLayout
    private lateinit var etSenha: TextInputLayout
    private lateinit var buttonLogin: Button
    private lateinit var buttonSair: Button
    private lateinit var buttonRecuperar: Button
    private lateinit var buttonCadastrar: Button
    private lateinit var buttonContinuarSLogin: Button

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Certifique-se de definir o layout da atividade

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

        buttonSair.setOnClickListener{
            auth.signOut()
            finish()
        }
    }


    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // se usuário estiver logado, automaticmente irá p proxima tela
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
            Log.d(TAG, "Logado!")
        } else {
            Log.d(TAG, "Não logado!!")
        }
    }
    // [END on_start_check_user]

    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if(user!=null) {
                        if (user.isEmailVerified) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            updateUI(user)
                            val intent = Intent(this, MapsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
        // [END sign_in_with_email]
    }

    private fun updateUI(user: FirebaseUser?) {
    }

    private fun reload() {
    }

    companion object {
        private const val TAG = "Login"
    }
}