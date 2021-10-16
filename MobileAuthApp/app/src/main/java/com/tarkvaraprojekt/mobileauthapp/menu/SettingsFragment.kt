package com.tarkvaraprojekt.mobileauthapp.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tarkvaraprojekt.mobileauthapp.R
import com.tarkvaraprojekt.mobileauthapp.databinding.FragmentSettingsBinding
import com.tarkvaraprojekt.mobileauthapp.model.SmartCardViewModel


// Currently CAN is not actually saved, only UI part is implemented
class SettingsFragment : Fragment() {

    private val viewModel: SmartCardViewModel by activityViewModels()

    private var binding: FragmentSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.userCan.length == 6) {
            binding!!.canSaved.text = getString(R.string.saved_can, viewModel.userCan)
            binding!!.canMenuAction.text = getString(R.string.can_delete)
        } else {
            binding!!.canSaved.text = getString(R.string.saved_can, getString(R.string.missing))
            binding!!.canMenuAction.text = getString(R.string.can_add)
        }
        if (viewModel.userPin.length in 4..12) {
            binding!!.pinSaved.text = getString(R.string.saved_pin, viewModel.userPin)
            binding!!.pinMenuAction.text = getString(R.string.pin1_delete)
        } else {
            binding!!.pinSaved.text = getString(R.string.saved_pin, getString(R.string.missing))
            binding!!.pinMenuAction.text = getString(R.string.pin1_add)
        }
        binding!!.canMenuAction.setOnClickListener {
            if (viewModel.userCan.length != 6) {
                val action = SettingsFragmentDirections.actionSettingsFragmentToCanFragment(true)
                findNavController().navigate(action)
            } else {
                // If can in ViewModel is 6 we know that we can only delete it.
                viewModel.deleteCan(requireContext())
                binding!!.canSaved.text = getString(R.string.saved_can, "puudub")
                binding!!.canMenuAction.text = getString(R.string.can_add)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}