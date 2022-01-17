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
import org.json.JSONObject

/**
 * Base url where the requests should be made. Add yours here. It must use https.
 */
private const val BASE_URL = "https://a0fe-2001-7d0-88ab-b880-7571-cba0-5db2-11b7.ngrok.io"
private const val AUTH_URL = "$BASE_URL/auth/login"
private const val CHALLENGE_URL = "$BASE_URL/auth/challenge"

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
                binding.loginTextView.text = getString(R.string.auth_success)
                // Logs are used to show what information can be retrieved from the mobileauthapp.
                Log.i("getResult", response.data?.getStringExtra("idCode").toString())
                Log.i("getResult", response.data?.getStringExtra("name").toString())
                Log.i("getResult", response.data?.getStringExtra("authority").toString())
                var user = ""
                try {
                    user = response.data?.getStringExtra("name").toString()
                } catch (e: Exception) {
                    Log.i("getResult", "unable to retrieve name")
                }
                showResult(user)
            }
            if (response.resultCode == Activity.RESULT_CANCELED) {
                binding.loginTextView.text = getString(R.string.auth_failure)
            }
        }

        showLogin()

        binding.loginOptionNfcButton.setOnClickListener {
            //getData()
            launchAuth()
        }

    }

    /**
     * Method that creates an intent to launch the MobileAuthApp
     */
    private fun launchAuth() {
        val launchIntent = Intent()
        launchIntent.setClassName("com.tarkvaraprojekt.mobileauthapp", "com.tarkvaraprojekt.mobileauthapp.MainActivity")
        launchIntent.putExtra("action", "auth")
        launchIntent.putExtra("challenge", CHALLENGE_URL)
        launchIntent.putExtra("originUrl", BASE_URL)
        launchIntent.putExtra("authUrl", AUTH_URL)
        launchIntent.putExtra("headers","${(0..100000).random()}")
        launchIntent.putExtra("mobile", true)
        authLauncher.launch(launchIntent)
    }

    /**
     * Method for retrieving data from an endpoint.
     * Ion library is used as it is very convenient for making simple GET requests.
     */
    /*
    private fun getData() {
        // Enter the server endpoint address to here
        val url = "$BASE_URL/auth/challenge"
        Ion.getDefault(this).conscryptMiddleware.enable(false)
        Ion.with(applicationContext)
            .load(url)
            .asJsonObject()
            .setCallback { _, result ->
                try {
                    // Get data from the result and call launchAuth method
                    val challenge = result.asJsonObject["nonce"].toString().replace("\"", "")
                    Log.v("Challenge", challenge)
                    launchAuth(challenge, BASE_URL, "/auth/authentication")
                } catch (e: Exception) {
                    Log.i("GETrequest", "was unsuccessful")
                }
            }
    }
     */

    private fun showLogin() {
        binding.loginOptions.visibility = View.VISIBLE
    }

    private fun showResult(user: String) {
        binding.loginOptions.visibility = View.GONE
        binding.resultLayout.visibility = View.VISIBLE
        binding.resultObject.text = getString(R.string.hello, user)
        binding.buttonForget.setOnClickListener {
            binding.loginTextView.text = getString(R.string.login_text)
            binding.resultObject.text = ""
            binding.resultLayout.visibility = View.GONE
            binding.loginOptions.visibility = View.VISIBLE
        }
    }
}