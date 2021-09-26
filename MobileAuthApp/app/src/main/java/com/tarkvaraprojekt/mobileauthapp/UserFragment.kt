package com.tarkvaraprojekt.mobileauthapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentUserBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

/**
 * Fragment that is used to display the persons name and national identification number.
 * Currently needed in order to test that the app is working because the results at the moment
 * are not sent to some other website or app.
 */
class UserFragment : Fragment() {

    private val TAG = UserFragment::class.java.name

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentUserBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.firstName.text = getString(R.string.info_text, "First name", viewModel.userFirstName)
        binding!!.lastName.text = getString(R.string.info_text, "Last name", viewModel.userLastName)
        binding!!.identificationNumber.text = getString(R.string.info_text, "Idenfitication number", viewModel.userIdentificationNumber)
        binding!!.clearButton.setOnClickListener { goToTheStart() }
    }

    private fun goToTheStart() {
        viewModel.clearUserInfo()
        Log.i(TAG, "First name value after clearUserInfo ${viewModel.userFirstName}")
        findNavController().navigate(R.id.action_userFragment_to_homeFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}