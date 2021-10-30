package com.tarkvaraprojekt.mobileauthapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentPin2Binding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that deals with asking PIN 2 from the user. Basically the same as PIN 1 fragment.
 */
class Pin2Fragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentPin2Binding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPin2Binding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.nextButton.setOnClickListener {
            checkPin2Length()
        }
        binding!!.cancelButton.setOnClickListener {
            cancel()
        }
    }

    /**
     * Checks if the length of the entered PIN 2 is in range 5..12 and if it is
     * then it is saved to the viewModel.
     */
    private fun checkPin2Length() {
        val enteredPin2 = binding!!.pin2EditText.editText?.text.toString()
        if (enteredPin2.length in 5..12) {
            viewModel.setUserPin2(enteredPin2)
        } else {
            Toast.makeText(requireContext(), getString(R.string.length_pin2), Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Authentication process is cancelled when cancel button is clicked and the application
     * will be closed.
     */
    private fun cancel() {
        val resultIntent = Intent()
        requireActivity().setResult(AppCompatActivity.RESULT_CANCELED, resultIntent)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}