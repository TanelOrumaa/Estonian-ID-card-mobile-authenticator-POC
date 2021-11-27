package com.tarkvaraprojekt.mobileauthapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tarkvaraprojekt.mobileauthapp.NFC.Comms
import com.tarkvaraprojekt.mobileauthapp.auth.Authenticator
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentAuthBinding
import com.tarkvaraprojekt.mobileauthapp.model.ParametersViewModel
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel
import java.lang.Exception
import kotlin.system.exitProcess

/**
 * Fragment that asks the user to detect the ID card with mobile NFC chip.
 * Currently contains a next button that won't be needed later on.
 * This button is just needed to test navigation between fragments so that every step exists.
 */
class AuthFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private val paramsModel: ParametersViewModel by activityViewModels()

    private var binding: FragmentAuthBinding? = null

    private val args: CanFragmentArgs by navArgs()

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
        timer = object : CountDownTimer((timeRemaining * 1000).toLong(), 1000) {
            override fun onTick(p0: Long) {
                timeRemaining--
                if (timeRemaining == 0) {
                    binding?.timeCounter?.text = getString(R.string.no_time)
                } else {
                    binding?.timeCounter?.text = getString(R.string.time_left, timeRemaining)
                }
            }

            override fun onFinish() {
                Thread.sleep(750)
                cancelAuth()
            }
        }.start()
        // The button exists in code for testing reasons, but not visible to the user anymore unless visibility is changed in the code.
        binding!!.nextButton.visibility = View.GONE
        binding!!.nextButton.setOnClickListener { goToNextFragment() }
        binding!!.cancelButton.setOnClickListener { cancelAuth() }
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter != null)
            getInfoFromIdCard(adapter)
        else { // If NFC adapter can not be detected then end the auth process as it is not possible to read an ID card
            cancelAuth()
        }
    }

    private fun goToNextFragment() {
        timer.cancel()
        val action = AuthFragmentDirections.actionAuthFragmentToResultFragment(mobile = args.mobile)
        findNavController().navigate(action)
    }

    private fun cancelAuth() {
        viewModel.clearUserInfo()
        timer.cancel()
        if (args.mobile) {
            val resultIntent = Intent()
            requireActivity().setResult(AppCompatActivity.RESULT_CANCELED, resultIntent)
            requireActivity().finish()
        } else {
            requireActivity().finishAndRemoveTask()
        }
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
                try {
                    val comms = Comms(it, viewModel.userCan)
                    val jws = Authenticator(comms).authenticate(
                        paramsModel.challenge,
                        paramsModel.origin,
                        viewModel.userPin
                    )
                    paramsModel.setToken(jws)
                    requireActivity().runOnUiThread {
                        goToNextFragment()
                    }
                } catch (e: Exception) {
                    when(e) {
                        is TagLostException -> requireActivity().runOnUiThread { binding!!.timeCounter.text = getString(R.string.id_card_removed_early) }
                        else -> {
                            when ("invalid pin") {
                                in e.message.toString().lowercase() -> requireActivity().runOnUiThread {
                                    val messagePieces = e.message.toString().split(" ")
                                    binding!!.timeCounter.text = getString(R.string.wrong_pin, messagePieces[messagePieces.size - 1])
                                    viewModel.deletePin(requireContext())
                                }
                                else -> requireActivity().runOnUiThread {
                                    binding!!.timeCounter.text = getString(R.string.wrong_can_text)
                                    viewModel.deleteCan(requireContext())
                                }
                            }
                        }
                    }
                    // Give user some time to read the error message
                    Thread.sleep(2000)
                    cancelAuth()
                } finally {
                    adapter.disableReaderMode(activity)
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_A, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}