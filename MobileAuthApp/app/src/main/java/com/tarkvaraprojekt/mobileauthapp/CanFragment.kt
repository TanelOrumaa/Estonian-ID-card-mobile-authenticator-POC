package com.tarkvaraprojekt.mobileauthapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentCanBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel
import org.w3c.dom.Text

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
        binding!!.canTextField.editText?.addTextChangedListener {
            checkEnteredCan()
        }
        binding!!.buttonCancel.setOnClickListener { goToTheStart() }
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
        val action = CanFragmentDirections.actionCanFragmentToPinFragment(auth = args.auth, mobile = args.mobile)
        findNavController().navigate(action)
    }

    /**
     * Navigates the user back to the start depending on where the user arrived.
     * If the user arrived from the settings menu then the start is the settings menu
     * not the HomeFragment.
     */
    private fun goToTheStart() {
        if (args.saving) {
            if (args.fromhome) {
                findNavController().navigate(R.id.action_canFragment_to_homeFragment)
            } else {
                findNavController().navigate(R.id.action_canFragment_to_settingsFragment)
            }
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
     * Method that creates and shows a snackbar that tells the user that CAN has been saved
     */
    private fun showSnackbar() {
        val snackbar = Snackbar.make(requireView(), R.string.can_status_saved, Snackbar.LENGTH_SHORT)
        val snackbarText: TextView = snackbar.view.findViewById(R.id.snackbar_text)
        snackbarText.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getDimension(R.dimen.small_text))
        snackbar.show()
    }

    /**
     * Checks whether the user has entered a 6 digit can to the input field.
     * If yes then the user is allowed to continue otherwise the user is
     * allowed to modify the entered can.
     */
    private fun checkEnteredCan() {
        val enteredCan = binding!!.canTextField.editText?.text.toString()
        if (enteredCan.length == 6) {
            viewModel.setUserCan(enteredCan)
            viewModel.storeCan(requireContext()) //Maybe storeCan should always automatically call setUserCan method as well because these methods usually are used together
            showSnackbar()
            if (args.saving) {
                goToTheStart()
            } else {
                goToTheNextFragment()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}