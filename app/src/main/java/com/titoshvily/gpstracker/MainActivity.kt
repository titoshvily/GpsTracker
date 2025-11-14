package com.titoshvily.gpstracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.titoshvily.gpstracker.databinding.ActivityMainBinding
import com.titoshvily.gpstracker.fragments.MainFragment
import com.titoshvily.gpstracker.fragments.SettingsFragment
import com.titoshvily.gpstracker.fragments.TracksFragment
import com.titoshvily.gpstracker.utils.openFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBottomNavClicks()
        openFragment(MainFragment.newInstance())

    }


    private fun onBottomNavClicks(){
        binding.bNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> openFragment(MainFragment.newInstance())
                R.id.tracks -> openFragment(TracksFragment.newInstance())
                R.id.settings -> openFragment(SettingsFragment())
            }

            true
        }
    }

}