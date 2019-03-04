package com.example.tnaworks.canirecyclethis

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import android.util.Log
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val permissions = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.CAMERA"
        )

        val requestCode = 200
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }

        FirebaseApp.initializeApp(this)

        // Must call before super.onCreate
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonAccessLocalRecyclingInfo.setOnClickListener(fun(_: View) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.greenwasteofpaloalto.com/sites/greenwasteofpaloalto.com/files/2017%20Detailed%20Material%20Guide.pdf"))
            startActivity(i)
        })

        textViewAdvice.setText("")
        textViewRecyclability.setText("")

        dispatchTakePictureIntent()
    }

    override fun onDestroy() {
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        storageDir.deleteRecursively()
        super.onDestroy()
    }

    val REQUEST_TAKE_PHOTO = 1
    var currentPhotoPath: String = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            val bitmap: Bitmap = getBitmapFromPath(currentPhotoPath)

            setPic(bitmap)
            identifyImage(bitmap)
        }
    }


    /**
     * Save the photo to an external dir.
     * The photos will remain private to our app only, so don't need permissions for WRITE_EXTERNAL_STORAGE in the
     * manifest (if above SDK 18, which we are).
     *
     * Here we create a collision-resistant file name, and save the path to a member variable.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     *  invoke an intent to capture a photo.
     *
     *  the startActivityForResult() method is protected by a condition that calls resolveActivity(),
     *  which returns the first activity component that can handle the intent. Performing this check is
     *  important because if you call startActivityForResult() using an intent that no app can handle,
     *  your app will crash. So as long as the result is not null, it's safe to use the intent.
     *
     *  https://developer.android.com/training/camera/photobasics.html
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    ex.printStackTrace()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.tnaworks.canirecyclethis.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
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

        // Or, to set the minimum confidence required:
        // val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler()
        // val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
        //     .setConfidenceThreshold(0.7f)
        //     .build()
        // val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)

        val labeler = FirebaseVision.getInstance().getCloudImageLabeler()

        labeler.processImage(image)
            .addOnSuccessListener { labels ->
                if(labels.count() > 0) {
                // Task completed successfully

                val isReyclable = isImageRecyclable(labels)
                val yesNo = if (isReyclable) "Yes" else "No"
                textViewRecyclability.setText(yesNo)

                val objectLabel = whatObjectIsThis(labels)
                textViewTagline.setText("Can I recycle this " + objectLabel + "?")

                textViewAdvice.setText(recyclingAdvice(labels))

                // Display resulting labels on screen
                for (label in labels) {
                    val text = label.text
                    // val entityId = label.entityId
                    val confidence = Math.round(label.confidence * 100.0) / 100.0
                    textViewLabels.text = "${textViewLabels.text}$text: $confidence\n"
                }
                } else {
                    textViewTagline.setText("I have no idea what the fuck that is.")
                }

            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                e.printStackTrace()
                throw e
            }
    }

    private fun whatObjectIsThis (labels: List<FirebaseVisionImageLabel>): String {
        return labels.first().text
    }

    private fun isImageRecyclable (labels: List<FirebaseVisionImageLabel>): Boolean {
        val matcher = "(?i)(?<!\\p{L})(bottle|jar|can|cardboard|paper)(?!\\p{L})".toRegex()

        val isRecyclable: (FirebaseVisionImageLabel) -> Boolean = {
            it.text.matches(matcher)
        }

        return labels.any(isRecyclable)
    }


    private fun recyclingAdvice (labels: List<FirebaseVisionImageLabel>): String {
        val advice = arrayOf(
            Pair(Regex("(?i)(?<!\\p{L})cardboard(?!\\p{L})"),"Don't forget to break down your cardboard!"),
            Pair(Regex("(?i)(?<!\\p{L})light bulb(?!\\p{L})"),"Careful: You can throw out incandescent and halogen bulbs, but LED bulbs are hazardous household waste!")
        )

        val match = advice.firstOrNull { pair ->
            labels.any{ it.text.matches(pair.first)}
        }

        return match?.second ?: ""
    }
}
