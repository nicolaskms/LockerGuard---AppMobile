package com.example.pi_time11

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class LiberarLocacaoActivity : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var edtText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liberar_locacao)

        btnVoltar = findViewById(R.id.btnVoltar)
        edtText = findViewById(R.id.txt_qrcode)

        btnVoltar.setOnClickListener{  /// TODO: APENAS TESTE NÃO É PARA VOLTAR PRO MAPA, REDIRECIONAR PARA O LOCAL CERTO
            val intent = Intent(this, GerenteActivity::class.java)
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
        val intent = Intent(this, GerenteActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun selecaoDePessoas(){
        val intent = Intent(this, SelecaoPessoasActivity::class.java)
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
            val clientenome = result.split(";")[1]
            val localizacao = result.split(";")[2]
            val tempoSelecionado = result.split(";")[3]

            "Cliente:$clientenome\n Localização: $localizacao\n Tempo: $tempoSelecionado".also { edtText.text = it }

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
        val db = FirebaseFirestore.getInstance()
        val armariosRef = db.collection("armarios").document(armarioId)
        val unidadesRef = armariosRef.collection("unidades")

        unidadesRef.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val isDisponivel = document.getBoolean("status") ?: false
                if (isDisponivel) {
                    // Armário disponível
                    Toast.makeText(this, "Armário Disponível", Toast.LENGTH_LONG).show()
                    AlocarArmario(armarioId) // Se estava disponível agora não está mais -> false

                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        selecaoDePessoas() // Vai para seleção de pessoas após 1.5s
                    }, 1500)
                    return@addOnSuccessListener
                }
            }
            // Se nenhum documento estiver disponível
            voltarParaMapa()
            Toast.makeText(this, "Nenhum armário disponível", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            // Em caso de falha ao acessar os dados
            voltarParaMapa()
            Toast.makeText(this, "Erro ao acessar dados: ${exception.message}", Toast.LENGTH_SHORT).show()
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