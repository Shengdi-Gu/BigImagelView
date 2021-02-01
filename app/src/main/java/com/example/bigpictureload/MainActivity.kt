package com.example.bigpictureload

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            var open = assets.open("aaa.png")
            biv_main.setImage(open)
        } catch (e: Exception) {
        }
    }
}