package com.example.ktprojectall.kotlin.ui.janks

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.tracing.trace
import com.example.ktprojectall.R
import java.util.Random

class JanksActivity : AppCompatActivity() {
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_janks)
        textView = findViewById<TextView>(R.id.textView)
        textView.setOnClickListener { changeTextSize() }
        animateTextBackground()
    }

    override fun onResume() {
        super.onResume()
        setStatusText()
        setStatusTextColor()
    }

    private fun animateTextBackground() {
        // Some animation logic that animates background
        // of TextView continiously after some duration
    }

    private fun changeTextSize() = trace("changeTextSize"){
        pretendHeavyComputation()
        textView.textSize = Random().nextInt(60).toFloat()
    }

    private fun setStatusTextColor() {
        textView.setTextColor(getCurrentColor())
    }

    private fun setStatusText() {
        val text = getCurrentStatus()
        textView.text = text
    }

    private fun getCurrentStatus()= trace("getCurrentStatus")  {
        // This is very heavy task.
        // Just pretend that this is very very heavy!
        pretendHeavyComputation()
        "getCurrentStatus"
    }

    private fun getCurrentColor() = trace("getCurrentColor") {
        // This is very heavy task.
        // Just pretend that this is very very heavy!
        pretendHeavyComputation()
        R.color.purple_200
    }

    private fun pretendHeavyComputation() {
        Thread.sleep(500)
    }
}