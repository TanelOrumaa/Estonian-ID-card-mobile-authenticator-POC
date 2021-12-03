package com.tarkvaraprojekt.mobileauthapp.menu

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.tarkvaraprojekt.mobileauthapp.MainActivity
import com.tarkvaraprojekt.mobileauthapp.R
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentSettingsBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * This fragment allows the user to save the CAN and the PIN 1 and also to delete them if necessary.
 * Should only be accessible for the user from the HomeFragment and not during the
 * authentication process itself.
 */
class SettingsFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentSettingsBinding? = null

    private var showPin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showCanField()
        showPinField()
        togglePinButton()
        binding!!.canMenuAction.setOnClickListener { canAction() }
        binding!!.pinMenuAction.setOnClickListener { pinAction() }
        binding!!.pinMenuShow.setOnClickListener { togglePin() }
        binding!!.returnButton.setOnClickListener { backToHome() }
    }

    /**
     * Method for showing a snackbar with a message that is given as a parameter
     */
    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
        val snackbarText: TextView = snackbar.view.findViewById(R.id.snackbar_text)
        snackbarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getDimension(R.dimen.small_text))
        snackbar.show()
    }

    /**
     * Method for showing the CAN field to the user and can be used to refresh the field as well.
     */
    private fun showCanField() {
        if (viewModel.userCan.length == 6) {
            binding!!.canSaved.text = getString(R.string.saved_can, viewModel.userCan)
            binding!!.canMenuAction.text = getString(R.string.can_delete)
        } else {
            binding!!.canSaved.text = getString(R.string.saved_can, getString(R.string.missing))
            binding!!.canMenuAction.text = getString(R.string.add_can_text)
        }
    }

    /**
     * Method that allows the user to delete saved CAN from the device and also to save new a CAN if
     * currently there is no CAN saved.
     */
    private fun canAction() {
        if (viewModel.userCan.length == 6) {
            viewModel.deleteCan(requireContext())
            showCanField()
            showSnackbar(getString(R.string.can_deleted))
        } else {
            (activity as MainActivity).inMenu = false
            val action = SettingsFragmentDirections.actionSettingsFragmentToCanFragment(saving = true)
            findNavController().navigate(action)
        }
    }

    /**
     * Method for showing the PIN 1 field to the user and can be used to refresh the field as well.
     * The PIN 1 is hidden by default and when it is hidden it is always shown as **** despite the
     * length of the PIN 1. Can be made visible with toggle button.
     */
    private fun showPinField() {
        if (viewModel.userPin.length in 4..12) {
            binding!!.pinMenuShow.visibility = Button.VISIBLE
            if (showPin)
                binding!!.pinSaved.text = getString(R.string.saved_pin, viewModel.userPin)
            else
                binding!!.pinSaved.text = getString(R.string.saved_pin, getString(R.string.hidden_pin))
            binding!!.pinMenuAction.text = getString(R.string.pin1_delete)
        } else {
            binding!!.pinMenuShow.visibility = Button.GONE
            binding!!.pinSaved.text = getString(R.string.saved_pin, getString(R.string.missing))
            binding!!.pinMenuAction.text = getString(R.string.pin1_add)
        }
    }

    /**
     * Method that allows the user to delete saved PIN 1 from the device and also to save a new PIN 1 if
     * currently there is no PIN 1 saved.
     */
    private fun pinAction() {
        if (viewModel.userPin.length in 4..12) {
            viewModel.deletePin(requireContext())
            showPinField()
            showSnackbar(getString(R.string.pin_deleted))
        } else {
            (activity as MainActivity).inMenu = false
            val action = SettingsFragmentDirections.actionSettingsFragmentToPinFragment(saving = true)
            findNavController().navigate(action)
        }
    }

    /**
     * Hides the PIN 1 or makes it visible.
     */
    private fun togglePin() {
        showPin = !showPin
        togglePinButton()
        showPinField()
    }

    /**
     * Updates the text on the button that controls the visiblity of the PIN 1.
     */
    private fun togglePinButton() {
        if (showPin) {
            binding!!.pinMenuShow.text = getString(R.string.hide)
        } else {
            binding!!.pinMenuShow.text = getString(R.string.show)
        }
    }

    /**
     * Navigates back to home fragment.
     */
    private fun backToHome() {
        (activity as MainActivity).inMenu = false
        findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}