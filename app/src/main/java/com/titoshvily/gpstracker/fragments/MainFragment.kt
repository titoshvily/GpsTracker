package com.titoshvily.gpstracker.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.titoshvily.gpstracker.databinding.FragmentMainBinding
import com.titoshvily.gpstracker.utils.DialogManager
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var requestLocationLauncher: ActivityResultLauncher<Array<String>>

    // --- Жизненный цикл фрагмента ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerPermissionLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissionLauncher()
        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    // --- Настройка OSM ---

    private fun settingsOsm() {
        Configuration.getInstance().load(
            requireContext(),
            requireActivity().getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOsm() = with(binding) {

        map.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
                map.overlays.clear()
                map.overlays.add(mLocOverlay)
        }

    }


    private fun registerPermissionLauncher() {
        requestLocationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                // Проверяем, было ли дано основное разрешение
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    // Разрешение получено, теперь проверяем GPS и инициализируем карту
                    checkGpsAndInitMap()
                } else {
                    // Разрешение не было дано. Показываем объяснение.
                    showPermissionExplanationDialog("Для работы карты необходимо разрешение на геолокацию.")
                }
            }
    }


    private fun checkLocationPermission() {

        when {
            isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                checkGpsAndInitMap()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionExplanationDialog("Для работы карты нужно разрешение, без него никак.")
            }
            else -> {
                requestLocationLauncher.launch(getPermissionsToRequest())
            }
        }
    }


    private fun checkGpsAndInitMap() {
        if (isLocationEnabled()) {
            initOsm()
        } else {
            DialogManager.showLocEnableDialog(requireActivity() as AppCompatActivity, object :
                DialogManager.Listener{
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }




    private fun getPermissionsToRequest(): Array<String> {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        return permissions.toTypedArray()
    }

    // Проверяет, дано ли конкретное разрешение
    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    // Проверяет, включен ли GPS
    private fun isLocationEnabled(): Boolean {
        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    // Показывает диалог с объяснением и кнопкой "Настройки"
    private fun showPermissionExplanationDialog(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Настройки") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .show()
    }



    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}
