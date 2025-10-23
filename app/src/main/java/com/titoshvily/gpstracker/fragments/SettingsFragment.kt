package com.titoshvily.gpstracker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.titoshvily.gpstracker.R
import com.titoshvily.gpstracker.databinding.FragmentMainBinding
import com.titoshvily.gpstracker.databinding.SettingsBinding
import com.titoshvily.gpstracker.databinding.ViewTrackBinding


class SettingsFragment : Fragment() {
    lateinit var binding: SettingsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}