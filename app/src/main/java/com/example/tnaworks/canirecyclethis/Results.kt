package com.example.tnaworks.canirecyclethis

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import kotlinx.android.synthetic.main.activity_results.*
import java.io.File
import java.io.IOException


class Results : AppCompatActivity() {

    // Teddy's magic
    private var bundle: Bundle = Bundle.EMPTY
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        buttonAccessLocalRecyclingInfo.setOnClickListener(fun(_: View) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.averagepeopleokaystyle.com"))
            startActivity(i)
        })

        bundle = intent.extras!!
        currentPhotoPath = bundle.getString("currentPhotoPath")!!

        setPic()
        identifyImage()
    }

    private fun setPic() {
        BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
                        imageViewSubject.setImageBitmap(bitmap)
        }
    }

    private fun identifyImage() {
        var image: FirebaseVisionImage
        try {
            image = FirebaseVisionImage.fromFilePath(this.applicationContext, Uri.fromFile(File(currentPhotoPath)))
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
