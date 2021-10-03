package com.tarkvaraprojekt.mobileauthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentCanBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that deals with asking the user for six digit CAN
 */
class CanFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentCanBinding? = null

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

        binding!!.nextButton.setOnClickListener { goToNextFragment() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
    }

    private fun goToNextFragment() {
        val enteredCan = binding!!.canEditText.editText?.text.toString()
        if (enteredCan.length != 6) {
            Toast.makeText(requireContext(), getString(R.string.length_can), Toast.LENGTH_SHORT)
                .show()
        } else {
            viewModel.setUserCan(
                binding!!.canEditText.editText?.text.toString()
            )
            findNavController().navigate(R.id.action_canFragment_to_authFragment)
        }
    }

    private fun goToTheStart() {
        viewModel.clearUserInfo()
        findNavController().navigate(R.id.action_canFragment_to_homeFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}