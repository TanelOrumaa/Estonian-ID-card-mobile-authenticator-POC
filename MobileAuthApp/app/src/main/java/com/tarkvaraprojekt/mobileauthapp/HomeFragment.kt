package com.tarkvaraprojekt.mobileauthapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentHomeBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * HomeFragment is only shown to the user when then the user launches the application. When the application
 * is launched by another application or a website then this Fragment will be skipped.
 * This fragment uses the fields from the MainActivity by casting the activity to MainActivity.
 * This might not be the best practice, but the application uses a single activity design so it should
 * always work.
 */
class HomeFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Making settings menu active again
        (activity as MainActivity).menuAvailable = true
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialChecks()
        var mobile = false
        if (requireActivity().intent.data?.getQueryParameter("arg1") != null) {
            mobile = true
        }
        val auth = requireActivity().intent.getBooleanExtra("auth", false)
        if (auth || mobile){
            goToTheNextFragment(true, mobile)
        }
        binding!!.beginButton.setOnClickListener { goToTheNextFragment() }
    }

    /**
     * Method where all the initial checks that should be done before any user input is accepted should be added.
     */
    private fun initialChecks() {
        viewModel.checkCan(requireContext())
        viewModel.checkPin(requireContext())
        displayStates()
    }

    /**
     * Starts the process of interacting with the ID card by sending user to the CAN fragment.
     */
    private fun goToTheNextFragment(auth: Boolean = false, mobile: Boolean = false) {
        // Making settings menu inactive
        (activity as MainActivity).menuAvailable = false
        // Currently saving is true because the application is not yet integrated with
        // other applications or websites.
        // TODO: Check the navigation action default values. Not everything has to be declared implicitly.
        if (auth) {
            val action = HomeFragmentDirections.actionHomeFragmentToCanFragment(reading = false, auth = true, mobile = mobile)
            findNavController().navigate(action)
        } else {
            val action = HomeFragmentDirections.actionHomeFragmentToCanFragment(reading = true, auth = false, mobile = mobile)
            findNavController().navigate(action)
        }
    }

    /**
     * Displays texts that inform the user whether the CAN and PIN 1 are saved on the device or not.
     * This might help the user to save some time as checking menu is not necessary unless the user
     * wishes to make changes to the saved CAN or PIN 1.
     */
    private fun displayStates() {
        canState()
        pinState()
    }

    /**
     * Checks the state of the CAN, saved or not saved. Updates the text and logo.
     */
    private fun canState() {
        if (viewModel.userCan.length == 6) {
            binding!!.canStatusText.text = getString(R.string.can_status_saved)
            binding!!.canStatusLogo.setImageResource(R.drawable.ic_check_logo)
        } else {
            binding!!.canStatusText.text = getString(R.string.can_status_negative)
            binding!!.canStatusLogo.setImageResource(R.drawable.ic_info_logo)
        }
    }

    /**
     * Checks the state of the PIN 1, saved or not saved. Updates the text and logo.
     */
    private fun pinState() {
        if (viewModel.userPin.length in 4..12) {
            binding!!.pinStatusText.text = getString(R.string.pin_status_saved)
            binding!!.pinStatusLogo.setImageResource(R.drawable.ic_check_logo)
        } else {
            binding!!.pinStatusText.text = getString(R.string.pin_status_negative)
            binding!!.pinStatusLogo.setImageResource(R.drawable.ic_info_logo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}