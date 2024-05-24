package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PagamentoActivity : AppCompatActivity() {

    private lateinit var buttonVoltar: Button
    private lateinit var buttonCadastrarCartao: Button
    private lateinit var buttonContinuar: Button
    private lateinit var textViewIdArmario: TextView
    private lateinit var textViewInformacoesCompra: TextView
    private lateinit var textViewLoc: TextView
    private lateinit var textViewApelidoCartao: TextView

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
        textViewLoc = findViewById(R.id.textViewLoc)
        textViewApelidoCartao = findViewById(R.id.textViewApelidoCartao)

        textViewIdArmario.text = "Id do Armario:$id";
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

        textViewLoc.text = "Localização:$localizacao"

        // Verificar se o usuário está autenticado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            // Verificar se o usuário possui algum cartão salvo
            db.collection("cartoes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Se houver cartões salvos, exibir o apelido do cartão em um TextView
                        val cartao = documents.documents[0]
                        val apelido = cartao.getString("Apelido")
                        textViewApelidoCartao.text = "Cartão cadastrado: $apelido"
                        textViewApelidoCartao.visibility = View.VISIBLE
                        buttonCadastrarCartao.text = "Alterar Cartão"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao buscar cartões do usuário", e)
                    // Tratar falha na busca dos cartões
                }
        } else {
            Log.d(TAG, "Usuário não autenticado")
            // Adicione um código aqui para lidar com o usuário não autenticado, se necessário
        }

        buttonCadastrarCartao = findViewById(R.id.btn_CadastrarCartao)
        buttonVoltar = findViewById(R.id.btnVoltar)
        buttonContinuar = findViewById(R.id.btnProsseguir)

            buttonCadastrarCartao.setOnClickListener {
                val firebaseAuth = FirebaseAuth.getInstance()
                val usuarioAtual = firebaseAuth.currentUser

                if (usuarioAtual != null && usuarioAtual.isEmailVerified) {
                    val tela = "2"
                            val intent = Intent(this, CartaoActivity::class.java).apply {
                                putExtra("id", id)
                                putExtra("tempoSelecionado", tempoSelecionado)
                                putExtra("localizacao", localizacao)
                                putExtra("tela",tela)
                            }
                            startActivity(intent)
                            finish()

                } else {
                    // Se o usuário não estiver logado ou o email não estiver verificado, exibir uma mensagem
                    Toast.makeText(this, "Faça login e verifique seu e-mail para continuar", Toast.LENGTH_SHORT).show()
                }
            }

        buttonVoltar.setOnClickListener{
            val intent = Intent(this, OpcoesActivity::class.java)
            intent.putExtra("localizacao",localizacao)
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
                val tempoFormatado = when (tempoSelecionado) {
                    "30 Min / 30R$" -> "30 min"
                    "1 Hora / 55R$" -> "1 hora"
                    "2 Horas / 110R$" -> "2 horas"
                    "4 Horas / 200 R$" -> "4 horas"
                    "Diária / 300 R$" -> "Diária"
                    else -> "Erro"
                }
                val pedido = hashMapOf(
                    "tempo" to tempoFormatado, // Aqui utilizamos a variável tempoFormatado
                    "valor" to valorSelecionado,
                    "userId" to userId,
                    "localId" to id
                )
                db.collection("pedidos")
                    .add(pedido)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "Pedido adicionado com ID: ${documentReference.id}")
                        Toast.makeText(this, "Pedido feito com sucesso!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, QrCodeActivity::class.java)
                        intent.putExtra("id", id)
                        intent.putExtra("tempoSelecionado", tempoFormatado)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erro ao adicionar pedido", e)
                    }
            }
        }

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            // Verificar se o usuário possui algum cartão salvo
            db.collection("cartoes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Se houver cartões salvos, exibir o apelido do cartão na text
                        val cartao = documents.documents[0]
                        val apelido = cartao.getString("Apelido")
                        textViewApelidoCartao.text = "Cartão cadastrado: $apelido"
                        textViewApelidoCartao.visibility = View.VISIBLE
                        buttonCadastrarCartao.text = "Alterar Cartão"
                        buttonContinuar.isEnabled = true // Habilitar o botão de locação
                    } else {
                        // Nenhum cartão cadastrado, desabilitar o botão de locação
                        buttonContinuar.isEnabled = false
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao buscar cartões do usuário", e)
                }
        } else {
            Log.d(TAG, "Usuário não autenticado")
            Toast.makeText(this, "Faça login para alugar um armário.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "ArmarioActivity"
    }
}
