package com.example.pi_time11

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class QrCodeActivity : AppCompatActivity() {

    private lateinit var buttonVoltar: Button
    private lateinit var buttonContinuar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)
        buttonVoltar = findViewById(R.id.buttonVoltar)
        //buttonContinuar = findViewById(R.id.buttonContinuar)

    }




}