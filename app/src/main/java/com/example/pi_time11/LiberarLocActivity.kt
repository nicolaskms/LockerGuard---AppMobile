package com.example.pi_time11

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LiberarLocActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_scan_tag)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC não está disponível", Toast.LENGTH_LONG).show()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            Log.d("NFC_TAG", "Nova intenção NFC recebida")
            try {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                if (rawMessages != null && rawMessages.isNotEmpty()) {
                    val ndefMessages = rawMessages.map { it as NdefMessage }
                    val payload = ndefMessages[0].records[0].payload
                    val tagContent = String(payload).substring(3) // Remove prefixo de codificação de texto
                    Toast.makeText(this, "Conteúdo da tag: $tagContent", Toast.LENGTH_SHORT).show()
                    deletePedidoWithNfcId(tagContent)
                } else {
                    Log.e("NFC_TAG", "Nenhuma mensagem NDEF encontrada")
                    Toast.makeText(this, "Nenhuma mensagem NDEF encontrada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("NFC_TAG", "Erro ao processar a intenção NFC", e)
                Toast.makeText(this, "Erro ao processar a intenção NFC", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("NFC_TAG", "Intenção NFC desconhecida: ${intent?.action}")
        }
    }

    private fun deletePedidoWithNfcId(nfcId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("pedidos")
                .whereEqualTo("nfc", nfcId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val pedidoId = document.id
                            db.collection("pedidos").document(pedidoId)
                                .delete()
                                .addOnSuccessListener {
                                    Log.d("NFC_TAG", "Documento do pedido deletado com sucesso.")
                                    Toast.makeText(this, "Pedido deletado com sucesso.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("NFC_TAG", "Erro ao deletar o documento do pedido", e)
                                    Toast.makeText(this, "Erro ao deletar o pedido.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.d("NFC_TAG", "Nenhum documento encontrado com o ID da pulseira.")
                        Toast.makeText(this, "Nenhum pedido encontrado com o ID da pulseira.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("NFC_TAG", "Erro ao buscar documentos", e)
                    Toast.makeText(this, "Erro ao buscar pedidos.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
        }
    }
}
