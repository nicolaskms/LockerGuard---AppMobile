package com.example.pi_time11

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputLayout
    private lateinit var buttonEnviar: Button
    private lateinit var buttonVoltar: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        etEmail = findViewById(R.id.etEmail)
        buttonEnviar = findViewById(R.id.buttonEnviar)
        buttonVoltar = findViewById(R.id.buttonVoltar)

        auth = FirebaseAuth.getInstance()

        buttonEnviar.setOnClickListener {
            val sEmail = etEmail.editText?.text.toString()
            sendPasswordReset(sEmail)
        }

        buttonVoltar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //função recuperar senha
    private fun sendPasswordReset(emailAddress: String) {
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    Toast.makeText(baseContext, "Email enviado!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}