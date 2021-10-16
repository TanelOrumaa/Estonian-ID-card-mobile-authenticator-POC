package com.tarkvaraprojekt.mobileauthapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentHomeBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel

class HomeFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkCan(requireContext())
        viewModel.checkPin(requireContext())
        binding!!.beginButton.setOnClickListener { goToNextFragment() }
    }

    private fun goToNextFragment() {
        findNavController().navigate(R.id.action_homeFragment_to_canFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}