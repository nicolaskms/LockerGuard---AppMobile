package com.example.pi_time11

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
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

        buttonCadastrarCartao = findViewById(R.id.buttonCadastrarCartao)
        buttonSair = findViewById(R.id.buttonSair)

        auth = Firebase.auth

        buttonSair.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonCadastrarCartao.setOnClickListener {
            val intent = Intent(this, CartaoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

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
                .snippet("Av. Reitor Benedito José Barreto Fonseca," +
                        " Prédio H15 - Parque dos Jacarandás, Campinas")
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLng(jundiai))
    }

    // funcao que abre a tela do armario escolhido pelo marker do maps
    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.title == "LockerGuard - Jundiaí") {
            val db = FirebaseFirestore.getInstance()
            val armariosRef = db.collection("armarios")

            // Consulta para buscar os dados do armário com o ID "011" (Jundiaí)
            armariosRef.whereEqualTo("id", "011")
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
                            "Nenhum documento encontrado com o ID '011' (Jundiaí).",
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
            return true
        } else if (marker.title == "LockerGuard - Campinas") {
            val db = FirebaseFirestore.getInstance()
            val armariosRef = db.collection("armarios")

            // Consulta para buscar os dados do armário com o ID "012" (Campinas)
            armariosRef.whereEqualTo("id", "012")
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
                            "Nenhum documento encontrado com o ID '012' (Campinas).",
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
            return true
        }
        return false
    }
}