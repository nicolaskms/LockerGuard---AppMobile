package com.example.pi_time11

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PagamentoActivity : AppCompatActivity() {
    private lateinit var buttonVoltar: Button
    private lateinit var buttonCadastrarCartao: Button
    private lateinit var buttonCartaoSalvo: Button
    private lateinit var buttonContinuar: Button
    private lateinit var textViewIdArmario: TextView
    private lateinit var textViewInformacoesCompra: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento)

        // Receber o tempo selecionado da Intent
        val tempoSelecionado = intent.getStringExtra("tempoSelecionado")
        val id = intent.getStringExtra("id")

        val valorSelecionado = when (tempoSelecionado) {
            "30 Min / 30R$" -> 30
            "1 Hora / 55R$" -> 55
            "2 Horas / 110R$" -> 110
            "4 Horas / 200 R$" -> 200
            "Diária / 300 R$" -> 300
            else -> 0 // ou outro valor padrão, se necessário
        }

        // Exibir as informações da compra com base no tempo selecionado
        textViewInformacoesCompra = findViewById(R.id.textViewInformacoesCompra)
        textViewIdArmario = findViewById(R.id.textViewIdArmario)

        textViewIdArmario.text = "Id do Armario:"+id;
        textViewInformacoesCompra.text = when (tempoSelecionado) {
            "30 Min / 30R$" -> "Tempo: 30 min        Valor: R$30"
            "1 Hora / 55R$" -> "Tempo: 1 hora        Valor: R$55"
            "2 Horas / 110R$" -> "Tempo: 2 horas        Valor: R$110"
            "4 Horas / 200 R$" -> "Tempo: 4 horas        Valor: R$200"
            "Diária / 300 R$" -> "Tempo: Diária        Valor: R$300"
            else -> tempoSelecionado
        }

        // Recebendo dados da Intent
        val localizacao = intent.getStringExtra("localizacao")


        buttonCadastrarCartao = findViewById(R.id.btn_CadastrarCartao)
        buttonVoltar = findViewById(R.id.btnVoltar)
        buttonCartaoSalvo = findViewById(R.id.btn_CartaoSalvo)
        buttonContinuar = findViewById(R.id.btnProsseguir)

        buttonCadastrarCartao.setOnClickListener {
            val intent = Intent(this, CartaoActivity::class.java)
            startActivity(intent)
            finish()
        }
        buttonVoltar.setOnClickListener{
            val intent = Intent(this, OpcoesActivity::class.java)
            intent.putExtra("tempoSelecionado", tempoSelecionado)
            intent.putExtra("id", id)
            startActivity(intent)
            finish()
        }

        buttonContinuar.setOnClickListener {
            // Verificar se o usuário está autenticado
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                // Gerar pedido no banco de dados Firestore
                val db = FirebaseFirestore.getInstance()
                val pedido = hashMapOf(
                    "tempo" to tempoSelecionado,
                    "valor" to valorSelecionado,
                    "userId" to userId,
                    "localId" to id
                )

                db.collection("pedidos")
                    .add(pedido)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "Pedido adicionado com ID: ${documentReference.id}")
                        Toast.makeText(this, "Pedido feito com sucesso!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, PagamentoActivity::class.java)
                        intent.putExtra("id", id)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erro ao adicionar pedido", e)
                    }
            } else {
                Log.d(TAG, "Usuário não autenticado")
                // Adicione um código aqui para lidar com o usuário não autenticado, se necessário
            }
        }


        // Exibir informações da compra
        val resumoCompra = "Tempo: $tempoSelecionado"
    }

    companion object {
        private const val TAG = "ArmarioActivity"
    }
}
