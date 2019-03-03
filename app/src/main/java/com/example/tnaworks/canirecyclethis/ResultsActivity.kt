package com.example.tnaworks.canirecyclethis

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import kotlinx.android.synthetic.main.activity_results.*
import java.io.IOException


class ResultsActivity : AppCompatActivity() {

    // Teddy's magic
    private var bundle: Bundle = Bundle.EMPTY
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        buttonAccessLocalRecyclingInfo.setOnClickListener(fun(_: View) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.averagepeopleokaystyle.com"))
            startActivity(i)
        })

        bundle = intent.extras!!
        currentPhotoPath = bundle.getString("currentPhotoPath")!!

        val bitmap: Bitmap = getBitmapFromPath(currentPhotoPath)

        setPic(bitmap)
        identifyImage(bitmap)

    }

    private fun getBitmapFromPath(path: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(path)
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

//        Log.i("Bitmap", "Orientation is " + orientation.toString())

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
            else -> {
            }
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true);
    }

    private fun setPic(bitmap: Bitmap) {
        imageViewSubject.setImageBitmap(bitmap)
    }

    private fun identifyImage(bitmap: Bitmap) {
        var image: FirebaseVisionImage
        try {
            image = FirebaseVisionImage.fromBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

        val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler()

        // Or, to set the minimum confidence required:
        // val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
        //     .setConfidenceThreshold(0.7f)
        //     .build()
        // val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)

        labeler.processImage(image)
            .addOnSuccessListener { labels ->
                // Task completed successfully
                for (label in labels) {
                    val text = label.text
                    // val entityId = label.entityId
                    val confidence = Math.round(label.confidence * 100.0) / 100.0
                    textViewRecyclability.text = (textViewRecyclability.text.toString() + " "
                            + text + ":" + confidence.toString() + ";\n")

                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                e.printStackTrace()
                throw e
            }
    }
}
