package com.tarkvaraprojekt.mobileauthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentPinBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that deals with asking the user for PIN1
 */
class PinFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentPinBinding? = null

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

        binding!!.nextButton.setOnClickListener { goToNextFragment() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
    }

    private fun goToNextFragment() {
        val enteredPin1 = binding!!.pinEditText.editText?.text.toString()
        if (enteredPin1.length in 4..12) {
            viewModel.setUserPin(
                binding!!.pinEditText.editText?.text.toString()
            )
            findNavController().navigate(R.id.action_pinFragment_to_canFragment)
        } else {
            // Currently it is not important to enter PIN1 so we will allow the user to leave this field empty
            //Toast.makeText(requireContext(), getString(R.string.length_pin), Toast.LENGTH_SHORT)
            //    .show()
            viewModel.setUserPin("1234")
            findNavController().navigate(R.id.action_pinFragment_to_canFragment)
        }
    }

    private fun goToTheStart() {
        viewModel.clearUserInfo()
        findNavController().navigate(R.id.action_pinFragment_to_homeFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}