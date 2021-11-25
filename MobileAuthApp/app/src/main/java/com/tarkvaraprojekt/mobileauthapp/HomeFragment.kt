package com.tarkvaraprojekt.mobileauthapp

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.NFC.Comms
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentHomeBinding
import com.tarkvaraprojekt.mobileauthapp.model.ParametersViewModel
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel
import java.lang.Exception
import java.net.URLDecoder

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

    private var binding: FragmentHomeBinding? = null

    // The ID card reader mode is enabled on the home fragment when can is saved.
    private var canSaved: Boolean = false


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

    // TODO: Split the contents of onViewCreated methods into smaller separate methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialChecks()
        updateAction(canSaved) // Should be called later
        var auth = false
        if (requireActivity().intent.data?.getQueryParameter("action") != null) {
            // Currently we only support authentication not signing.
            auth = true
        }
        val mobile = requireActivity().intent.getBooleanExtra("mobile", false)
        if (auth || mobile){
            try {
                if (mobile) {
                    // We use !! because we want an exception when something is not right.
                    intentParams.setChallenge(requireActivity().intent.getStringExtra("challenge")!!)
                    intentParams.setAuthUrl(requireActivity().intent.getStringExtra("authUrl")!!)
                    intentParams.setOrigin(requireActivity().intent.getStringExtra("originUrl")!!)
                } else { //Website
                    var challenge = requireActivity().intent.data!!.getQueryParameter("challenge")!!
                    // TODO: Since due to encoding plus gets converted to space, temporary solution is to replace it back.
                    challenge = challenge.replace(" ", "+")
                    intentParams.setChallenge(challenge)
                    intentParams.setAuthUrl(requireActivity().intent.data!!.getQueryParameter("authUrl")!!)
                    intentParams.setOrigin(requireActivity().intent.data!!.getQueryParameter("originUrl")!!)
                }
            } catch (e: Exception) {
                // There was a problem with parameters, which means that authentication is not possible.
                val resultIntent = Intent()
                requireActivity().setResult(AppCompatActivity.RESULT_CANCELED, resultIntent)
                requireActivity().finish()
            }

            goToTheNextFragment(true, mobile)
        }
    }

    /**
     * Starts the process of interacting with the ID card by sending user to the CAN fragment.
     *
     * NOTE: This method should only be used for authentication flow in the future.
     */
    private fun goToTheNextFragment(auth: Boolean = false, mobile: Boolean = false) {
        (activity as MainActivity).menuAvailable = false
        if (auth) {
            val action = HomeFragmentDirections.actionHomeFragmentToCanFragment(reading = false, auth = true, mobile = mobile)
            findNavController().navigate(action)
        } else {
            val action = HomeFragmentDirections.actionHomeFragmentToCanFragment(reading = true, auth = false, mobile = mobile)
            findNavController().navigate(action)
        }
    }

    /**
     * Checks the state of the CAN, saved or not saved. Updates the text and logo.
     */
    private fun canState() {
        if (viewModel.userCan.length == 6) {
            binding!!.canStatusText.text = getString(R.string.can_status_saved)
            binding!!.canStatusLogo.setImageResource(R.drawable.ic_check_logo)
            canSaved = true
        } else {
            binding!!.canStatusText.text = getString(R.string.can_status_negative)
            binding!!.canStatusLogo.setImageResource(R.drawable.ic_info_logo)
            canSaved = false
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
     * Informs user whether the ID card can be detected or not.
     */
    private fun updateAction(canIsSaved: Boolean) {
        if (canIsSaved) {
            binding!!.detectionActionText.text = getString(R.string.action_detect)
            enableReaderMode()
        } else {
            binding!!.detectionActionText.text = getString(R.string.action_detect_unavailable)
        }

    }

    // TODO: When error occurs it should be cleared within a reasonable timeframe and it should be possible to detect cards again
    // TODO: Listen to system broadcasts to detect if NFC is turned on/off during when the app is working
    private fun enableReaderMode() {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null) {
            binding!!.detectionActionText.text = getString(R.string.nfc_not_available)
        } else {
            adapter.enableReaderMode(activity, { tag ->
                requireActivity().runOnUiThread {
                    binding!!.detectionActionText.text = getString(R.string.card_detected)
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
                            is TagLostException -> requireActivity().runOnUiThread { binding!!.detectionActionText.text = getString(R.string.id_card_removed_early)}
                            else -> requireActivity().runOnUiThread { binding!!.detectionActionText.text = getString(R.string.nfc_reading_error) }
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
        binding = null
    }
}