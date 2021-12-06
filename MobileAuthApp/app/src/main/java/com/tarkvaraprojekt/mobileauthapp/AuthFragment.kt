package com.tarkvaraprojekt.mobileauthapp

import android.app.Activity
import android.content.Context
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
import com.tarkvaraprojekt.mobileauthapp.auth.AuthAppException
import com.tarkvaraprojekt.mobileauthapp.auth.Authenticator
import com.tarkvaraprojekt.mobileauthapp.auth.InvalidCANException
import com.tarkvaraprojekt.mobileauthapp.auth.InvalidPINException
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentAuthBinding
import com.tarkvaraprojekt.mobileauthapp.model.ParametersViewModel
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel
import java.io.IOException
import java.lang.Exception
import java.security.GeneralSecurityException
import kotlin.system.exitProcess

/**
 * Fragment that asks the user to detect the ID card with mobile NFC chip.
 * Currently contains a next button that won't be needed later on.
 * This button is just needed to test navigation between fragments so that every step exists.
 */
class AuthFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private val paramsModel: ParametersViewModel by activityViewModels()

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val args: CanFragmentArgs by navArgs()

    private lateinit var timer: CountDownTimer

    private var timeRemaining: Int = 90

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timer = object : CountDownTimer((timeRemaining * 1000).toLong(), 1000) {
            override fun onTick(p0: Long) {
                timeRemaining--
                if (timeRemaining == 0) {
                    binding.timeCounter.text = getString(R.string.no_time)
                } else {
                    binding.timeCounter.text = getString(R.string.time_left, timeRemaining)
                }
            }

            override fun onFinish() {
                Thread.sleep(750)
                cancelAuth()
            }
        }.start()
        // The button exists in code for testing reasons, but not visible to the user anymore unless visibility is changed in the code.
        binding.nextButton.visibility = View.GONE
        binding.nextButton.setOnClickListener { goToNextFragment() }
        binding.cancelButton.setOnClickListener { cancelAuth() }
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter != null)
            getInfoFromIdCard(adapter)
        else { // If NFC adapter can not be detected then end the auth process as it is not possible to read an ID card
            cancelAuth() // It would be a good idea to show user some notification as it might be confusing if the app suddenly closes
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
                binding.timeCounter.text = getString(R.string.card_detected)
            }
            var msgCode = 0
            var msgArg : Int? = null

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
                        binding.timeCounter.text = getString(R.string.data_read)
                        goToNextFragment()
                    }
                } catch (e: android.nfc.TagLostException) {
                    msgCode = R.string.tag_lost
                } catch (e: InvalidCANException) {
                    msgCode = R.string.wrong_can_text
                    // If the CAN is wrong we will also delete the saved CAN so that the user won't use it again.
                    viewModel.deleteCan(requireContext())
                } catch (e: InvalidPINException) {
                    msgCode = R.string.wrong_pin
                    msgArg = e.remainingAttempts
                    viewModel.deletePin(requireContext())
                } catch (e: AuthAppException) {
                    msgCode = when (e.code) {
                        400 -> R.string.err_parameter
                        401 -> R.string.err_authentication
                        446 -> R.string.err_card_locked
                        448 -> R.string.err_bad_data
                        500 -> R.string.err_internal
                        else -> R.string.err_unknown
                    }
                } catch (e: GeneralSecurityException) {
                    msgCode = R.string.err_internal
                } catch (e: IOException) {
                    msgCode = R.string.err_reading_card
                } catch (e: Exception) {
                    msgCode = R.string.err_unknown
                } finally {
                    adapter.disableReaderMode(activity)
                }

                if (msgCode != 0) {
                    requireActivity().runOnUiThread {
                        var msg = getString(msgCode)
                        if (msgArg != null)
                            msg = String.format(msg, msgArg)
                        binding.timeCounter.text = msg
                    }
                    // Gives user some time to read the error message
                    Thread.sleep(2000)
                    cancelAuth()
                }
            }
        }, NfcAdapter.FLAG_READER_NFC_A, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}