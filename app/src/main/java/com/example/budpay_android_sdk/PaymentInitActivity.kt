package com.example.budpay_sdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PaymentInitActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val TAG = "PaymentInitActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("email") ?: ""
        val amount = intent.getStringExtra("amount") ?: ""
        val currency = intent.getStringExtra("currency") ?: "NGN"
        val apiKey = intent.getStringExtra("api_key") ?: ""
        val successActivity = intent.getStringExtra("success_activity") ?: ""
        val failedActivity = intent.getStringExtra("failed_activity") ?: ""

//        Log.d(TAG, "Received email: $email, amount: $amount, currency: $currency, apiKey: $apiKey")

        if (email.isNotEmpty() && amount.isNotEmpty() && apiKey.isNotEmpty()) {
//            Log.d(TAG, "Initializing transaction...")
            initializeTransaction(email, amount, currency, apiKey, successActivity, failedActivity)
        } else {
//            Log.e(TAG, "Missing required parameters")
//            Toast.makeText(this, "Missing required parameters", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeTransaction(
        email: String,
        amount: String,
        currency: String,
        apiKey: String,
        successActivity: String,
        failedActivity: String
    ) {
        val callbackUrl = "https://yourapp.com/callback" // Optional: Replace with your actual callback URL
        val requestBody = createRequestBody(email, amount, callbackUrl, currency)

        val request = Request.Builder()
            .url("https://api.budpay.com/api/v2/transaction/initialize")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

//        Log.d(TAG, "Sending initialization request to the API...")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
//                Log.e(TAG, "Failed to initialize transaction: ${e.message}")
                runOnUiThread {
//                    Toast.makeText(this@PaymentInitActivity, "Failed to initialize transaction", Toast.LENGTH_SHORT).show()
                    launchActivity(failedActivity, "error: ${e.message}", apiKey)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
//                Log.d(TAG, "Transaction Initialization response: $body")

                runOnUiThread {
                    if (response.isSuccessful && body != null) {
                        try {
                            val json = JSONObject(body)
                            val data = json.getJSONObject("data")
                            val authUrl = data.getString("authorization_url")
                            val reference = data.getString("reference")

//                            Log.d(TAG, "Received authUrl: $authUrl, reference: $reference")
                            launchWebView(authUrl, reference, apiKey, successActivity, failedActivity)

                        } catch (e: Exception) {
//                            Log.e(TAG, "Error parsing response: ${e.message}")
//                            Toast.makeText(this@PaymentInitActivity, "Invalid response from server", Toast.LENGTH_SHORT).show()
                            launchActivity(failedActivity, "error: ${e.message}", apiKey)
                        }
                    } else {
//                        Log.e(TAG, "Transaction initialization failed with response: $body")
//                        Toast.makeText(this@PaymentInitActivity, "Transaction initialization failed", Toast.LENGTH_SHORT).show()
                        launchActivity(failedActivity, body ?: "{}", apiKey)
                    }
                }
            }
        })
    }

    private fun createRequestBody(email: String, amount: String, callbackUrl: String, currency: String): RequestBody {
        val json = JSONObject()
        json.put("email", email)
        json.put("amount", amount)
        json.put("callback", callbackUrl)
        json.put("currency", currency)

        return json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun launchWebView(
        authUrl: String,
        reference: String,
        apiKey: String,
        successActivity: String,
        failedActivity: String
    ) {
        val intent = Intent(this, PaymentWebViewActivity::class.java).apply {
            putExtra("auth_url", authUrl)
            putExtra("reference", reference)
            putExtra("api_key", apiKey)
            putExtra("success_activity", successActivity)
            putExtra("failed_activity", failedActivity)
        }
//        Log.d(TAG, "Launching WebView with authUrl: $authUrl")
        startActivity(intent)
        finish()
    }

    private fun launchActivity(activityName: String, payload: String, apiKey: String) {
        try {
//            Log.d(TAG, "Attempting to launch: $activityName")
            val targetClass = Class.forName(activityName)
            val intent = Intent(this, targetClass).apply {
                putExtra("response_payload", payload)
                putExtra("api_key", apiKey)
            }
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
//            Log.e(TAG, "Activity not found: $activityName", e)
//            Toast.makeText(this, "Activity not found: $activityName", Toast.LENGTH_LONG).show()
        }
    }

}
