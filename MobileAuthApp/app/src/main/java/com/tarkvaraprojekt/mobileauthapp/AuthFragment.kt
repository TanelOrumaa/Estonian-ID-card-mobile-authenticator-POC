package com.tarkvaraprojekt.mobileauthapp

import android.content.Intent
import android.nfc.NfcAdapter
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

    private val intentParameters: ParametersViewModel by activityViewModels()

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
                    binding!!.timeCounter.text = getString(R.string.no_time)
                } else {
                    binding!!.timeCounter.text = getString(R.string.time_left, timeRemaining)
                }
            }

            override fun onFinish() {
                Thread.sleep(750)
                goToTheStart()
            }
        }.start()
        binding!!.nextButton.setOnClickListener { goToNextFragment() }
        binding!!.cancelButton.setOnClickListener { goToTheStart() }
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter != null)
            getInfoFromIdCard(adapter)
    }

    private fun getInfoFromIdCard(adapter: NfcAdapter) {
        if (args.reading) {
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
                        val response = comms.readPersonalData(byteArrayOf(1, 2, 6, 3, 4, 8))
                        viewModel.setUserFirstName(response[1])
                        viewModel.setUserLastName(response[0])
                        viewModel.setUserIdentificationNumber(response[2])
                        viewModel.setGender(response[3])
                        viewModel.setCitizenship(response[4])
                        viewModel.setExpiration(response[5])
                        requireActivity().runOnUiThread {
                            binding!!.timeCounter.text = getString(R.string.data_read)
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            binding!!.timeCounter.text = getString(R.string.no_success)
                        }
                        // If the CAN is wrong we will also delete the saved CAN so that the user won't use it again.
                        viewModel.deleteCan(requireContext())
                        // Gives user some time to read the error message
                        Thread.sleep(1000)
                        goToTheStart()
                    } finally {
                        adapter.disableReaderMode(activity)
                    }
                }
            }, NfcAdapter.FLAG_READER_NFC_A, null)
        } else { //We want to create a JWT instead of reading the info from the card.
            goToNextFragment()
        }
    }

    private fun goToNextFragment() {
        timer.cancel()
        if (args.auth) {
            val action = AuthFragmentDirections.actionAuthFragmentToResultFragment(mobile = args.mobile)
            findNavController().navigate(action)
        } else {
            findNavController().navigate(R.id.action_authFragment_to_userFragment)
        }
    }

    private fun goToTheStart() {
        viewModel.clearUserInfo()
        timer.cancel()
        if (args.reading) {
            findNavController().navigate(R.id.action_authFragment_to_homeFragment)
        } else {
            if (!args.mobile) {
                //Currently for some reason the activity is not killed entirely. Must be looked into further.
                requireActivity().finish()
                exitProcess(0)
            } else {
                val resultIntent = Intent()
                requireActivity().setResult(AppCompatActivity.RESULT_CANCELED, resultIntent)
                requireActivity().finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}