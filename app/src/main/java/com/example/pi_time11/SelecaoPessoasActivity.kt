package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SelecaoPessoasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecao_pessoas)

        val buttonConfirmar = findViewById<Button>(R.id.buttonConfirmar)
        buttonConfirmar.setOnClickListener {

            val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                val radioButton = findViewById<RadioButton>(selectedRadioButtonId)
                val selectedOption = radioButton.text.toString()
                Toast.makeText(this, "Opção selecionada: $selectedOption", Toast.LENGTH_SHORT).show()

                if (selectedOption == "Uma pessoa")
                {
                    irParaCamera()
                    intent.putExtra("pessoas",1)
                }
                else
                {
                    irParaCamera()
                    intent.putExtra("pessoas",2)
                }

            } else {
                Toast.makeText(this, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irParaCamera(){
        val idPedido = intent.getStringExtra("idPedido")
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("idPedido",idPedido)
        startActivity(intent)
        finish()
    }
}