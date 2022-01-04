package com.mehmetboluk.trackerapp.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mehmetboluk.trackerapp.R
import com.mehmetboluk.trackerapp.databinding.FragmentSettingsBinding
import com.mehmetboluk.trackerapp.databinding.FragmentSetupBinding
import com.mehmetboluk.trackerapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.mehmetboluk.trackerapp.other.Constants.KEY_NAME
import com.mehmetboluk.trackerapp.other.Constants.KEY_WEIGHT
import com.mehmetboluk.trackerapp.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadsFieldsFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Changes were saved", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please enter all input data", Snackbar.LENGTH_LONG).show()
            }
        }

    }

    private fun loadsFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()
        val toolbarText = "Let's go $nameText!"
        (activity as MainActivity).binding.tvToolbarTitle.text = toolbarText
        return true
    }
}