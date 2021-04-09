package com.example.myapplication1

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //ubicacion actual del cliente
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private val marcadorTitulo = ""

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker?): Boolean = false

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        title="Mapa"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener(this)
        mMap.uiSettings.isZoomControlsEnabled = true

        setUpMap()
        agregarMarcadores()

    }


    //localizacion de usuario
    private fun setUpMap(){
     if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
         return
     }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if(location != null){
                lastLocation=location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 13f))
            }
        }
    }

    private fun agregarMarcadores(){
        // Add a marker in Sydney and move the camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        floatingButton.setOnClickListener(){

            //Dialogo para el titulo
            val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Ingrese el nombre de la ubicación")
            // input
            val input = EditText(this)
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            //input.setHint("Ingrese el nombre de la ubicación")
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // botones
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                // Here you get get input text from the Edittext
                var markedName = input.text.toString()
                //Marcador
                mMap.setOnMapClickListener(OnMapClickListener { point ->
                    //Toast.makeText(this, point.latitude.toString() + ", " + point.longitude, Toast.LENGTH_SHORT).show()
                    val locationClicked = LatLng(point.latitude, point.longitude)
                    //Toast.makeText(this, locationClicked.toString(), Toast.LENGTH_SHORT).show()

                    //Crear marcador
                    mMap.addMarker(MarkerOptions().position(locationClicked).title(markedName))
//                mMap.addMarker(MarkerOptions().position(LatLng(Location)).title("Probando"))

                    mMap.setOnMapClickListener(null)
                })
                
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()


        }



    }




}