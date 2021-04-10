package com.example.myapplication1

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //ubicacion actual del cliente
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    val myDatabase = FirebaseDatabase.getInstance().getReference("Coordenadas")
    var markedName=""
    lateinit var locationClicked: LatLng

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
        // cargar ubicacion del usuario
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if(location != null){
                lastLocation=location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 4f))
            }
        }
//        val marcadores = arrayOf(LatLng(-184.0, 151.0), LatLng(-34.0, 151.0),  LatLng(-34.0, 152.0))
//        val titulos = arrayOf("1", "2", "3")
//        for(posicion in marcadores.indices ){
//            mMap.addMarker(MarkerOptions().position(marcadores[posicion]).title(titulos[posicion]))
//        }

       // Toast.makeText(this, "${myDatabase.key}", Toast.LENGTH_LONG).show()

        myDatabase.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                return
            }

            // GET VALUES FROM FIREBASE
            override fun onDataChange(snapshot: DataSnapshot) {
                val mapaCoordenadas = snapshot.getValue()
                Log.i("TAG2", "Got value ${mapaCoordenadas}")

                if(mapaCoordenadas is Map<*, *>) {
                    println("mapaCoordenadas is of type Map")
                   // print("-------------------------------------")
                   // print(mapaCoordenadas.size)
                    print("--------------FOR EACH-----------------------")

                    for ((k, v) in mapaCoordenadas) {
                        print("--------------FOR-----------------------")
                        println("$k = $v")
                        if(v is Map<*,*>){
                            var lat = v["latitude"]
                            var lng = v["longitude"]
                            mMap.addMarker(MarkerOptions().position(LatLng(lat as Double, lng as Double)).title(k as String?))
//                            mMap.addMarker(MarkerOptions().position(LatLng(10.33,-66.83)).title(k as String?))
                        }
                    }

//                    mapaCoordenadas.forEach {
//                        k, v ->
//                        println("$k = $v")
//                        print("--------------AQUIIIII-----------------------")
//                        print(v)
//                        mMap.addMarker(MarkerOptions().position(LatLng(10.33,-66.83)).title(k as String?))
//                    }



                }
            }
        })

    }

    private fun agregarMarcadores(){

        floatingButton.setOnClickListener(){
            //Dialogo para el titulo
            val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Ingrese el nombre de la ubicación")
            // input
            val input = EditText(this)
            // Specify the type of input expected
            //input.setHint("Ingrese el nombre de la ubicación")
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // botones
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                // Here you get get input text from the Edittext
                markedName = input.text.toString()
                //Marcador
                mMap.setOnMapClickListener(OnMapClickListener { point ->
                    //Toast.makeText(this, point.latitude.toString() + ", " + point.longitude, Toast.LENGTH_SHORT).show()
                    locationClicked = LatLng(point.latitude, point.longitude)
                    //Toast.makeText(this, locationClicked.toString(), Toast.LENGTH_SHORT).show()

                    //Crear marcador
                    mMap.addMarker(MarkerOptions().position(locationClicked).title(markedName))

                    mMap.setOnMapClickListener(null)


                    //mandar datos a firebase ESCRITURA
                    myDatabase.child(markedName).setValue(locationClicked).addOnCompleteListener{
                        Toast.makeText(this, "Marcador Guardado", Toast.LENGTH_LONG).show()
                    }

                })

            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()
        }
    }


}