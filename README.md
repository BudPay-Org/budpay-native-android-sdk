# BudPay Android SDK

## Overview

The **BudPay Android SDK** allows seamless integration with the BudPay payment gateway. This SDK provides a simple and efficient way to handle payments by providing the necessary activities for initiating payments, verifying transactions, and handling payment success and failure scenarios.

The SDK supports the initialization of transactions, payment verification via WebView, and easy handling of success and failure responses. This README provides instructions on how to set up and use the SDK in your Android application.

## Requirements

- Android Studio with a recent version of Android SDK.
- **Min SDK**: Android 5.0 (Lollipop) or higher.
- **Gradle**: Make sure you are using a compatible Gradle version for your project.
# Installation

## Step 1: Add SDK to your project

Add the SDK module to your Android project:
- Import the `budpay_android_sdk` module into your project, or include it as a dependency in your `build.gradle` file.


Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Step 2: Add dependencies

Make sure the following dependencies are added in your `build.gradle` file:

```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.9.1' // For making network requests
    implementation 'androidx.appcompat:appcompat:1.3.1' // For AppCompatActivity support
    // Add other necessary dependencies
}
```

## Step 3: Set up the SDK

Once the SDK is added to your project, you can start using it by initiating the payment flow.

### Main Activities Overview

The SDK consists of several key components:

- **MainActivity**: The entry point to trigger the payment flow.
- **PaymentInitActivity**: The activity responsible for initializing the payment with the BudPay API.
- **PaymentWebViewActivity**: Displays a WebView where the user will be redirected to make payment and authorize the transaction.
- **SuccessActivity**: Displays when the payment is successful.
- **FailedActivity**: Displays when the payment fails.

## Usage

### Step 1: Initiate Payment

To start a payment transaction, use `MainActivity` to trigger the payment flow.

```kotlin
val email = "customer@example.com"
val amount = "10000"
val currency = "NGN"
val apiKey = "your_budpay_api_key"
val successActivity = "com.example.budpay_sdk.SuccessActivity"
val failedActivity = "com.example.budpay_sdk.FailedActivity"

val paymentIntent = Intent(this, PaymentInitActivity::class.java).apply {
    putExtra("email", email)
    putExtra("amount", amount)
    putExtra("currency", currency)
    putExtra("api_key", apiKey)
    putExtra("success_activity", successActivity)
    putExtra("failed_activity", failedActivity)
}

startActivity(paymentIntent)
```

## Step 2: Initialize Transaction

In `PaymentInitActivity`, the SDK communicates with the BudPay API to initiate the transaction.

```kotlin
private fun initializeTransaction(
    email: String, 
    amount: String, 
    currency: String, 
    apiKey: String, 
    successActivity: String, 
    failedActivity: String
) {
    val requestBody = createRequestBody(email, amount, "https://yourapp.com/callback", currency)
    val request = Request.Builder()
        .url("https://api.budpay.com/api/v2/transaction/initialize")
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread {
                launchActivity(failedActivity, "error: ${e.message}", apiKey)
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            runOnUiThread {
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val authUrl = json.getJSONObject("data").getString("authorization_url")
                    val reference = json.getJSONObject("data").getString("reference")
                    launchWebView(authUrl, reference, apiKey, successActivity, failedActivity)
                } else {
                    launchActivity(failedActivity, body ?: "{}", apiKey)
                }
            }
        }
    })
}
```

## Step 3: WebView for Payment Authorization

In `PaymentWebViewActivity`, the user will authorize the payment through the provided URL.

```kotlin
with(webView) {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            val reference = Uri.parse(url).getQueryParameter("reference")
            if (url.contains("status=success", ignoreCase = true)) {
                verifyTransaction(apiKey, reference, successActivity, failedActivity)
            } else if (url.contains("status=failed", ignoreCase = true)) {
                launchActivity(failedActivity, "Transaction failed", apiKey)
            }
            return true
        }
    }
    loadUrl(authUrl)
}
```

## Step 4: Verify Transaction

Once the payment is authorized, you can verify the transaction status.

```kotlin
private fun verifyTransaction(apiKey: String, reference: String, successActivity: String, failedActivity: String) {
    val url = "https://api.budpay.com/api/v2/transaction/verify/$reference"
    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $apiKey")
        .get()
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread {
                launchActivity(failedActivity, "Verification failed: ${e.message}", apiKey)
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody?.contains("success", true) == true) {
                launchActivity(successActivity, responseBody ?: "{}", apiKey)
            } else {
                launchActivity(failedActivity, responseBody ?: "{}", apiKey)
            }
        }
    })
}
```

## Success & Failure Handling

The SDK allows you to specify different activities to handle success and failure cases. These activities can display the transaction result as needed.

```kotlin
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

class FailedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val payload = intent.getStringExtra("response_payload") ?: "No details"
        val textView = TextView(this).apply {
            text = "Payment Failed!\n\n$payload"
        }
        setContentView(textView)
    }
}
```

## How to Integrate BudPay Android SDK Using JitPack

### Step 1: Add the JitPack Repository to Your Build File

In order to use the BudPay Android SDK, you'll need to add the JitPack repository to your project. Follow the steps below based on your build system.

#### For Gradle (Groovy)

Add the following to your `settings.gradle` file:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

## For Gradle (Kotlin DSL)

Add the following to your `settings.gradle.kts` file:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

## For Maven

In your `pom.xml`, add the following repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Step 2: Add the Dependency

Once the JitPack repository is added, you can now include the BudPay Android SDK dependency in your project.

### For Gradle

In your `build.gradle` file, add the following dependency:

```gradle
dependencies {
    implementation 'com.github.BudPay-Org:budpay-native-android-v1.0.0'
}
```

### For Maven

In your `pom.xml`, add the following dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.BudPay-Org</groupId>
        <artifactId>budpay-native-android-v1.0.0</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

