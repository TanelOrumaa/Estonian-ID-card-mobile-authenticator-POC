package com.tarkvaraprojekt.mobileauthapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentCanBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that deals with asking the user for six digit CAN
 */
class CanFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentCanBinding? = null

    // Navigation arguments. saving = true means that we are navigating here from the settings menu and must return to the settings
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
        if (viewModel.userCan.length == 6) {
            skip()
        }
        if (args.saving) {
            binding!!.nextButton.text = getString(R.string.save_text)
        }
        binding!!.nextButton.setOnClickListener { goToNextFragment() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
    }

    // If CAN is already set
    private fun skip() {
        findNavController().navigate(R.id.action_canFragment_to_pinFragment)
    }

    // Might need some rework, must break it up and make logic better.
    private fun goToNextFragment() {
        val enteredCan = binding!!.canEditText.editText?.text.toString()
        if (enteredCan.length != 6) {
            Toast.makeText(requireContext(), getString(R.string.length_can), Toast.LENGTH_SHORT)
                .show()
        } else {
            viewModel.setUserCan(
                binding!!.canEditText.editText?.text.toString()
            )
            if (args.saving) {
                viewModel.storeCan(requireContext())
                findNavController().navigate(R.id.action_canFragment_to_settingsFragment)
            } else {
                val canStoreQuestion: AlertDialog? = activity?.let { frag ->
                    val builder = AlertDialog.Builder(frag)
                    builder.apply {
                        setPositiveButton(R.string.save_text) { _, _ ->
                            viewModel.storeCan(
                                requireContext()
                            )
                            findNavController().navigate(R.id.action_canFragment_to_pinFragment)
                        }
                        setNegativeButton(R.string.deny_text) { _, _ ->
                            findNavController().navigate(R.id.action_canFragment_to_pinFragment)
                        }
                    }
                    builder.setMessage(R.string.can_save_request)
                    builder.setTitle(R.string.save_can_title)
                    builder.create()
                }
                canStoreQuestion?.show()
            }
        }
    }

    private fun goToTheStart() {
        if (args.saving) {
            findNavController().navigate(R.id.action_canFragment_to_settingsFragment)
        } else {
            viewModel.clearUserInfo()
            findNavController().navigate(R.id.action_canFragment_to_homeFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}