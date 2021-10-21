package com.tarkvaraprojekt.mobileauthapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentPinBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that deals with asking the user for PIN 1. If the user has already saved the PIN 1 then it is not asked again
 * and the fragment is skipped and if the PIN 1 is not saved then the user is asked whether it should be saved or
 * not before continuing.
 */
class PinFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentPinBinding? = null

    // Navigation arguments:
    // saving = true means that the user must be returned to the settings menu
    // reading = true means that we are reading information from the ID card that does
    //                not require PIN 1 so it is not necessary to ask it.
    private val args: CanFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkIfSkip()
        // If the user arrives from the settings menu then the button says
        // save instead of continue.
        if (args.saving) {
            binding!!.nextButton.text = getString(R.string.save_text)
        }
        binding!!.nextButton.setOnClickListener { checkEnteredPin() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
    }

    /**
     * Checks if the current fragment can be skipped or not.
     * If the user has PIN 1 saved on the device or PIN 1 is not required
     * then the PIN 1 won't be asked.
     */
    private fun checkIfSkip() {
        if (args.reading) {
            goToTheNextFragment()
        } else if (viewModel.userPin.length in 4..12) {
            goToTheNextFragment()
        }
    }

    /**
     * Takes user to the next fragment, which is AuthFragment.
     */
    private fun goToTheNextFragment() {
        val action = PinFragmentDirections.actionPinFragmentToAuthFragment(auth = args.auth)
        findNavController().navigate(action)
    }

    /**
     * Checks whether the user has entered a PIN 1 with length between [4, 12] in the
     * input field. If yes then the user is allowed to continue otherwise the user is
     * allowed to modify the entered PIN 1.
     */
    private fun checkEnteredPin() {
        val enteredPin = binding!!.pinEditText.editText?.text.toString()
        if (enteredPin.length in 4..12) {
            viewModel.setUserPin(enteredPin)
            if (args.saving) {
                viewModel.storePin(requireContext())
                goToTheStart()
            } else {
                val storePinQuestion = getDialog()
                storePinQuestion?.show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.length_pin), Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Builds a dialog that asks the user whether the entered PIN 1 should be saved
     * on the device or not.
     */
    private fun getDialog(): AlertDialog? {
        return activity?.let { frag ->
            val builder = AlertDialog.Builder(frag)
            builder.apply {
                // If response is positive save the PIN 1 on the device.
                setPositiveButton(R.string.save_text) { _, _ ->
                    viewModel.storePin(
                        requireContext()
                    )
                    goToTheNextFragment()
                }
                setNegativeButton(R.string.deny_text) { _, _ ->
                    goToTheNextFragment()
                }
            }
            builder.setMessage(R.string.pin_save_request)
            builder.setTitle(R.string.save_pin_title)
            builder.create()
        }
    }

    /**
     * Returns user to the start. If the user arrived from the settings menu then the start is
     * settings menu not the HomeFragment.
     */
    private fun goToTheStart() {
        if (args.saving) {
            findNavController().navigate(R.id.action_pinFragment_to_settingsFragment)
        } else {
            viewModel.clearUserInfo()
            findNavController().navigate(R.id.action_pinFragment_to_homeFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}