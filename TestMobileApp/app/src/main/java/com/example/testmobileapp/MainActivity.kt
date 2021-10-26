package com.example.testmobileapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.testmobileapp.databinding.ActivityMainBinding

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

    private fun launchAuth() {
        val launchIntent = Intent()
        launchIntent.setClassName("com.tarkvaraprojekt.mobileauthapp", "com.tarkvaraprojekt.mobileauthapp.MainActivity")
        launchIntent.putExtra("auth", true)
        authLauncher.launch(launchIntent)
    }
}