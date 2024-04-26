package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import java.util.Calendar

class OpcoesActivity : AppCompatActivity() {

    private lateinit var buttonVoltar: Button
    private lateinit var buttonProsseguir: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton5: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opcoes)

        buttonVoltar = findViewById(R.id.btnVoltar)
        buttonProsseguir = findViewById(R.id.btn_Continuar)
        radioGroup = findViewById(R.id.radioGroup)
        radioButton5 = findViewById(R.id.radioButton5)

        buttonVoltar.setOnClickListener {
            finish()
        }

        val id = intent.getStringExtra("id")
        val localizacao = intent.getStringExtra("localizacao")

        // Obtenha a hora atual
        val calendar = Calendar.getInstance()
        val horaAtual = calendar.get(Calendar.HOUR_OF_DAY)

        // Defina a visibilidade do radioButton5 com base na hora atual
        if (horaAtual >= 7 && horaAtual <= 8) {
            radioButton5.visibility = View.VISIBLE
        } else {
            radioButton5.visibility = View.GONE
        }

        buttonProsseguir.setOnClickListener {
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId == -1) {
                Toast.makeText(this, "Escolha uma opção para continuar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val radioButton = findViewById<RadioButton>(selectedRadioButtonId)
            val tempoSelecionado = radioButton?.text.toString()
            val intent = Intent(this, PagamentoActivity::class.java).apply {
                putExtra("tempoSelecionado", tempoSelecionado)
                putExtra("localizacao", localizacao)
                putExtra("id", id)
            }
            startActivity(intent)
            finish()
        }
    }
}
