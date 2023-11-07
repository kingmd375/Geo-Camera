package com.example.geocamera.MainActivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.geocamera.MarkersApplication
import com.example.geocamera.Model.Marker
import com.example.geocamera.NewEditPicActivity.NewEditPicActivity
import com.example.geocamera.R
import com.example.geocamera.Util.LocationUtilCallback
import com.example.geocamera.Util.createLocationCallback
import com.example.geocamera.Util.createLocationRequest
import com.example.geocamera.Util.getLastLocation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.geocamera.Util.replaceFragmentInActivity
import com.example.geocamera.Util.stopLocationUpdates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity() {
    // create view model
    private val mapViewModel: MapViewModel by viewModels {
        MapViewModelFactory((application as MarkersApplication).repository)
    }

    // map stuff
    private lateinit var mapsFragment: OpenStreetMapFragment
    private var numMarkers: Int = 0

    // location stuff
    private var locationPermissionEnabled: Boolean = false
    private var locationRequestsEnabled: Boolean = false
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var mCurrentLocation: Location
    private lateinit var mLocationCallback: LocationCallback
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            //If successful, startLocationRequests
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }
            //If successful at coarse detail, we still want those
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                locationPermissionEnabled = true
                startLocationRequests()
            }

            else -> {
                //Otherwise, send toast saying location is not enabled
                locationPermissionEnabled = false
                Toast.makeText(this, "Location Not Enabled", Toast.LENGTH_LONG)
            }
        }
    }

    // picture stuff
    private var currentPhotoPath: String = ""
    private val newEditPicLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK)
        {
            Log.d("MainActivity", "result received")

            // get marker ID
            var picID = it.data?.getStringExtra("PIC_ID")?.toInt()
            if (picID == null) picID = -1

            if(picID == -1) {
                // create new marker
                // get data from new pic activity
                var picLoc = it.data?.getStringExtra("PIC_LOC")
                var date = it.data?.getStringExtra("DATE")
                var desc = it.data?.getStringExtra("DESC")
                if (picLoc == null) picLoc = "No path"
                if (date == null) date = "No date"
                if (desc == null) desc = ""

                // add new marker to map
                mapsFragment.addMarker(GeoPoint(mCurrentLocation), numMarkers)
                // add new marker to database
                mapViewModel.add(Marker(numMarkers, mCurrentLocation.toString(), picLoc, date, desc))

                // increment marker count
                numMarkers++
            }
            else {
                // update existing marker
                // only description is the only thing that can change
                var desc = it.data?.getStringExtra("DESC")
                if (desc == null) desc = ""

                // update database entry
                mapViewModel.updateDesc(picID, desc)
            }
        }
    }
    private val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED) {
            Log.d("MainActivity", "Take picture activity cancelled")
        } else {
            Log.d("MainActivity", "Picture taken")
            // launch new pic activity
            val newPicIntent = Intent(this@MainActivity, NewEditPicActivity::class.java)
            // pass in id = -1 to represent new marker
            newPicIntent.putExtra("PIC_ID", -1)
            // pass in image path
            newPicIntent.putExtra("PIC_LOC", currentPhotoPath)
            // pass in current date formatted nicely
            newPicIntent.putExtra(
                "DATE",
                SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())
            )
            Log.d("MainActivity", "Launching new pic activity")
            newEditPicLauncher.launch(newPicIntent)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // fab click listener
        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener{
            // take photo, which will launch activity
            takeNewPhoto()
        }

        //Get preferences for tile cache
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        //Get location provider
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //Get last known location
        getLastLocation(this, locationProviderClient, locationUtilCallback)

        //Get access to mapsFragment object
        mapsFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as OpenStreetMapFragment? ?:OpenStreetMapFragment.newInstance(this).also{
            replaceFragmentInActivity(it, R.id.fragmentContainerView)
        }
    }

    override fun onStart() {
        super.onStart()
        //Start location updates
        startLocationRequests()
    }

    override fun onStop() {
        super.onStop()
        //if we are currently getting updates
        if (locationRequestsEnabled) {
            //stop getting updates
            locationRequestsEnabled = false
            stopLocationUpdates(locationProviderClient, mLocationCallback)
        }
    }

    public fun markerClicked(markerId: Int) {

        val editPicIntent = Intent(this@MainActivity, NewEditPicActivity::class.java)
        // get data for marker clicked
        val marker = mapViewModel.getMarker(markerId)
        editPicIntent.putExtra("PIC_LOC", marker.markerImagePath)
        editPicIntent.putExtra("DATE", marker.markerDate)
        editPicIntent.putExtra("DESC", marker.markerDescription)

        // start new edit marker activity
        newEditPicLauncher.launch(editPicIntent)
    }



    // Camera functions

    private fun takeNewPhoto(){
        // start camera app
        val picIntent = Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if (picIntent.resolveActivity(packageManager) != null)
        {
            val filePath: String = createFilePath()
            val myFile = File(filePath)
            currentPhotoPath = filePath
            val photoUri = FileProvider.getUriForFile(this, "com.example.geocamera.fileprovider", myFile)
            picIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureResultLauncher.launch(picIntent)
        }
    }

    private fun createFilePath(): String {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        return image.absolutePath
    }



    // Location functions

    //LocationUtilCallback object
    //Dynamically defining two results from locationUtils
    //Namely requestPermissions and locationUpdated
    private val locationUtilCallback = object : LocationUtilCallback {
        //If locationUtil request fails because of permission issues
        //Ask for permissions
        override fun requestPermissionCallback() {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        //If locationUtil returns a Location object
        //Populate the current location and log
        override fun locationUpdatedCallback(location: Location) {
            mCurrentLocation = location
            Log.d(
                "MainActivity",
                "Location is [Lat: ${location.latitude}, Long: ${location.longitude}]"
            )
        }
    }

    private fun startLocationRequests() {
        //If we aren't currently getting location updates
        if (!locationRequestsEnabled) {
            //create a location callback
            mLocationCallback = createLocationCallback(locationUtilCallback)
            //and request location updates, setting the boolean equal to whether this was successful
            locationRequestsEnabled =
                createLocationRequest(this, locationProviderClient, mLocationCallback)
        }
    }
}