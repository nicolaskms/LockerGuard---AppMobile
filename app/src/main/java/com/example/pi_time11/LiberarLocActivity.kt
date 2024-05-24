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

class LiberarLocActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var btnvoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liberar_loc)
        btnvoltar = findViewById(R.id.btnVoltar)
        btnvoltar.setOnClickListener {
            val intent = Intent(this, GerenteActivity::class.java)
            startActivity(intent)
        }

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
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                handleTag(tag)
            }
        } else {
            Log.e("NFC_TAG", "Intenção NFC desconhecida: ${intent?.action}")
        }
    }

    private fun handleTag(tag: Tag) {
        val ndef = Ndef.get(tag)

        if (ndef != null) {
            ndef.connect()
            val ndefMessage = ndef.cachedNdefMessage
            val ndefRecord = ndefMessage?.records?.get(0)
            val tagContent = ndefRecord?.payload?.let { String(it).substring(3) } // Remove prefixo de codificação de texto

            if (tagContent != null) {
                Log.d("NFC_TAG", "Conteúdo da tag: $tagContent")
                Toast.makeText(this, "Conteúdo da tag: $tagContent", Toast.LENGTH_SHORT).show()

                // Exclui o pedido e limpa a tag
                deletePedidoWithNfcId(tagContent) { success ->
                        clearTag(ndef)
                    val intent = Intent(this, GerenteActivity::class.java)
                    startActivity(intent)
                }
            } else {
                Log.d("NFC_TAG", "A tag está vazia")
                Toast.makeText(this, "A tag está vazia.", Toast.LENGTH_SHORT).show()
            }
            ndef.close()
        } else {
            Log.e("NFC_TAG", "A tag não suporta NDEF")
            Toast.makeText(this, "A tag não suporta NDEF.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePedidoWithNfcId(nfcId: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("pedidos")
                .whereEqualTo(FieldPath.documentId(), nfcId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            db.collection("pedidos").document(nfcId)
                                .delete()
                                .addOnSuccessListener {
                                    Log.d("NFC_TAG", "Documento do pedido deletado com sucesso.")
                                    Toast.makeText(this, "Pedido deletado com sucesso.", Toast.LENGTH_SHORT).show()
                                    callback(true)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("NFC_TAG", "Erro ao deletar o documento do pedido", e)
                                    Toast.makeText(this, "Erro ao deletar o pedido.", Toast.LENGTH_SHORT).show()
                                    callback(false)
                                }
                        }
                    } else {
                        Log.d("NFC_TAG", "Nenhum documento encontrado com o ID da pulseira.")
                        Toast.makeText(this, "Nenhum pedido encontrado com o ID da pulseira.", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("NFC_TAG", "Erro ao buscar documentos", e)
                    Toast.makeText(this, "Erro ao buscar pedidos.", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }

    private fun clearTag(ndef: Ndef) {
        try {
            ndef.connect()

            // Cria um registro vazio NDEF
            val emptyRecord = NdefRecord(NdefRecord.TNF_EMPTY, ByteArray(0), ByteArray(0), ByteArray(0))
            val emptyMessage = NdefMessage(arrayOf(emptyRecord))

            // Escreve a mensagem vazia na tag
            ndef.writeNdefMessage(emptyMessage)

            Log.d("NFC_TAG", "Tag NFC zerada com sucesso.")
            Toast.makeText(this, "Tag NFC zerada com sucesso.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("NFC_TAG", "Erro ao zerar a tag NFC", e)
            Toast.makeText(this, "Erro ao zerar a tag NFC.", Toast.LENGTH_SHORT).show()
        } finally {
            ndef.close()
        }
    }
}
