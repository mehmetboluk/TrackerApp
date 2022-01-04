package com.mehmetboluk.trackerapp.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mehmetboluk.trackerapp.R
import com.mehmetboluk.trackerapp.databinding.FragmentSetupBinding
import com.mehmetboluk.trackerapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.mehmetboluk.trackerapp.other.Constants.KEY_NAME
import com.mehmetboluk.trackerapp.other.Constants.KEY_WEIGHT
import com.mehmetboluk.trackerapp.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup){

    @Inject
    lateinit var sharedPref : SharedPreferences
    @set: Inject
    var isFirstAppOpen = true
    private lateinit var binding: FragmentSetupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen){
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment, savedInstanceState,navOption)
        }

        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(
                    requireView(), "Please enter all input data", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun writePersonalDataToSharedPref() : Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val toolbarText = "Let's go $name!"
        (activity as MainActivity).binding.tvToolbarTitle.text = toolbarText
        return true
    }
}