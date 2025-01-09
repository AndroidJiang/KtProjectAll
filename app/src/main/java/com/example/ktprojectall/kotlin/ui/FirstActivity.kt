package com.example.ktprojectall.kotlin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.ktprojectall.R
import com.example.ktprojectall.kotlin.ui.coord.CoordActivity
import com.example.ktprojectall.kotlin.ui.janks.JanksActivity
import com.example.ktprojectall.kotlin.ui.menu.CircleLayoutActivity
import com.example.ktprojectall.kotlin.ui.scrollconflict.ScrollingActivity


class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_first)

        val toggleButton = findViewById<Button>(R.id.button1)
        val toggleButton2 = findViewById<Button>(R.id.button2)
        val toggleButton3 = findViewById<Button>(R.id.button3)
        val toggleButton4 = findViewById<Button>(R.id.button4)
        toggleButton.setOnClickListener {

            startActivity(Intent(this, CoordActivity::class.java))

            // 创建一个用于分享文本的 Intent
//            val shareTextIntent = Intent(Intent.ACTION_SEND)
//            shareTextIntent.type = "text/plain"
//            shareTextIntent.putExtra(Intent.EXTRA_TEXT, "这是要分享的文本")
//
//
//// 创建一个用于分享图片的 Intent
//            val shareImageIntent = Intent(Intent.ACTION_SEND)
//            shareImageIntent.type = "image/*"
//            val imageUri = Uri.parse("content://path/to/image")
//            shareImageIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
//
//// 显示选择器并启动分享
//            startActivity(Intent.createChooser(shareImageIntent, "分享图片"))
        }
        toggleButton2.setOnClickListener {             startActivity(Intent(this, CircleLayoutActivity::class.java))
        }


        toggleButton3.setOnClickListener {
            startActivity(Intent(this, JanksActivity::class.java))
        }
        toggleButton4.setOnClickListener {
            startActivity(Intent(this, ScrollingActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e("FirstActivity", "first-onPause")
    }

}