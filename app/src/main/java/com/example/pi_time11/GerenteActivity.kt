package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.google.firebase.auth.FirebaseAuth

class GerenteActivity : AppCompatActivity() {
    private lateinit var btnLocarArmario: AppCompatImageButton
    private lateinit var btnLiberarLoc: AppCompatImageButton
    private lateinit var buttonSair: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerente)

        btnLocarArmario = findViewById(R.id.btnLocarArmario)
        btnLiberarLoc = findViewById(R.id.btnLiberarLoc)
        buttonSair = findViewById(R.id.buttonSair)
        auth = FirebaseAuth.getInstance()


        btnLiberarLoc.setOnClickListener {
            val intent = Intent(this, LiberarLocActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnLocarArmario.setOnClickListener {
            val intent = Intent(this, LiberarLocacaoActivity::class.java)
            startActivity(intent)
            finish()
        }
        buttonSair.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}