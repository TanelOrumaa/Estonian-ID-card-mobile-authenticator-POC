package com.tarkvaraprojekt.mobileauthapp

import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.tarkvaraprojekt.mobileauthapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navigationController = navHostFragment.navController
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_language_option -> {
            Toast.makeText(this, getString(R.string.menu_action_unavailable), Toast.LENGTH_SHORT)
                .show()
            true
        }
        R.id.menu_settings_option -> {
            Toast.makeText(this, getString(R.string.menu_action_unavailable), Toast.LENGTH_SHORT)
                .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}