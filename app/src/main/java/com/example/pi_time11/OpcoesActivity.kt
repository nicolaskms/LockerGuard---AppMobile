package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup

class OpcoesActivity : AppCompatActivity() {

    private lateinit var buttonVoltar: Button
    private lateinit var buttonProsseguir: Button
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opcoes)

        buttonVoltar = findViewById(R.id.btnVoltar)
        buttonProsseguir = findViewById(R.id.btn_Continuar)
        radioGroup = findViewById(R.id.radioGroup)

        buttonVoltar.setOnClickListener {
            finish()
        }

        val id = intent.getStringExtra("id")
        val localizacao = intent.getStringExtra("localizacao")
        buttonProsseguir.setOnClickListener {
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(selectedRadioButtonId)
            val tempoSelecionado = radioButton?.text.toString()
            val intent = Intent(this, PagamentoActivity::class.java).apply {
                putExtra("tempoSelecionado", tempoSelecionado)
                putExtra("localizacao",localizacao)
                putExtra("id", id)
            }
            startActivity(intent)
            finish()
        }
    }
}
