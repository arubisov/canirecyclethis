package com.example.tnaworks.canirecyclethis

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_results.*

class Results : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        buttonAccessLocalRecyclingInfo.setOnClickListener(fun(_: View) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.averagepeopleokstyle.com"))
            startActivity(i)
        })
    }
}
