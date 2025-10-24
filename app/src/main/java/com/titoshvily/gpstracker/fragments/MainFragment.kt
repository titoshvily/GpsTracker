package com.titoshvily.gpstracker.fragments

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.titoshvily.gpstracker.R
import com.titoshvily.gpstracker.databinding.FragmentMainBinding
import com.titoshvily.gpstracker.utils.checkPermission
import com.titoshvily.gpstracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainFragment : Fragment() {
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    lateinit var binding: FragmentMainBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        registerPermission()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLocPermission()
    }


    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOsm() = with(binding) {
        map.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLogOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLogOverlay.enableMyLocation()
        mLogOverlay.enableFollowLocation()
        mLogOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(mLogOverlay)
        }
    }


    private fun registerPermission() {
        pLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    initOsm()
                } else {
                    showToast("Отсутствует разрешение местоположения")
                }
            }
    }


    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10() {

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOsm()

        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }


    private fun checkPermissionBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOsm()
        } else {
            pLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = MainFragment()
    }
}