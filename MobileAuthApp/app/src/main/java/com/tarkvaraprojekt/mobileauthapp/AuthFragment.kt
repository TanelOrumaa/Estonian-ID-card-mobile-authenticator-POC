package com.tarkvaraprojekt.mobileauthapp

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentAuthBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that asks the user to detect the ID card with mobile NFC chip.
 * Currently contains a next button that won't be needed later on.
 * This button is just needed to test navigation between fragments so that every step exists.
 */
class AuthFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentAuthBinding? = null

    private lateinit var timer: CountDownTimer

    private var timeRemaining: Int = 90

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timer = object : CountDownTimer(90000, 1000) {
            override fun onTick(p0: Long) {
                binding!!.timeCounter.text = getString(R.string.time_left, timeRemaining)
                timeRemaining--
            }

            override fun onFinish() {
                binding!!.timeCounter.text = getString(R.string.no_time)
            }
        }.start()
        binding!!.nextButton.setOnClickListener { goToNextFragment() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
    }

    private fun goToNextFragment() {
        //Dummy data for now
        viewModel.setUserFirstName("John")
        viewModel.setUserLastName("Doe")
        viewModel.setUserIdentificationNumber("012345678910")
        timer.cancel()
        findNavController().navigate(R.id.action_authFragment_to_userFragment)
    }

    private fun goToTheStart() {
        viewModel.clearUserInfo()
        timer.cancel()
        findNavController().navigate(R.id.action_authFragment_to_homeFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}