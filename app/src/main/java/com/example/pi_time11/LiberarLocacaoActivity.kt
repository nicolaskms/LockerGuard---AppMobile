package com.example.pi_time11

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DecoratedBarcodeView.TorchListener
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.journeyapps.barcodescanner.BarcodeResult
import com.google.firebase.firestore.FirebaseFirestore

class LiberarLocacaoActivity : AppCompatActivity(), DecoratedBarcodeView.TorchListener {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var beepManager: BeepManager
    private var lastText: String? = null
    private var isTorchOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liberar_locacao)

        // Inicializa a view do scanner
        barcodeView = findViewById(R.id.barcode_scanner)
        barcodeView.setTorchListener(this)

        // Inicializa o gerenciador de beep
        beepManager = BeepManager(this)

        // Verifica e solicita permissão para usar a câmera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        } else {
            // se for permitido inicia o scanner
            barcodeView.resume()
        }

        // Configura as configurações da câmera
        val cameraSettings = CameraSettings()
        barcodeView.cameraSettings = cameraSettings

        // Define o listener de resultados de digitalização
        barcodeView.decodeSingle { result: BarcodeResult? ->
            result?.let {
                onBarcodeScanned(it.result)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, inicia o scanner
                barcodeView.resume()
            } else {
                // Permissão negada, exibe uma mensagem de erro
                Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onBarcodeScanned(result: Result) {
        val fullResult = result.text
        if (fullResult != lastText) {
            lastText = fullResult

            // Divide a string do QR Code e pega a primeira parte (ID do armário) nao tem necessidade do resto das infos
            val armarioId = fullResult.split(";")[0]

            // Log para verificar o resultado
            Log.d(TAG, "ID do Armário: $armarioId")

            // Chama a função para verificao de disponibilidade
            verificarDisponibilidadeArmario(armarioId)

            // Beep e vibração ao escanear o QR Code
            beepManager.playBeepSoundAndVibrate()
        }
    }

    private fun verificarDisponibilidadeArmario(armarioId: String) {
        val docRef = FirebaseFirestore.getInstance().collection("armarios").document(armarioId)
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val isDisponivel = document.getBoolean("disponivel") ?: false
                if (isDisponivel) {
                    // Armário disponível
                    liberarArmario(armarioId) // se estava disponivel agora não esta mais -> false
                } else {
                    // Armário não disponível
                    Toast.makeText(this, "Armário não está disponível", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Armário não encontrado", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao acessar dados", Toast.LENGTH_SHORT).show()
        }
    }

    fun liberarArmario(armarioId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("armarios")
            .document(armarioId)
            .update("disponivel", false)  // Define o armário como não disponível
            .addOnSuccessListener {
                Log.d("Firestore", "Armário $armarioId liberado e marcado como não disponível.")
                // mudar para outra tela ou mostrar uma confirmação para o usuário?
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao liberar o armário $armarioId", e)
            }
    }

    override fun onTorchOn() {
        isTorchOn = true
    }

    override fun onTorchOff() {
        isTorchOn = false
    }

    companion object {
        private const val TAG = "LiberarLocacaoActivity"
        private const val CAMERA_PERMISSION_REQUEST = 123
    }
}