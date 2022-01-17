package com.tarkvaraprojekt.mobileauthapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import com.tarkvaraprojekt.mobileauthapp.databinding.ActivityMainBinding
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentResultBinding
import com.tarkvaraprojekt.mobileauthapp.model.ParametersViewModel


/**
 * The only activity of the application (single activity design).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navigationController: NavController
    private val paramsModel: ParametersViewModel by viewModels()


    // If true the settings menu can be accessed from the toolbar in the upper part of the screen.
    var menuAvailable: Boolean = true

    var inMenu: Boolean = false

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
                menuAvailable = false
                inMenu = true
                true
            } else {
                if (!inMenu) {
                    Toast.makeText(this, getString(R.string.menu_unavailable_message), Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun returnError(errorCode: Int) {
        val json = JsonObject()
        json.addProperty("auth-token", "")
        json.addProperty("error", errorCode)

        Ion.getDefault(this).conscryptMiddleware.enable(false)
        val ion = Ion.with(this)
            .load(paramsModel.authUrl)
        for ((header, value) in paramsModel.headers) {
            ion.setHeader(header, value)
        }

        ion
            .setJsonObjectBody(json)
            .asJsonObject()
            .setCallback { _, _ ->

            }
    }
}