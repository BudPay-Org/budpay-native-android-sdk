package com.example.budpay_android_sdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.budpay_sdk.PaymentInitActivity
import com.example.budpay_android_sdk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "BudPayMain"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing MainActivity")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val paymentButton: Button = findViewById(R.id.paymentbutton)
            paymentButton.setOnClickListener {
                Log.d(TAG, "onCreate: Pay Now button clicked")
                initiatePayment()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding or attaching button: ${e.message}", e)
        }
    }

    private fun initiatePayment() {
        Log.d(TAG, "initiatePayment: Preparing payment intent")

        val email = "graytillerman@gmail.com"
        val amount = "78000"
        val currency = "NGN"
        val apiKey = "sk_test_j58aervhw5xucmj6mstflbe79dspzopj8frf4ta"
        val successActivity = "com.example.budpay_sdk.SuccessActivity"
        val failedActivity = "com.example.budpay_sdk.FailedActivity"

        try {
            val paymentIntent = Intent(this, PaymentInitActivity::class.java).apply {
                putExtra("email", email)
                putExtra("amount", amount)
                putExtra("currency", currency)
                putExtra("api_key", apiKey)
                putExtra("success_activity", successActivity)
                putExtra("failed_activity", failedActivity)
            }

            Log.d(TAG, "initiatePayment: Starting PaymentInitActivity")
            startActivity(paymentIntent)
        } catch (e: Exception) {
            Log.e(TAG, "initiatePayment: Failed to start PaymentInitActivity - ${e.message}", e)
        }
    }
}
