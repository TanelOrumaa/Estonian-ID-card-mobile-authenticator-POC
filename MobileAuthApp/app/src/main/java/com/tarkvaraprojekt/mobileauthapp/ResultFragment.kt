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
import com.google.gson.JsonParser
import com.koushikdutta.ion.Ion
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentResultBinding
import com.tarkvaraprojekt.mobileauthapp.model.ParametersViewModel
import org.json.JSONObject

/**
 * ResultFragment is used to create a JWT and to send response to the website/application
 * that launched the MobileAuthApp. If the mobile auth app was started by a website
 * the result is sent to a server with a POST request.
 */
class ResultFragment : Fragment() {

    private val paramsModel: ParametersViewModel by activityViewModels()

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postToken()
    }

    /**
     * Only used when the MobileAuthApp was launched by an app. Not for website use.
     * Not really the safest way of doing things, but sufficient for POC purposes.
     */
    private fun createResponse(
        success: Boolean = true,
        idCode: String = "noCode",
        name: String = "noName",
        authority: String = "noAuthority"
    ) {
        val responseCode =
            if (success) AppCompatActivity.RESULT_OK else AppCompatActivity.RESULT_CANCELED
        val resultIntent = Intent()
        resultIntent.putExtra("idCode", idCode)
        resultIntent.putExtra("name", name)
        resultIntent.putExtra("authority", authority)
        requireActivity().setResult(responseCode, resultIntent)
        requireActivity().finish()
    }

    /**
     * Makes a POST request to the backend server with a tokenItem
     */
    fun postToken() {
        val json = JsonObject()
        json.addProperty("auth-token", paramsModel.token)



        Ion.getDefault(activity).conscryptMiddleware.enable(false)
        val ion = Ion.with(activity)
            .load(paramsModel.authUrl)
        for ((header, value) in paramsModel.headers) {
            ion.setHeader(header, value)
        }

        ion
            .setJsonObjectBody(json)
            .asJsonObject()
            .setCallback { e, result ->
                Log.i("resultTag", result.toString())
                if (result == null) {
                    if (args.mobile) {
                        createResponse(false)
                    } else {
                        requireActivity().finishAndRemoveTask()
                    }
                } else {
                    if (args.mobile) {
                        val userData = result.asJsonObject["userData"]
                        val idCode = userData.asJsonObject["idCode"].asString
                        val name = userData.asJsonObject["name"].asString
                        val authority = result.asJsonObject["roles"].asJsonArray[0].asJsonObject["authority"].asString
                        createResponse(true, idCode, name, authority)
                    } else {
                        requireActivity().finishAndRemoveTask()
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}