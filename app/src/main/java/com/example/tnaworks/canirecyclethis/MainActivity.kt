package com.example.tnaworks.canirecyclethis

import android.content.Intent
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

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        anton_test_button.setOnClickListener { v ->
            dispatchTakePictureIntent()
        }
    }


    /**
     * Save the photo to an external dir.
     * The photos will remain private to our app only, so don't need permissions for WRITE_EXTERNAL_STORAGE in the
     * manifest (if above SDK 18, which we are).
     *
     * Here we create a collision-resistant file name, and save the path to a member variable.
     */
    var currentPhotoPath: String = ""

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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
    val REQUEST_TAKE_PHOTO = 1

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



}
