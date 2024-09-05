package com.example.ktprojectall.kotlin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.ktprojectall.R
import com.example.ktprojectall.kotlin.ui.Coord.CoordActivity

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_first)

        val toggleButton = findViewById<Button>(R.id.button1)
        toggleButton.setOnClickListener {

            startActivity(Intent(this, CoordActivity::class.java))
        }
    }


}