package com.tarkvaraprojekt.mobileauthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentResultBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * ResultFragment is used to create a JWT and to send response to the website/application
 * that launched the MobileAuthApp.
 */
class ResultFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentResultBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}