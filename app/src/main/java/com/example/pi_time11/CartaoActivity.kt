package com.example.pi_time11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartaoActivity : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrar: Button
    private lateinit var etCard: TextInputLayout
    private lateinit var etCvv: TextInputLayout
    private lateinit var etDataval: TextInputLayout
    private lateinit var etCPF: TextInputLayout
    private lateinit var etApelido: TextInputLayout

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cartao)

        val tempoSelecionado = intent.getStringExtra("tempoSelecionado")
        val id = intent.getStringExtra("id")
        val localizacao = intent.getStringExtra("localizacao")
        val tela = intent.getStringExtra("tela")
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnVoltar = findViewById(R.id.btnVoltar)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        etCard = findViewById(R.id.etCard)
        etCvv = findViewById(R.id.etCvv)
        etDataval = findViewById(R.id.etDataval)
        etCPF = findViewById(R.id.etCPF)
        etApelido = findViewById(R.id.etApelido)

        btnVoltar.setOnClickListener {
            if (tela == "1"){
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                val intent = Intent(this, PagamentoActivity::class.java)
                intent.putExtra("tempoSelecionado", tempoSelecionado)
                intent.putExtra("id", id)
                intent.putExtra("localizacao", localizacao)
                startActivity(intent)
                finish()
            }
        }

        btnCadastrar.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()
            val usuarioAtual = firebaseAuth.currentUser
            val db = FirebaseFirestore.getInstance()
            val userId = usuarioAtual?.uid

            val sCard = etCard.editText?.text.toString()
            val sCvv = etCvv.editText?.text.toString()
            val sDataval = etDataval.editText?.text.toString()
            val sCPF = etCPF.editText?.text.toString()
            val sApelido = etApelido.editText?.text.toString()

            if (sCard.isNotEmpty() && sCvv.isNotEmpty() && sDataval.isNotEmpty() &&
                sCPF.isNotEmpty() && sApelido.isNotEmpty()) {

                // Antes de adicionar um novo cartão, exclui todos os cartões do usuário atual
                db.collection("cartoes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Erro ao excluir cartão", e)
                                }
                        }
                        // Depois que todos os cartões são excluídos, adicione o novo cartão
                        adicionarNovoCartao(userId!!, sCard, sCvv, sDataval, sCPF, sApelido, tempoSelecionado, id, localizacao, tela)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Erro ao buscar cartões do usuário", e)
                    }
            } else {
                // Exibe mensagem se algum campo estiver vazio
                Toast.makeText(baseContext, "Por favor, preencha os requisitos de todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função para adicionar um novo cartão
    private fun adicionarNovoCartao(userId: String, sCard: String, sCvv: String, sDataval: String, sCPF: String, sApelido: String, tempoSelecionado: String?, id: String?, localizacao: String?, tela: String?) {
        // Referência para a coleção "cartoes" no Firestore
        val cartoesRef = db.collection("cartoes")

        // Cria um novo documento para o cartão
        val novoCartao = hashMapOf(
            "userId" to userId,
            "card" to sCard,
            "cvv" to sCvv,
            "dataval" to sDataval,
            "cpf" to sCPF,
            "Apelido" to sApelido
        )
        // Adiciona os dados do cartão ao Firestore
        cartoesRef.add(novoCartao)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                // Exibe mensagem de sucesso
                Toast.makeText(this, "Cartão salvo com sucesso!", Toast.LENGTH_SHORT).show()
                Log.d(PagamentoActivity.TAG, "Cartão adicionado com sucesso")
                // Vai para o pagamento
                if (tela == "1"){
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    val intent = Intent(this, PagamentoActivity::class.java)
                    intent.putExtra("tempoSelecionado", tempoSelecionado)
                    intent.putExtra("localizacao",localizacao)
                    intent.putExtra("id", id)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(this, "Erro ao salvar o cartão. Por favor, tente novamente.", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val TAG = "CartaoActivity"
    }
}
