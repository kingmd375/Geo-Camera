package com.example.geocamera.NewEditPicActivity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geocamera.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// receives pic id, timestamp allows description to be made, returns description.
class NewEditPicActivity : AppCompatActivity() {
    var picLoc: String = ""
    var desc: String = ""
    lateinit var imageView: ImageView
    lateinit var dateText: TextView
    lateinit var descText: EditText
    lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_edit_pic)
        Log.d("NewEditPic", "started activity")
        imageView = findViewById(R.id.imageView)
        dateText = findViewById(R.id.dateText)
        descText = findViewById(R.id.descText)
        saveButton = findViewById(R.id.button)

        // get/set image and date
        picLoc = intent.getStringExtra("PIC_LOC").toString()
        dateText.text = intent.getStringExtra("DATE")

        // set onClick listener of button
        saveButton.setOnClickListener {
            val retIntent = Intent()
            retIntent.putExtra("PIC_LOC", picLoc)
            retIntent.putExtra("DESCRIPTION", desc)
            setResult(RESULT_OK, retIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                Thread.sleep(200)
                withContext(Dispatchers.Main){
                    setPic()
                }
            }
        }
    }

    private fun setPic() {
        val targetW: Int = imageView.getWidth()

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(picLoc, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val photoRatio:Double = (photoH.toDouble())/(photoW.toDouble())
        val targetH: Int = (targetW * photoRatio).roundToInt()
        // Determine how much to scale down the image
        val scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))


        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val bitmap = BitmapFactory.decodeFile(picLoc, bmOptions)
        imageView.setImageBitmap(bitmap)
    }
}