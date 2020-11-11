package com.abroliza.openstreetmapexample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainActivity : AppCompatActivity()  {

    private val MULTIPLE_PERMISSION_REQUEST_CODE = 4
    private var mapView: MapView? = null
    private var mLastLocation: Location? = null
//    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolbar)
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build()
//        }
        //checkPermissionsState()
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        setupMap()
    }

    private fun checkPermissionsState() {
        val internetPermissionCheck: Int = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
        val networkStatePermissionCheck: Int = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
        val writeExternalStoragePermissionCheck: Int = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val coarseLocationPermissionCheck: Int = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationPermissionCheck: Int = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        val wifiStatePermissionCheck: Int = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE)
        if (internetPermissionCheck == PackageManager.PERMISSION_GRANTED && networkStatePermissionCheck == PackageManager.PERMISSION_GRANTED && writeExternalStoragePermissionCheck == PackageManager.PERMISSION_GRANTED && coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED && fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED && wifiStatePermissionCheck == PackageManager.PERMISSION_GRANTED) {
            setupMap()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE),
                    MULTIPLE_PERMISSION_REQUEST_CODE)
        }
    }

//    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>?, grantResults: IntArray) {
//        when (requestCode) {
//            MULTIPLE_PERMISSION_REQUEST_CODE -> {
//                if (grantResults.size > 0) {
//                    var somePermissionWasDenied = false
//                    for (result in grantResults) {
//                        if (result == PackageManager.PERMISSION_DENIED) {
//                            somePermissionWasDenied = true
//                        }
//                    }
//                    if (somePermissionWasDenied) {
//                        Toast.makeText(this, "Cant load maps without all the permissions granted", Toast.LENGTH_SHORT).show()
//                    } else {
//                        setupMap()
//                    }
//                } else {
//                    Toast.makeText(this, "Cant load maps without all the permissions granted", Toast.LENGTH_SHORT).show()
//                }
//                return
//            }
//        }
//    }

    private fun setupMap() {

        mapView = findViewById(R.id.mapview) as MapView
        mapView!!.isClickable = true
        mapView!!.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        //setContentView(mapView); //displaying the MapView
        //mapView!!.controller.setZoom(15) //set initial zoom-level, depends on your need
        mapView!!.controller.zoomTo(15,1) //set initial zoom-level, depends on your need
        //mapView.getController().setCenter(ONCATIVO);
        //mapView.setUseDataConnection(false); //keeps the mapView from loading online tiles using network connection.
        mapView!!.setTileSource(TileSourceFactory.MAPNIK)



        val mGpsMyLocationProvider = GpsMyLocationProvider(this)
        /*val mLocationOverlay = MyLocationNewOverlay(mGpsMyLocationProvider, mapView)
        mLocationOverlay.enableMyLocation()
        mLocationOverlay.enableFollowLocation()*/


        val oMapLocationOverlay = MyLocationNewOverlay(mGpsMyLocationProvider, mapView)
        mapView!!.overlays.add(oMapLocationOverlay)
        oMapLocationOverlay.enableMyLocation()
        oMapLocationOverlay.enableFollowLocation()

        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView!!.overlays.add(compassOverlay)

        val marker : Marker = Marker(mapView)
        marker.icon = resources.getDrawable(R.drawable.marker_default)
        marker.title = "My Location"
        mapView?.overlays?.add(marker)

        val pathOverlay = LatLonGridlineOverlay2()
        pathOverlay.setBackgroundColor(Color.BLACK)
        pathOverlay.setFontColor(Color.GREEN)
        pathOverlay.setLineColor(Color.GREEN)
       // mapView?.overlayManager?.add(pathOverlay)

        val startPoint = GeoPoint(24.416989, 54.607801)
        val endPoint = GeoPoint(24.453716, 54.388160)
        val line = Polyline()
        val roadManager: RoadManager = OSRMRoadManager(this)
        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(startPoint)
        waypoints.add(endPoint)


        

        oMapLocationOverlay.runOnFirstFix {
           // mapView!!.getOverlays().clear()
           // mapView!!.getOverlays().add(oMapLocationOverlay)
            runOnUiThread(object : Runnable{
                override fun run() {

                    mapView?.controller?.animateTo(GeoPoint(oMapLocationOverlay.myLocation.latitude, oMapLocationOverlay.myLocation.longitude))
                    //mapView?.controller?.animateTo(startPoint)
                }
            })
//            runOnUiThread { mapView!!.controller.animateTo(oMapLocationOverlay.myLocation) }
        }
        /*mapView!!.setMapListener(DelayedMapListener(object : MapListener {
            override fun onZoom(e: ZoomEvent): Boolean {
                val mapView = findViewById(R.id.mapview) as MapView
                val latitudeStr = "" + mapView.mapCenter.latitude
                val longitudeStr = "" + mapView.mapCenter.longitude
                val latitudeFormattedStr = latitudeStr.substring(0, Math.min(latitudeStr.length, 7))
                val longitudeFormattedStr = longitudeStr.substring(0, Math.min(longitudeStr.length, 7))
                Log.i("zoom", "" + mapView.mapCenter.latitude + ", " + mapView.mapCenter.longitude)
                val latLongTv = findViewById(R.id.textView) as TextView
                latLongTv.text = "$latitudeFormattedStr, $longitudeFormattedStr"
                return true
            }

            override fun onScroll(e: ScrollEvent): Boolean {
                val mapView = findViewById(R.id.mapview) as MapView
                val latitudeStr = "" + mapView.mapCenter.latitude
                val longitudeStr = "" + mapView.mapCenter.longitude
                val latitudeFormattedStr = latitudeStr.substring(0, Math.min(latitudeStr.length, 7))
                val longitudeFormattedStr = longitudeStr.substring(0, Math.min(longitudeStr.length, 7))
                Log.i("scroll", "" + mapView.mapCenter.latitude + ", " + mapView.mapCenter.longitude)
                val latLongTv = findViewById(R.id.textView) as TextView
                latLongTv.text = "$latitudeFormattedStr, $longitudeFormattedStr"
                return true
            }
        }, 1000))*/
        val thread = Thread(Runnable {
            val road: Road = roadManager.getRoad(waypoints)
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            mapView?.overlays?.add(roadOverlay)
            mapView?.invalidate()
        })
        val handler = Handler()
        handler.post {
            thread.start()
        }
    }

    private fun setCenterInMyCurrentLocation() {
        if (mLastLocation != null) {
            mapView!!.controller.setCenter(GeoPoint(mLastLocation!!.latitude, mLastLocation!!.longitude))
        } else {
            Toast.makeText(this, "Getting current location", Toast.LENGTH_SHORT).show()
        }
    }

//    fun onConnected(connectionHint: Bundle?) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
//            return
//        }
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
//    }

    fun onConnectionSuspended(i: Int) {}

//    fun onConnectionFailed(@NonNull connectionResult: ConnectionResult?) {}

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.itemId
//        if (id == R.id.action_settings) {
//            return true
//        } else if (id == R.id.action_locate) {
//            setCenterInMyCurrentLocation()
//        }
//        return super.onOptionsItemSelected(item)
//    }
}