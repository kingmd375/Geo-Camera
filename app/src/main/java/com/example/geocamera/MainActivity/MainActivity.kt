package com.example.geocamera.MainActivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.geocamera.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.geocamera.Util.replaceFragmentInActivity

class MainActivity : AppCompatActivity() {
    var currentPhotoPath: String = ""

    val takePictureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_CANCELED) {
            Log.d("MainActivity", "Take picture activity cancelled")
        } else {
            Log.d("MainActivity", "Picture taken")
            setPic()
        }
    }
    private lateinit var mapsFragment: OpenStreetMapFragment

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MapsActivity","Permission Granted")
            } else {
                Toast.makeText(this,"Location Permissions not granted. Location disabled on map",
                    Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener{
            takeNewPhoto()
        }

        //Get preferences for tile cache
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        //Check for location permissions
        checkForLocationPermission()

        //Get access to mapsFragment object
        mapsFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as OpenStreetMapFragment? ?:OpenStreetMapFragment.newInstance().also{
            replaceFragmentInActivity(it, R.id.fragmentContainerView)
        }
    }

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

    private  fun setPic() {
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath)

    }

    private fun checkForLocationPermission(){
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                //getLastKnownLocation()
                //registerLocationUpdateCallbacks()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}