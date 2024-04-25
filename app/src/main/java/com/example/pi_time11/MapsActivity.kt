package com.example.pi_time11

import android.Manifest
import com.google.android.gms.location.FusedLocationProviderClient
import android.location.Location
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var buttonCadastrarCartao: Button
    private lateinit var buttonSair: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonSair = findViewById(R.id.buttonSair)

        auth = Firebase.auth

        buttonSair.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

        // Adiciona marcadores
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

        // Obtém e exibe a localização do usuário
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permissão concedida, agora podemos atualizar a localização do usuário
                    onMapReady(mMap)
                } else {
                    Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    // funcao que abre a tela do armario escolhido pelo marker do maps
    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.title == "LockerGuard - Jundiaí" || marker.title == "LockerGuard - Campinas") {
            // Obtém a localização do usuário
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { userLocation ->
                    // Calcula a distância entre a localização do usuário e o marcador do armário
                    val distanciaParaArmario = calcularDistancia(marker.position, userLocation)
                    // Abre a tela do armário apenas se a distância for menor ou igual a 1km (1000 metros)
                    if (distanciaParaArmario <= 1000) { // Distância limite em metros
                        val db = FirebaseFirestore.getInstance()
                        val armariosRef = db.collection("armarios")

                        // Consulta para buscar os dados do armário com o ID correspondente
                        val armarioId = if (marker.title == "LockerGuard - Jundiaí") "011" else "012"
                        armariosRef.whereEqualTo("id", armarioId)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    val documento = documents.first()
                                    val localizacao = documento.getString("localizacao")
                                    val id = documento.getString("id")

                                    // Criando Intent e passando os dados com o putExtra()
                                    val intent = Intent(this, ArmarioActivity::class.java)
                                    intent.putExtra("localizacao", localizacao)
                                    intent.putExtra("id", id)
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

    private fun calcularDistancia(segunda: LatLng, userLocation: Location): Float {
        val localizacaoSegunda = Location("")
        localizacaoSegunda.latitude = segunda.latitude
        localizacaoSegunda.longitude = segunda.longitude

        return userLocation.distanceTo(localizacaoSegunda)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1
    }
}