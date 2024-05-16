package com.example.pi_time11

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
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
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract

class LiberarLocacaoActivity : AppCompatActivity() {
    
    private lateinit var btnVoltar: Button
    private lateinit var edtText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liberar_locacao)

        btnVoltar = findViewById(R.id.btnVoltar)
        edtText = findViewById(R.id.txt_qrcode)

        btnVoltar.setOnClickListener{  /// TODO: APENAS TESTE NÃO É PARA VOLTAR PRO MAPA, REDIRECIONAR PARA O LOCAL CERTO
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
        CheckCameraPermissions(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
            if (isGranted){
                showCamera()
            }
            else{
                Toast.makeText(this, "Sem permissão", Toast.LENGTH_SHORT).show()
            }
        }
    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result ->
            run {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
                } else {
                    setResult(result.contents)
                }
            }
        }
    private fun voltarParaMapa(){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun setResult(result: String){
        // Imprime o resultado completo para verificação
        Log.d(TAG, "Resultado completo do QR Code: $result")

        // Verifica se o resultado contém o separador esperado
        if (result.contains(";")) {
            // Divide a string do QR Code e pega a primeira parte (ID do armário)
            val armarioId = result.split(";")[0]
            val gerenteNome = result.split(";")[1]
            val localizacao = result.split(";")[2]
            val locToString = if (localizacao == "011") "Jundiaí" else "Campinas"
            val tempoSelecionado = result.split(";")[3]

            "Gerente:$gerenteNome\n Localização: $locToString\n Tempo: $tempoSelecionado".also { edtText.text = it }

            // Log para verificar o ID do armário
            Log.d(TAG, "ID do Armário: $armarioId")

            // Chama a função para verificação de disponibilidade
            verificarDisponibilidadeArmario(armarioId)

        } else {
            // Caso não encontre o separador esperado, loga um erro ou mostra uma mensagem
            Log.e(TAG, "Formato do QR Code não esperado: $result")
            Toast.makeText(this, "Formato do QR Code inválido.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showCamera() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setBarcodeImageEnabled(true)
        options.setPrompt("QRCode Scanner")
        options.setCameraId(0)
        options.setOrientationLocked(false)

        scanLauncher.launch(options)
    }
    private fun CheckCameraPermissions(context: Context)
    {
        if (ContextCompat.checkSelfPermission(context,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                showCamera()
        }
        else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA))
        {
            Toast.makeText(this, "Permissão para utilizar a camera é necessario", Toast.LENGTH_SHORT).show()
        }
        else{
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun verificarDisponibilidadeArmario(armarioId: String) {
        val docRef = FirebaseFirestore.getInstance().collection("armarios").document(armarioId)
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val isDisponivel = document.getBoolean("disponivel") ?: false
                if (isDisponivel) {
                    // Armário disponível
                    Toast.makeText(this, "Armário Disponivel", Toast.LENGTH_LONG).show()
                    AlocarArmario(armarioId) // se estava disponivel agora não esta mais -> false
                } else {
                    // Armário não disponível
                    voltarParaMapa()
                    Toast.makeText(this, "Armário não está disponível", Toast.LENGTH_LONG).show()
                }
            } else {
                voltarParaMapa()
                Toast.makeText(this, "Armário não encontrado", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao acessar dados", Toast.LENGTH_SHORT).show()
        }
    }

    fun AlocarArmario(armarioId: String) {
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
    fun LiberarArmario(armarioId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("armarios")
            .document(armarioId)
            .update("disponivel", true)  // Define o armário como disponível
            .addOnSuccessListener {
                Log.d("Firestore", "Armário $armarioId liberado e marcado como não disponível.")
                // mudar para outra tela ou mostrar uma confirmação para o usuário?
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Erro ao liberar o armário $armarioId", e)
            }
    }
    companion object {
        private const val TAG = "LiberarLocacaoActivity"
        private const val CAMERA_PERMISSION_REQUEST = 123
    }
}