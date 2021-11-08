package com.tarkvaraprojekt.mobileauthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentResultBinding
import com.tarkvaraprojekt.mobileauthapp.model.ParametersViewModel
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel
import com.tarkvaraprojekt.mobileauthapp.network.BASE_URL
import com.tarkvaraprojekt.mobileauthapp.network.TokenApi
import com.tarkvaraprojekt.mobileauthapp.network.TokenApiService
import com.tarkvaraprojekt.mobileauthapp.network.TokenItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

/**
 * ResultFragment is used to create a JWT and to send response to the website/application
 * that launched the MobileAuthApp. If the mobile auth app was started by a website
 * the result is sent to a server with a POST request.
 */
class ResultFragment : Fragment() {

    private val paramsModel: ParametersViewModel by activityViewModels()

    private var binding: FragmentResultBinding? = null

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.resultBackButton.setOnClickListener {
//            if (args.mobile) {
//                createResponse()
//            }
            postToken()
        }
    }

    /**
     * Makes a POST request to the backend server with a tokenItem
     */
    fun postToken() {
        val json = JsonObject()
        json.addProperty("token", paramsModel.token)
        json.addProperty("challenge", paramsModel.challenge)

        Ion.getDefault(activity).getConscryptMiddleware().enable(false)

        Ion.with(activity)
            .load("https://6bb0-85-253-195-252.ngrok.io/auth/authentication")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback { e, result ->
                    // do stuff with the result or error
                    Log.i("Log thingy", result.toString())
                }
//        CoroutineScope(Dispatchers.Default).launch {
//            val response = TokenApi.retrofitService.postToken(jsonBody)
//            Log.v("Response", response.message())
//            if (response.isSuccessful) {
//                //Success scenario here
//            } else {
//                //Failure scenario here
//                if (args.mobile) {
//                    createResponse(false)
//                } else {
//                    //Currently for some reason the activity is not killed entirely. Must be looked into further.
//                    requireActivity().finish()
//                    exitProcess(0)
//                }
//            }
//        }
    }

    /**
     * Only used when the MobileAuthApp was launched by an app. Not for website use.
     */
    private fun createResponse(success: Boolean = true) {
        val responseCode = if (success) AppCompatActivity.RESULT_OK else AppCompatActivity.RESULT_CANCELED
        val resultIntent = Intent()
        requireActivity().setResult(responseCode, resultIntent)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}