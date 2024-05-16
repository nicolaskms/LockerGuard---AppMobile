package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class OpcoesActivity : AppCompatActivity() {

    private lateinit var buttonVoltar: Button
    private lateinit var buttonProsseguir: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton5: RadioButton
    private lateinit var radioButton4: RadioButton
    private lateinit var radioButton3: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton1: RadioButton
    private lateinit var fechado: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opcoes)

        buttonVoltar = findViewById(R.id.btnVoltar)
        buttonProsseguir = findViewById(R.id.btn_Continuar)
        radioGroup = findViewById(R.id.radioGroup)
        radioButton5 = findViewById(R.id.radioButton5)
        radioButton4 = findViewById(R.id.radioButton4)
        radioButton3 = findViewById(R.id.radioButton3)
        radioButton2 = findViewById(R.id.radioButton2)
        radioButton1 = findViewById(R.id.radioButton1)
        fechado = findViewById(R.id.fechado)

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
        if(horaAtual >= 14){
            radioButton4.visibility = View.GONE
        }
        if (horaAtual >= 16){
            radioButton3.visibility = View.GONE
        }
        if(horaAtual >= 17){
            radioButton2.visibility = View.GONE
        }
        if (horaAtual >= 18){
            radioButton1.visibility = View.GONE
            fechado.visibility = View.VISIBLE
            buttonProsseguir.visibility = View.GONE
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
