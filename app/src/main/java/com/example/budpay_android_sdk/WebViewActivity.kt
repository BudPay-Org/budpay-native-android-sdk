package com.example.budpay_android_sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budpay_android_sdk.databinding.ActivityWebViewBinding
import com.example.budpay_sdk.FailedActivity
import com.example.budpay_sdk.SuccessActivity

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authorizationUrl = intent.getStringExtra("authorization_url") ?: return
        val apiKey = intent.getStringExtra("api_key")

        with(binding.webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()

                    if (url.contains("status=success", ignoreCase = true)) {
                        val uri = Uri.parse(url)
                        val reference = uri.getQueryParameter("reference")
                        val status = uri.getQueryParameter("status")
                        redirectToSuccess(reference, status)
                        return true
                    } else if (url.contains("status=failed", ignoreCase = true)) {
                        val uri = Uri.parse(url)
                        val reference = uri.getQueryParameter("reference")
                        val status = uri.getQueryParameter("status")
                        redirectToFailure(reference, status)
                        return true
                    }

                    return false
                }
            }

            loadUrl(authorizationUrl)
        }
    }

    private fun redirectToSuccess(reference: String?, status: String?) {
        val intent = Intent(this, SuccessActivity::class.java).apply {
            putExtra("response_payload", "Reference: $reference\nStatus: $status")
            putExtra("api_key", intent.getStringExtra("api_key")) // pass apiKey
        }
        startActivity(intent)
        finish()
        Toast.makeText(this, "Transaction was successful", Toast.LENGTH_LONG).show()
    }

    private fun redirectToFailure(reference: String?, status: String?) {
        val intent = Intent(this, FailedActivity::class.java).apply {
            putExtra("response_payload", "Reference: $reference\nStatus: $status")
            putExtra("api_key", intent.getStringExtra("api_key")) // pass apiKey
        }
        startActivity(intent)
        finish()
        Toast.makeText(this, "Transaction failed", Toast.LENGTH_LONG).show()
    }

}
