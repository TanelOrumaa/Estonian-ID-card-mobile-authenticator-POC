package com.tarkvaraprojekt.mobileauthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.nfc.NfcAdapter
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.koushikdutta.ion.Ion
import com.tarkvaraprojekt.mobileauthapp.NFC.Comms
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentHomeBinding
import com.tarkvaraprojekt.mobileauthapp.model.ParametersViewModel
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel
import java.lang.Exception
import java.lang.RuntimeException
import java.net.URL

/**
 * HomeFragment is only shown to the user when then the user launches the application. When the application
 * is launched by another application or a website then this Fragment will be skipped.
 * This fragment uses the fields from the MainActivity by casting the activity to MainActivity.
 * This might not be the best practice, but the application uses a single activity design so it should
 * always work.
 */
class HomeFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private val intentParams: ParametersViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // The ID card reader mode is enabled on the home fragment when can is saved.
    private var canSaved: Boolean = false

    // Is the app used for authentication
    private var auth: Boolean = false

    private var receiver: BroadcastReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Making settings menu active again
        (activity as MainActivity).menuAvailable = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialChecks()
        if (requireActivity().intent.data?.getQueryParameter("action") != null) {
            // Currently we only support authentication not signing.
            auth = true
        }
        val mobile = requireActivity().intent.getBooleanExtra("mobile", false)
        if (auth || mobile) {
            startAuthentication(mobile)
        } else {
            receiver = object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    updateAction(canSaved)
                }
            }
            val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
            requireActivity().registerReceiver(receiver, filter)
            updateAction(canSaved)
        }
    }

    /**
     * Starts the process of interacting with the ID card by sending user to the CAN fragment.
     */
    private fun goToTheNextFragment(mobile: Boolean = false) {
        (activity as MainActivity).menuAvailable = false
        val action = HomeFragmentDirections.actionHomeFragmentToCanFragment(auth = true, mobile = mobile)
        findNavController().navigate(action)
    }

    /**
     * Method that starts the authentication use case.
     *
     * NOTE: Comment out try-catch block when testing without backend
     */
    private fun startAuthentication(mobile: Boolean) {
        try {
            if (mobile) {
                // We use !! to get extras because we want an exception to be thrown when something is missing.
                intentParams.setChallenge(requireActivity().intent.getStringExtra("challenge")!!)
                intentParams.setAuthUrl(requireActivity().intent.getStringExtra("authUrl")!!)
                intentParams.setOrigin(requireActivity().intent.getStringExtra("originUrl")!!)
                goToTheNextFragment(mobile)
            } else { //Website
                /*
                var challenge = requireActivity().intent.data!!.getQueryParameter("challenge")!!
                // TODO: Since due to encoding plus gets converted to space, temporary solution is to replace it back.
                challenge = challenge.replace(" ", "+")
                intentParams.setChallenge(challenge)
                intentParams.setAuthUrl(requireActivity().intent.data!!.getQueryParameter("authUrl")!!)
                intentParams.setOrigin(requireActivity().intent.data!!.getQueryParameter("originUrl")!!)
                */
                val getAuthChallengeUrl = requireActivity().intent.data!!.getQueryParameter("getAuthChallengeUrl")!!
                val postAuthTokenUrl = requireActivity().intent.data!!.getQueryParameter("postAuthTokenUrl")!!
                val headers = requireActivity().intent.data!!.getQueryParameter("headers")!!
                intentParams.setAuthUrl(postAuthTokenUrl)
                intentParams.setOrigin("https://" + URL(getAuthChallengeUrl).host)
                intentParams.setHeaders(headers)
                Ion.getDefault(activity).conscryptMiddleware.enable(false)
                Ion.with(activity)
                    .load(getAuthChallengeUrl)
                    .asJsonObject()
                    .setCallback { _, result ->
                        try {
                            // Get data from the result and call launchAuth method
                            val challenge = result.asJsonObject["nonce"].toString().replace("\"", "")
                            intentParams.setChallenge(challenge)
                            goToTheNextFragment(mobile)
                        } catch (e: Exception) {
                            Log.i("GETrequest", "was unsuccessful")
                            throw RuntimeException()
                        }
                    }

            }
        } catch (e: Exception) {
            // There was a problem with parameters, which means that authentication is not possible.
            // In that case we will cancel the authentication immediately as it would be waste of the user's time to carry on
            // before getting an inevitable error.
            val message = MaterialAlertDialogBuilder(requireContext())
            message.setTitle(getString(R.string.problem_parameters))
            if (intentParams.challenge == "") {
                message.setMessage(getString(R.string.problem_challenge))
            } else if (intentParams.authUrl == "") {
                message.setMessage(getString(R.string.problem_authurl))
            } else if (intentParams.origin == "") {
                message.setMessage(getString(R.string.problem_originurl))
            } else {
                message.setMessage(getString(R.string.problem_other))
            }
            message.setPositiveButton(getString(R.string.continue_button)) {_, _ ->
                val resultIntent = Intent()
                requireActivity().setResult(AppCompatActivity.RESULT_CANCELED, resultIntent)
                requireActivity().finish()
            }
            message.show()
        }
    }

    /**
     * Checks the state of the CAN, saved or not saved. Updates the text and logo.
     */
    private fun canState() {
        if (viewModel.userCan.length == 6) {
            binding.canStatusText.text = getString(R.string.can_status_saved)
            binding.canStatusLogo.setImageResource(R.drawable.ic_check_logo)
            canSaved = true
        } else {
            binding.canStatusText.text = getString(R.string.can_status_negative)
            binding.canStatusLogo.setImageResource(R.drawable.ic_info_logo)
            canSaved = false
        }
    }

    /**
     * Checks the state of the PIN 1, saved or not saved. Updates the text and logo.
     */
    private fun pinState() {
        if (viewModel.userPin.length in 4..12) {
            binding.pinStatusText.text = getString(R.string.pin_status_saved)
            binding.pinStatusLogo.setImageResource(R.drawable.ic_check_logo)
        } else {
            binding.pinStatusText.text = getString(R.string.pin_status_negative)
            binding.pinStatusLogo.setImageResource(R.drawable.ic_info_logo)
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
     * Method where all the initial checks that should be completed before any user input is accepted should be conducted.
     */
    private fun initialChecks() {
        viewModel.checkCan(requireContext())
        viewModel.checkPin(requireContext())
        displayStates()
    }

    /**
     * Displays a help message to the user explaining what the CAN is
     */
    private fun displayMessage(title: String, message: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.return_text){_, _ -> }
            .show()
        val title = dialog.findViewById<TextView>(R.id.alertTitle)
        title?.textSize = 24F

    }

    /**
     * Informs user whether the ID card can be detected or not.
     */
    private fun updateAction(canIsSaved: Boolean) {
        if (canIsSaved) {
            binding.detectionActionText.text = getString(R.string.action_detect)
            enableReaderMode()
            binding.homeActionButton.visibility = View.GONE
            binding.homeHelpButton.visibility = View.GONE
        } else {
            binding.detectionActionText.text = getString(R.string.action_detect_unavailable)
            binding.homeActionButton.text = getString(R.string.add_can_text)
            binding.homeActionButton.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCanFragment(saving = true, fromhome = true)
                findNavController().navigate(action)
            }
            binding.homeHelpButton.setOnClickListener {
                displayMessage(getString(R.string.can_question), getString(R.string.can_explanation))
            }
            binding.homeActionButton.visibility = View.VISIBLE
            binding.homeHelpButton.visibility = View.VISIBLE
        }
    }

    /**
     * Resets the error message and allows the user to try again
     */
    private fun reset() {
        binding.homeActionButton.text = getString(R.string.try_again_text)
        binding.homeActionButton.setOnClickListener {
            updateAction(canSaved)
        }
        binding.homeActionButton.visibility = View.VISIBLE
    }

    /**
     * Method that enables the NFC reader mode, which allows the app to communicate with the ID card and retrieve information.
     */
    private fun enableReaderMode() {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null || !adapter.isEnabled) {
            binding.detectionActionText.text = getString(R.string.nfc_not_available)
        } else {
            adapter.enableReaderMode(activity, { tag ->
                requireActivity().runOnUiThread {
                    binding.detectionActionText.text = getString(R.string.card_detected)
                }
                val card = IsoDep.get(tag)
                card.timeout = 32768
                card.use {
                    try {
                        val comms = Comms(it, viewModel.userCan)
                        val response = comms.readPersonalData(byteArrayOf(1, 2, 6, 3, 4, 8))
                        viewModel.setUserFirstName(response[1])
                        viewModel.setUserLastName(response[0])
                        viewModel.setUserIdentificationNumber(response[2])
                        viewModel.setGender(response[3])
                        viewModel.setCitizenship(response[4])
                        viewModel.setExpiration(response[5])
                        requireActivity().runOnUiThread {
                            val action = HomeFragmentDirections.actionHomeFragmentToUserFragment()
                            findNavController().navigate(action)
                        }
                    } catch (e: Exception) {
                        when(e) {
                            is TagLostException -> requireActivity().runOnUiThread {
                                binding.detectionActionText.text = getString(R.string.id_card_removed_early)
                                reset()
                            }
                            else -> requireActivity().runOnUiThread {
                                binding.detectionActionText.text = getString(R.string.nfc_reading_error)
                                viewModel.deleteCan(requireContext())
                                canState()
                                reset()
                            }
                        }
                    } finally {
                        adapter.disableReaderMode(activity)
                    }
                }
            }, NfcAdapter.FLAG_READER_NFC_A, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (receiver != null) {
            requireActivity().unregisterReceiver(receiver)
        }
        _binding = null
    }
}