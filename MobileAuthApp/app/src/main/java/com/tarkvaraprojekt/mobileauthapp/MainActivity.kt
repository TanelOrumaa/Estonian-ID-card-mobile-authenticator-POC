package com.tarkvaraprojekt.mobileauthapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.tarkvaraprojekt.mobileauthapp.databinding.ActivityMainBinding


/**
 * The only activity of the application (single activity design).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navigationController: NavController

    // If true the settings menu can be accessed from the toolbar in the upper part of the screen.
    var menuAvailable: Boolean = true

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
        R.id.menu_settings_option -> {
            if (menuAvailable) {
                navigationController.navigate(R.id.action_homeFragment_to_settingsFragment)
                true
            } else {
                Toast.makeText(this, getString(R.string.unavailable), Toast.LENGTH_SHORT).show()
                false
            }
        }
        else -> super.onOptionsItemSelected(item)
    }
}