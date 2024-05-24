package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SelecaoPessoasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecao_pessoas)

        val buttonConfirmar = findViewById<Button>(R.id.buttonConfirmar)
        buttonConfirmar.setOnClickListener {

            val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {

                val selectedOption = when (radioGroup.checkedRadioButtonId) {
                    R.id.rUmaPessoa -> 1
                    R.id.rDuasPessoas -> 2
                    else -> 1
                }

                intent.putExtra("numPessoas", selectedOption)
                irParaCamera()

            } else {
                Toast.makeText(this, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irParaCamera(){
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
        finish()
    }
}