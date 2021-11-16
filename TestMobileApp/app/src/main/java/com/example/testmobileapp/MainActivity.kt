package com.example.testmobileapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.testmobileapp.databinding.ActivityMainBinding
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion

/**
 * Test mobile app to demonstrate how other applications can use MobileAuthApp.
 * Single purpose app that launches the MobileAuthApp and gets the response back (JWT).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var authLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { response ->
            if (response.resultCode == Activity.RESULT_OK) {
                // Currently we are not actually checking whether we get a valid token.
                // For testing purposes only, to make sure that we are able to get a response at all.
                binding.loginTextView.text = getString(R.string.auth_success)
            }
            if (response.resultCode == Activity.RESULT_CANCELED) {
                binding.loginTextView.text = getString(R.string.auth_failure)
            }
        }

        showLogin()

        binding.loginOptionNfcButton.setOnClickListener { getData() }

    }

    /**
     * Method that creates an intent to launch the MobileAuthApp
     */
    private fun launchAuth(challenge: String = "challenge", originUrl: String = "baseUrl", authUrl: String = "authUrl") {
        val launchIntent = Intent()
        launchIntent.setClassName("com.tarkvaraprojekt.mobileauthapp", "com.tarkvaraprojekt.mobileauthapp.MainActivity")
        launchIntent.putExtra("action", "auth")
        launchIntent.putExtra("challenge", challenge)
        launchIntent.putExtra("originUrl", originUrl)
        launchIntent.putExtra("authUrl", authUrl)
        launchIntent.putExtra("mobile", true)
        authLauncher.launch(launchIntent)
    }

    /**
     * Method for retrieving data from an endpoint.
     * Ion library is used as it is very convenient for making simple GET requests.
     */
    private fun getData() {
        // Enter the server endpoint address to here
        //val originUrl = "enter-base-url-here"
        val originUrl = "https://5d0c-85-253-195-195.ngrok.io"
        val url = "$originUrl/auth/challenge"
        Ion.getDefault(this).conscryptMiddleware.enable(false)
        Ion.with(applicationContext)
            .load(url)
            .asJsonObject()
            .setCallback { _, result ->
                try {
                    // Get data from the result and call launchAuth method
                    val challenge = result.asJsonObject["nonce"].toString().replace("\"", "")
                    Log.v("Challenge", challenge)
                    launchAuth(challenge, originUrl, "/auth/authentication")
                } catch (e: Exception) {
                    Log.i("GETrequest", "was unsuccessful")
                }
            }
    }

    private fun showLogin() {
        binding.loginOptions.visibility = View.VISIBLE
    }

    private fun showResult(resultObject: String, token: String) {
        binding.loginOptions.visibility = View.GONE
        binding.resultLayout.visibility = View.VISIBLE
        binding.resultObject.text = resultObject
        binding.resultToken.text = token
        binding.buttonForget.setOnClickListener {
            binding.resultObject.text = ""
            binding.resultToken.text = ""
            binding.resultLayout.visibility = View.GONE
            binding.loginOptions.visibility = View.VISIBLE
        }
    }
}