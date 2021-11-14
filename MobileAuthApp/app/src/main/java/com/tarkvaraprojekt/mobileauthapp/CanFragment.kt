package com.tarkvaraprojekt.mobileauthapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentCanBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that deals with asking the user for a six digit CAN. If the CAN is already saved
 * then the fragment is skipped immediately and if the CAN is not saved then the user
 * is asked whether it should be saved for the future or not before continuing.
 */
class CanFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentCanBinding? = null

    // Navigation arguments:
    // saving = true means that we are navigating here from the settings menu and must return to the settings menu.
    // reading = true means that we are only reading the information from the ID card that does not need PIN 1,
    //                this information is passed on to the next PinFragment.
    private val args: CanFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCanBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkIfSkip()
        // If the user arrives from the settings menu then the button should say
        // save instead of continue.
        if (args.saving) {
            binding!!.nextButton.text = getString(R.string.save_text)
        }
        binding!!.nextButton.setOnClickListener { checkEnteredCan() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
    }

    /**
     * Checks if the current fragment can be skipped or not.
     * If the user has CAN saved on the device there is no need to ask it again.
     */
    private fun checkIfSkip() {
        if (viewModel.userCan.length == 6) {
            goToTheNextFragment()
        }
    }

    /**
     * Takes user to the next fragment, which is PinFragment.
     */
    private fun goToTheNextFragment() {
        val action = CanFragmentDirections.actionCanFragmentToPinFragment(reading = args.reading, auth = args.auth, mobile = args.mobile)
        findNavController().navigate(action)
    }

    /**
     * Checks whether the user has entered a 6 digit can to the input field.
     * If yes then the user is allowed to continue otherwise the user is
     * allowed to modify the entered can.
     */
    private fun checkEnteredCan() {
        val enteredCan = binding!!.canEditText.editText?.text.toString()
        if (enteredCan.length == 6) {
            viewModel.setUserCan(enteredCan)
            if (args.saving) {
                viewModel.storeCan(requireContext())
                goToTheStart()
            } else {
                val storeCanQuestion = getDialog()
                storeCanQuestion?.show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.length_can), Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Builds a dialog that asks the user whether the entered CAN should be saved
     * on the device or not.
     */
    private fun getDialog(): AlertDialog? {
        return activity?.let { frag ->
            val builder = AlertDialog.Builder(frag)
            builder.apply {
                // If response is positive then save the CAN on the device.
                setPositiveButton(R.string.save_text) { _, _ ->
                    viewModel.storeCan(
                        requireContext()
                    )
                    goToTheNextFragment()
                }
                setNegativeButton(R.string.deny_text) { _, _ ->
                    goToTheNextFragment()
                }
            }
            builder.setMessage(R.string.can_save_request)
            builder.setTitle(R.string.save_can_title)
            builder.create()
        }
    }

    /**
     * Navigates the user back to the start depending on where the user arrived.
     * If the user arrived from the settings menu then the start is the settings menu
     * not the HomeFragment.
     */
    private fun goToTheStart() {
        // TODO: Needs special handling when the app is launched with intent. Temporary solution at the moment.
        if (args.saving) {
            findNavController().navigate(R.id.action_canFragment_to_settingsFragment)
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}