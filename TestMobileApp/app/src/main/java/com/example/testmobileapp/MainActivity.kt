package com.example.testmobileapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.testmobileapp.databinding.ActivityMainBinding
import com.koushikdutta.ion.Ion

/**
 * Test mobile app to demonstrate how other applications can use MobileAuthApp.
 * Single purpose app that launches the MobileAuthApp and gets the response back (JWT).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var authLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
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

        binding.loginOptionNfcButton.setOnClickListener { launchAuth() }

    }

    /**
     * Method that creates an intent to launch the MobileAuthApp
     */
    private fun launchAuth(arg: String = "nothing") {
        val launchIntent = Intent()
        launchIntent.setClassName("com.tarkvaraprojekt.mobileauthapp", "com.tarkvaraprojekt.mobileauthapp.MainActivity")
        launchIntent.putExtra("auth", true)
        launchIntent.putExtra("nonce", arg) // Currently nothing
        authLauncher.launch(launchIntent)
    }

    /**
     * Method for retrieving data from an endpoint.
     * Ion library is used as it is very convenient for making simple GET requests.
     */
    private fun getData() {
        val url = "real-address-here"
        Ion.with(applicationContext)
            .load(url)
            .asJsonObject()
            .setCallback { _, result ->
                try {
                    // Get data from the result and call launchAuth method
                    launchAuth()
                } catch (e: Exception) {
                    Log.i("GETrequest", "was unsuccessful")
                }
            }
    }
}