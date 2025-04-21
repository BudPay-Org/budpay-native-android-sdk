package com.example.budpay_sdk

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val payload = intent.getStringExtra("response_payload") ?: "No details"
        val textView = TextView(this).apply {
            text = "Payment Successful!\n\n$payload"
        }

        setContentView(textView)
    }
}
