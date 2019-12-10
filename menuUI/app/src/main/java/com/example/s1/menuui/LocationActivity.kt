package com.example.s1.menuui

import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.text.PrecomputedText
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.type.LatLng
import kotlinx.android.synthetic.main.activity_find_location.*


class LocationActivity: AppCompatActivity(), OnMapReadyCallback {
//    private var mapView : MapView? = null
    private var gmap : GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val TAG = "BLUEMING"
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_location)
        setSupportActionBar(toolbar)
        val sb = supportActionBar!!
        sb.setDisplayShowTitleEnabled(false)
        sb.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        var mapViewBundle : Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        //register button
        register_address_btn.setOnClickListener {
            Log.d(TAG, "register click in")
            Log.d(TAG, "Edit Text : " + additional_address_edit_tv.text)
            if (additional_address_edit_tv.text == null) {
                Log.d(TAG, "Edit text is null")
                AlertDialog.Builder(this)
                    .setMessage(R.string.missing_address)
                    .setPositiveButton("OK", null).create().show()
            }
            else {
                Log.d(TAG, "Edit text is not null")
                var address = current_location_tv.text.toString() + " " + additional_address_edit_tv.text
                Log.d(TAG, "Final Address : " + address)
                var intent = Intent()
                intent.putExtra("address", address)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override protected fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override protected fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override protected fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override protected fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override protected fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(p0: GoogleMap?) {
//        val seoul = com.google.android.gms.maps.model.LatLng(37.566, 126.978)
        val seoul = com.google.android.gms.maps.model.LatLng(40.758899, -73.987325 )
        val marker = MarkerOptions()
//        val marker = MarkerOptions().position(seoul).title("Current Location")
        var currentLocation : com.google.android.gms.maps.model.LatLng = seoul
//        val locationManager = this.getSystemService(Context.LOCATION_SERVICE)
        Log.d(TAG, "OnMapReady in!")
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                Log.d(TAG, "Success")
                currentLocation = com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                marker.position(currentLocation).snippet("YOU!").title("Current Location")
                gmap = p0
                gmap?.setMinZoomPreference(16.5F)
                gmap?.addMarker(marker)
//                setCurrentAddress(it.latitude, it.longitude)
                GetAddressTask().execute(it.latitude, it.longitude)
                gmap?.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed")
                AlertDialog.Builder(this)
                    .setMessage(it.toString())
                    .setPositiveButton("OK", null).create().show()
                finish()
            }

//        marker.position(currentLocation).snippet("YOU!").title("Current Location")
//        gmap = p0
//        gmap?.setMinZoomPreference(12F)
//        var latLng = com.google.android.gms.maps.model.LatLng(40.71423, -74.00597)
//        gmap?.addMarker(marker)
//        gmap?.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class GetAddressTask : AsyncTask<Double, Void, String>() {
        override fun doInBackground(vararg params: Double?): String {
            val geocoder = Geocoder(baseContext)

            if (geocoder == null)
                Log.d(TAG, "GeoCoder is null")
            else
                Log.d(TAG, "Geocoder is not null")

            Log.d(TAG, "latitude : " + params[0]!! + ", longitude : " + params[1]!!)
            val currentAddress = geocoder.getFromLocation(params[0]!!, params[1]!!, 1)
            /*
            The returned values may be obtained by means of a network lookup.
            The results are a best guess and are not guaranteed to be meaningful or correct.
            It may be useful to call this method from a thread separate from your primary UI thread.
             */
            /*
            Use AsyncTask to fetch coordinates from server using geocoder.
            For example, getFromLocationName() should be called using AsyncTask.
            UI thread (main activity) does not allow the tasks which take too much time, hence the method returns empty list.
             */

            return currentAddress[0].getAddressLine(0)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            current_location_tv.text = result
        }
    }
}