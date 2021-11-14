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
        binding!!.resultBackButton.visibility = View.GONE
        postToken()
    }

    /**
     * Makes a POST request to the backend server with a tokenItem
     */
    fun postToken() {
        val json = JsonObject()
        json.addProperty("token", paramsModel.token)
        json.addProperty("challenge", paramsModel.challenge)

        Ion.getDefault(activity).conscryptMiddleware.enable(false)
        Ion.with(activity)
            .load(paramsModel.origin + paramsModel.authUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback { e, result ->
                    // do stuff with the result or error
                    if (result == null) {
                        // TODO: Set auth message failed and close the app
                        Log.i("Log thingy fail", "result was null")
                        if (args.mobile) {
                            createResponse(false)
                        } else {
                            requireActivity().finishAndRemoveTask()
                        }
                    } else {
                        Log.i("Log thingy success", result.toString())
                        if (args.mobile) {
                            createResponse(true, result.toString(), paramsModel.token)
                        } else {
                            requireActivity().finishAndRemoveTask()
                        }
                    }
                }
    }

    /**
     * Only used when the MobileAuthApp was launched by an app. Not for website use.
     */
    private fun createResponse(success: Boolean = true, result: String = "noResult", token: String = "noToken") {
        val responseCode = if (success) AppCompatActivity.RESULT_OK else AppCompatActivity.RESULT_CANCELED
        val resultIntent = Intent()
        resultIntent.putExtra("result", result)
        resultIntent.putExtra("token", token)
        requireActivity().setResult(responseCode, resultIntent)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}