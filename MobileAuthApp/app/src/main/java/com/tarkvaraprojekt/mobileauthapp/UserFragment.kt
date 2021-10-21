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
 * Currently needed in order to test that the app is working and information is read
 * from the ID card via NFC.
 */
class UserFragment : Fragment() {

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
        displayInformation()
        binding!!.clearButton.setOnClickListener { goToTheStart() }
    }

    /**
     * Assigns text values to the fields in order to display user information.
     */
    private fun displayInformation() {
        binding!!.userName.text =
            getString(R.string.user_name, viewModel.userFirstName, viewModel.userLastName)
        binding!!.identificationNumber.text = viewModel.userIdentificationNumber
        binding!!.gender.text = viewModel.gender
        binding!!.expiration.text = viewModel.expiration.replace(" ", "/")
        binding!!.citizenship.text = viewModel.citizenship
    }

    /**
     * Navigates user back to the start and also deletes any temporary information.
     */
    private fun goToTheStart() {
        viewModel.clearUserInfo()
        findNavController().navigate(R.id.action_userFragment_to_homeFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}