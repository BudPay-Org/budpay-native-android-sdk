package com.example.budpay_sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class PaymentWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val client = OkHttpClient()
    private val TAG = "PaymentWebView"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate called")

        webView = WebView(this)
        setContentView(webView)

        val authUrl = intent.getStringExtra("auth_url")
        val apiKey = intent.getStringExtra("api_key")
        val successActivity = intent.getStringExtra("success_activity")
        val failedActivity = intent.getStringExtra("failed_activity")

        with(webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
//                    Log.d(TAG, "Intercepted URL: $url")

                    val uri = Uri.parse(url)
                    val reference = uri.getQueryParameter("reference")
//                    val status = uri.getQueryParameter("status")

                    when {
                        url.contains("status=success", ignoreCase = true) -> {
                            if (reference != null && apiKey != null && successActivity != null && failedActivity != null) {
//                                Log.d(TAG, "Transaction success detected. Reference: $reference")
                                verifyTransaction(apiKey, reference, successActivity, failedActivity)
                            }
                            return true
                        }
                        url.contains("status=failed", ignoreCase = true) -> {
                            if (reference != null && failedActivity != null) {
//                                Log.d(TAG, "Transaction failure detected. Reference: $reference")
                                launchActivity(failedActivity, "Transaction failed", apiKey ?: "")
                            }
                            return true
                        }
                        else -> return false
                    }
                }
            }

            if (authUrl != null) {
//                Log.d(TAG, "Loading WebView with URL: $authUrl")
                loadUrl(authUrl)
            }
        }
    }

    private fun verifyTransaction(apiKey: String, reference: String, successActivity: String, failedActivity: String) {
        val url = "https://api.budpay.com/api/v2/transaction/verify/:$reference"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()

//        Log.d(TAG, "Sending verification request to: $url")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
//                Log.e(TAG, "Verification failed: ${e.message}")
                runOnUiThread {
//                    Toast.makeText(this@PaymentWebViewActivity, "Verification failed", Toast.LENGTH_SHORT).show()
                    launchActivity(failedActivity, "error: ${e.message}", apiKey)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val isSuccess = response.isSuccessful && responseBody?.contains("success", ignoreCase = true) == true

//                Log.d(TAG, "Verification response: ${response.code} | Body: $responseBody")
//                Log.d(TAG, "Transaction success: $isSuccess")

                runOnUiThread {
                    if (isSuccess) {
                        launchActivity(successActivity, responseBody ?: "{}", apiKey)
                    } else {
                        launchActivity(failedActivity, responseBody ?: "{}", apiKey)
                    }
                }
            }
        })
    }

    private fun launchActivity(activityName: String, payload: String, apiKey: String) {
//        Log.d(TAG, "Attempting to launch activity: $activityName")
        try {
            val targetClass = Class.forName(activityName)
            val intent = Intent(this, targetClass).apply {
                putExtra("response_payload", payload)
                putExtra("api_key", apiKey)
            }
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
//            Log.e(TAG, "Activity not found: $activityName")
//            Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show()
        }
    }
}
