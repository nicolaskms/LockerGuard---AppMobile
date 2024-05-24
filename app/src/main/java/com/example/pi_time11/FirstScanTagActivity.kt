package com.example.pi_time11

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("NAME_SHADOWING")
class FirstScanTagActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var buttonVoltar: Button

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

        buttonVoltar = findViewById(R.id.btnVoltar)
        buttonVoltar.setOnClickListener {
            val intent = Intent(this, GerenteActivity::class.java)
            startActivity(intent)
            finish()
        }
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
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                handleTag(tag)
            }
        } else {
            Log.e("NFC_TAG", "Intenção NFC desconhecida: ${intent?.action}")
        }
    }

    private fun handleTag(tag: Tag) {
        val idPedido = intent.getStringExtra("idPedido") ?: return
        val ndef = Ndef.get(tag)

        if (ndef != null) {
            ndef.connect()
            val ndefMessage = ndef.cachedNdefMessage
            val tagContent = ndefMessage?.records?.getOrNull(0)?.payload?.let { String(it).substringOrNull(3) }

            if (!tagContent.isNullOrBlank()) {
                Log.d("NFC_TAG", "Conteúdo atual da tag: $tagContent")
            } else {
                Log.d("NFC_TAG", "A tag está vazia")
            }

            val updatedRecord = NdefRecord.createTextRecord("en", idPedido)
            val updatedMessage = NdefMessage(arrayOf(updatedRecord))
            ndef.writeNdefMessage(updatedMessage)
            ndef.close()

            Log.d("NFC_TAG", "ID do pedido escrito na tag NFC: $idPedido")
            Toast.makeText(this, "Tag NFC atualizada com o ID do pedido.", Toast.LENGTH_SHORT).show()

            updatePedidoWithNfc(idPedido)
        } else {
            Log.e("NFC_TAG", "A tag não suporta NDEF")
            Toast.makeText(this, "A tag não suporta NDEF.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun String.substringOrNull(startIndex: Int): String? {
        return if (startIndex in 0 until length) {
            substring(startIndex)
        } else {
            null
        }
    }


    private fun updatePedidoWithNfc(idPedido: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("pedidos")
                .whereEqualTo(FieldPath.documentId(), idPedido)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val pedidoId = document.id
                            db.collection("pedidos").document(pedidoId)
                                .update("nfc", true)
                                .addOnSuccessListener {
                                    Log.d("NFC_TAG", "Documento do pedido atualizado com sucesso.")
                                    Toast.makeText(this, "Pedido atualizado com status NFC.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("NFC_TAG", "Erro ao atualizar o documento do pedido", e)
                                    Toast.makeText(this, "Erro ao atualizar o pedido.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.d("NFC_TAG", "Nenhum documento encontrado para o usuário.")
                        Toast.makeText(this, "Nenhum pedido encontrado para o usuário.", Toast.LENGTH_SHORT).show()
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