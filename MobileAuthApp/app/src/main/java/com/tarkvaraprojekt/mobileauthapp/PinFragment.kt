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
 * Fragment that deals with asking the user for PIN1
 */
class PinFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentPinBinding? = null

    // Navigation arguments. saving = true means that we are navigating here from the settings menu and must return to the settings
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
        if (viewModel.userPin.length in 4..12) {
            skip()
        }
        if (args.saving) {
            binding!!.nextButton.text = getString(R.string.save_text)
        }
        binding!!.nextButton.setOnClickListener { goToNextFragment() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
    }

    private fun skip() {
        findNavController().navigate(R.id.action_pinFragment_to_authFragment)
    }

    private fun goToNextFragment() {
        val enteredPin1 = binding!!.pinEditText.editText?.text.toString()
        if (enteredPin1.length in 4..12) {
            viewModel.setUserPin(
                binding!!.pinEditText.editText?.text.toString()
            )
            if (args.saving) {
                viewModel.storePin(requireContext())
                findNavController().navigate(R.id.action_pinFragment_to_settingsFragment)
            } else {
                val canStoreQuestion: AlertDialog? = activity?.let { frag ->
                    val builder = AlertDialog.Builder(frag)
                    builder.apply {
                        setPositiveButton(R.string.save_text) { _, _ ->
                            viewModel.storePin(
                                requireContext()
                            )
                            findNavController().navigate(R.id.action_pinFragment_to_authFragment)
                        }
                        setNegativeButton(R.string.deny_text) { _, _ ->
                            findNavController().navigate(R.id.action_pinFragment_to_authFragment)
                        }
                    }
                    builder.setMessage(R.string.pin_save_request)
                    builder.setTitle(R.string.save_pin_title)
                    builder.create()
                }
                canStoreQuestion?.show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.length_pin), Toast.LENGTH_SHORT)
                .show()
        }
    }

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