package com.tarkvaraprojekt.mobileauthapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
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
    private val args: PinFragmentArgs by navArgs()

    private var saveToggle = true

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
        // Switch should be not visible when user is in savings mode
        if (args.saving) {
            binding!!.savePinQuestion.visibility = View.GONE
            binding!!.saveLayout.visibility = View.GONE
        } else {
            saveToggle =
                activity?.getPreferences(Context.MODE_PRIVATE)?.getBoolean("saveToggle", true) == true //Android Studio recommendation to get rid of Boolean?.
            Log.i("myLogging", activity?.getPreferences(Context.MODE_PRIVATE)?.getBoolean("saveToggle", true).toString())
            if (!saveToggle) {
                binding!!.saveSwitch.isChecked = false
            }
            binding!!.saveSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding!!.saveStatus.text = getString(R.string.pin_save_on)
                    activity?.getPreferences(Context.MODE_PRIVATE)?.edit()?.putBoolean("saveToggle", true)?.apply()
                } else {
                    binding!!.saveStatus.text = getString(R.string.pin_save_off)
                    activity?.getPreferences(Context.MODE_PRIVATE)?.edit()?.putBoolean("saveToggle", false)?.apply()
                }
                saveToggle = !saveToggle
            }
        }
        binding!!.buttonContinue.setOnClickListener { checkEnteredPin() }
        binding!!.buttonCancel.setOnClickListener { goToTheStart() }
    }

    /**
     * Takes user to the next fragment, which is AuthFragment.
     */
    private fun goToTheNextFragment() {
        val action = PinFragmentDirections.actionPinFragmentToAuthFragment(auth = args.auth, mobile = args.mobile)
        findNavController().navigate(action)
    }

    /**
     * Returns user to the start. If the user arrived from the settings menu then the start is
     * settings menu not the HomeFragment.
     */
    private fun goToTheStart() {
        if (args.saving) {
            findNavController().navigate(R.id.action_pinFragment_to_settingsFragment)
        } else if (args.auth || args.mobile) {
            if (args.mobile) {
                val resultIntent = Intent()
                requireActivity().setResult(AppCompatActivity.RESULT_CANCELED, resultIntent)
                requireActivity().finish()
            } else {
                requireActivity().finishAndRemoveTask()
            }
        } else {
            findNavController().navigate(R.id.action_canFragment_to_homeFragment)
        }
    }

    /**
     * Checks if the current fragment can be skipped or not.
     * If the user has PIN 1 saved on the device or PIN 1 is not required
     * then the PIN 1 won't be asked.
     */
    private fun checkIfSkip() {
        if (viewModel.userPin.length in 4..12) {
            goToTheNextFragment()
        }
    }

    /**
     * Method that creates and shows a snackbar that tells the user that PIN 1 has been saved
     */
    private fun showSnackbar() {
        val snackbar = Snackbar.make(requireView(), R.string.pin_status_saved, Snackbar.LENGTH_SHORT)
        val snackbarText: TextView = snackbar.view.findViewById(R.id.snackbar_text)
        snackbarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getDimension(R.dimen.small_text))
        snackbar.show()
    }

    /**
     * Checks whether the user has entered a PIN 1 with length between [4, 12] in the
     * input field. If yes then the user is allowed to continue otherwise the user is
     * allowed to modify the entered PIN 1.
     */
    private fun checkEnteredPin() {
        val enteredPin = binding!!.pinTextField.editText?.text.toString()
        if (enteredPin.length in 4..12) {
            viewModel.setUserPin(enteredPin)
            if (args.saving) {
                viewModel.storePin(requireContext())
                showSnackbar()
                goToTheStart()
            } else {
                if (saveToggle) {
                    viewModel.storePin(requireContext())
                    showSnackbar()
                }
                goToTheNextFragment()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.pin_helper_text), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}