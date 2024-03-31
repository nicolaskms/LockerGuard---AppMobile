package com.example.pi_time11

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout

class CadastroActivity : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrar: Button
    private lateinit var etNome: TextInputLayout
    private lateinit var etEmail: TextInputLayout
    private lateinit var etIdade: TextInputLayout
    private lateinit var etCPF: TextInputLayout
    private lateinit var etCelular: TextInputLayout
    private lateinit var etSenha: TextInputLayout

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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnVoltar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            //proximo finish() estudar se é realmente necessario
            finish()
        }
    }
}