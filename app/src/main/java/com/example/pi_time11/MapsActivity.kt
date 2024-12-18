package com.example.pi_time11

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var buttonCadastrarCartao: Button
    private lateinit var textAviso: TextView
    private lateinit var buttonSair: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize views
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        buttonSair = findViewById(R.id.buttonSair)
        buttonCadastrarCartao = findViewById(R.id.btn_CadastrarCartao)
        textAviso = findViewById(R.id.TextViewMaps)
        auth = Firebase.auth

        // Set button listeners
        buttonSair.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonCadastrarCartao.setOnClickListener {
            val tela = "1"
            val intent = Intent(this, CartaoActivity::class.java)
            intent.putExtra("tela", tela)
            startActivity(intent)
            finish()
        }

        // Check if user is logged in
        val user = FirebaseAuth.getInstance().currentUser?.uid
        if (user == null) {
            buttonCadastrarCartao.visibility = View.GONE
            textAviso.text = "Crie uma conta ou faça login para alugar um armário!"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

        // Add markers
        val jundiai = LatLng(-23.184810613775756, -46.97102255357517)
        mMap.addMarker(
            MarkerOptions()
                .position(jundiai)
                .title("LockerGuard - Jundiaí")
                .snippet("Rua Chiara Lubich - Jundiai")
        )

        val campinas = LatLng(-22.83410834334813, -47.052841723275584)
        mMap.addMarker(
            MarkerOptions()
                .position(campinas)
                .title("LockerGuard - Campinas")
                .snippet("Av. Reitor Benedito José Barreto Fonseca, Prédio H15 - Parque dos Jacarandás, Campinas")
        )

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_LOCATION
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permissão concedida, agora podemos atualizar a localização do usuário
                    onMapReady(mMap)
                } else {
                    Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.title == "LockerGuard - Jundiaí" || marker.title == "LockerGuard - Campinas") {
            // Obtém a localização do usuário
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { userLocation ->
                    // Calcula a distância entre a localização do usuário e o marcador do armário
                    val distanciaParaArmario = calcularDistancia(marker.position, userLocation)
                    // Abre a tela do armário apenas se a distância for menor ou igual a 1km (1000 metros)
                    if (distanciaParaArmario <= 1000) {
                        val db = FirebaseFirestore.getInstance()
                        val pedidosRef = db.collection("pedidos")

                        // Consulta para buscar o pedido do usuário para o armário
                        val usuarioId = auth.currentUser?.uid
                        val armarioId = if (marker.title == "LockerGuard - Jundiaí") "011" else "012"
                        val localizacao = marker.snippet ?: ""

                        pedidosRef.whereEqualTo("userId", usuarioId)
                            .whereEqualTo("localId", armarioId)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    // Se existir um pedido, redireciona para a tela de QrCodeActivity
                                    val intent = Intent(this, QrCodeActivity::class.java)
                                    val documento = documents.first()
                                    val tempo = documento.getString("tempo")

                                    intent.putExtra("id", armarioId)
                                    intent.putExtra("localizacao", localizacao)
                                    intent.putExtra("tempoSelecionado", tempo)

                                    startActivity(intent)
                                } else {
                                    // Se não houver pedido, redireciona para a tela de detalhes do armário
                                    abrirDetalhesArmario(armarioId)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Erro ao buscar dados do Firestore: ", exception)
                                Toast.makeText(
                                    baseContext,
                                    "Faça login para alugar um armário",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Você está muito longe do armário para abrir.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            return true
        }
        return false
    }

    private fun abrirDetalhesArmario(armarioId: String) {
        val db = FirebaseFirestore.getInstance()
        val armariosRef = db.collection("armarios")

        armariosRef.whereEqualTo("id", armarioId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val documento = documents.first()
                    val tempo = documento.getString("tempo")
                    val localizacao = if (armarioId == "011") "Rua Chiara Lubich - Jundiai" else "Av. Reitor Benedito José Barreto Fonseca, Prédio H15 - Parque dos Jacarandás, Campinas"

                    // Criando Intent e passando os dados com o putExtra()
                    val intent = Intent(this, ArmarioActivity::class.java)
                    intent.putExtra("id", armarioId)
                    intent.putExtra("localizacao", localizacao)
                    intent.putExtra("tempoSelecionado", tempo)

                    startActivity(intent)
                } else {
                    Toast.makeText(
                        baseContext,
                        "Nenhum documento encontrado com o ID '$armarioId'.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Erro ao buscar dados do Firestore: ", exception)
                Toast.makeText(
                    baseContext,
                    "Erro ao buscar dados do Firestore.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun calcularDistancia(markerPosition: LatLng, userLocation: Location): Float {
        val locationMarker = Location("").apply {
            latitude = markerPosition.latitude
            longitude = markerPosition.longitude
        }
        return userLocation.distanceTo(locationMarker)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1
    }
}
