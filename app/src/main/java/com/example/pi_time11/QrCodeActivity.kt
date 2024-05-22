package com.example.pi_time11

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

class QrCodeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonVoltar: ImageButton
    private lateinit var armarioIdTextView: TextView
    private lateinit var qrCodeImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)
        auth = Firebase.auth

        // Inicialização dos elementos da UI
        buttonVoltar = findViewById(R.id.btnVoltar)
        armarioIdTextView = findViewById(R.id.armario_id) // Inicializando o TextView aqui
        qrCodeImageView = findViewById(R.id.imageViewQRCode)

        // Recebendo o ID do armário enviado pela intent
        val armarioId = intent.getStringExtra("id")

        // Exibindo o ID do armário no TextView
        armarioId?.let {
            armarioIdTextView.text = armarioId
        }

        // Consulta ao Firestore para obter o nome do cliente usando o ID do usuário autenticado
        // Consulta ao Firestore para obter o nome do cliente usando o ID do usuário autenticado
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(auth.currentUser?.uid ?: "")

        userRef.get()
            .addOnSuccessListener { userDocument ->
                if (userDocument != null && userDocument.exists()) {
                    val nomeDoCliente = userDocument.getString("name")
                    val tempoSelecionado = intent.getStringExtra("tempoSelecionado") ?: "Null"
                    val locString = if (armarioId == "011") "Jundiaí" else "Campinas" // Corrigido aqui
                    val qrCodeData = "$armarioId;$nomeDoCliente;$locString;$tempoSelecionado"
                    val bitmap = generateQRCode(qrCodeData)
                    qrCodeImageView.setImageBitmap(bitmap)
                } else {
                    Log.d(TAG, "Documento de usuário não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Falha ao obter documento de usuário: ", exception)
            }


        // Ação do botão "Voltar"
        buttonVoltar.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Consulta ao Firestore para obter outros dados do armário, se necessário
        val armariosRef = FirebaseFirestore.getInstance().collection("armarios").document(armarioId ?: "01")

        armariosRef.get() // inicia a consulta ao Firestore para obter o documento da coleção "armarios"
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val armario = document.getString("armario")
                    // Exibir outros dados do armário, se necessário
                } else {
                    Log.d(TAG, "Documento não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Falha ao obter documento: ", exception)
            }
    }

    private fun generateQRCode(text: String): Bitmap? {
        val multiFormatWriter = MultiFormatWriter()
        return try {
            val bitMatrix: BitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 800, 800)
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val TAG = "QrCodeActivity"
    }
}
