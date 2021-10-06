package com.tarkvaraprojekt.mobileauthapp

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.NFC.Comms
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentAuthBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel
import kotlin.concurrent.thread

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
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter != null)
            getInfoFromIdCard(adapter)
    }

    private fun getInfoFromIdCard(adapter: NfcAdapter) {
        adapter.enableReaderMode(activity, { tag ->
            timer.cancel()
            requireActivity().runOnUiThread {
                binding!!.timeCounter.text = getString(R.string.card_detected)
            }
            val card = IsoDep.get(tag)
            card.timeout = 32768
            card.use {
                val comms = Comms(it, viewModel.userCan)
                val response = comms.readPersonalData(byteArrayOf(1, 2, 6))
                if (response != null) {
                    viewModel.setUserFirstName(response[1])
                    viewModel.setUserLastName(response[0])
                    viewModel.setUserIdentificationNumber(response[2])
                    requireActivity().runOnUiThread{
                        binding!!.timeCounter.text = getString(R.string.data_read)
                    }
                }
                it.close()
                adapter.disableReaderMode(activity)
            }
        }, NfcAdapter.FLAG_READER_NFC_A, null)
    }

    private fun goToNextFragment() {
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