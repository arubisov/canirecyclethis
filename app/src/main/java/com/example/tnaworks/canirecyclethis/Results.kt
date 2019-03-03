package com.example.tnaworks.canirecyclethis

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_results.*


class Results : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        buttonAccessLocalRecyclingInfo.setOnClickListener(fun(_: View) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.averagepeopleokaystyle.com"))
            startActivity(i)
        })

        setPic()

        val bundle = intent.extras
        val objectMatches = bundle.getParcelableArray("objectMatches") as Array<Pair<String,Double>>

        for (match in objectMatches) {
            var objType = match.first
            var probability = match.second
            textViewRecyclability.setText(textViewRecyclability.text.toString() + " " + objType + ":" + probability.toString() + ";\n")
        }
    }

    private fun setPic() {
        // Teddy's magic
        val bundle = intent.extras
        val currentPhotoPath = bundle.getString("currentPhotoPath")
        BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
                        imageViewSubject.setImageBitmap(bitmap)
        }
    }


}
