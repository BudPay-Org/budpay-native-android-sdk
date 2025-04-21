package com.example.budpay_sdk

import android.content.Context
import android.content.Intent

object BudPaySDK {

    fun initializePayment(
        context: Context,
        email: String,
        amount: String,
        apiKey: String,
        currency: String = "NGN",
        callbackUrl: String = "https://your-callback-url.com",
        successActivity: Class<*>,
        failedActivity: Class<*>
    ) {
        val intent = Intent(context, PaymentWebViewActivity::class.java).apply {
            putExtra("email", email)
            putExtra("amount", amount)
            putExtra("api_key", apiKey)
            putExtra("currency", currency)
            putExtra("callback_url", callbackUrl)
            putExtra("success_activity", successActivity.name)
            putExtra("failed_activity", failedActivity.name)
        }
        context.startActivity(intent)
    }
}
