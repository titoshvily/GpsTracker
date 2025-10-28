package com.titoshvily.gpstracker.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.titoshvily.gpstracker.MainViewModel
import com.titoshvily.gpstracker.R
import com.titoshvily.gpstracker.databinding.FragmentMainBinding
import com.titoshvily.gpstracker.location.LocationModel
import com.titoshvily.gpstracker.location.LocationService
import com.titoshvily.gpstracker.utils.DialogManager
import com.titoshvily.gpstracker.utils.TimeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.Distance
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask

class MainFragment : Fragment() {
    private var timer: Timer? = null
    private var startTime = 0L

    private var isServiceRunning = false
    private lateinit var binding: FragmentMainBinding
    private lateinit var requestLocationLauncher: ActivityResultLauncher<Array<String>>
    private val model : MainViewModel by activityViewModels()


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
        setOnClicks()
        checkLocationPermission()
        checkServiceState()
        updateTime()
        registerLocReceiver()
        locationUpdates()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }

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

                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {

                    checkGpsAndInitMap()

                } else {

                    showPermissionExplanationDialog("Для работы карты необходимо разрешение на геолокацию.")
                }
            }
    }

    private fun setOnClicks() = with(binding) {
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)
    }

    private fun updateTime(){
        model.timeData.observe(viewLifecycleOwner){
            binding.tvTime.text = it
        }
    }

    private fun startTimer(){
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object : TimerTask(){
            override fun run() {
                activity?.runOnUiThread {
                    model.timeData.value = getCurrentTime()
                }
            }

        }, 1000, 1000)
    }


    @SuppressLint("DefaultLocale")
    private fun getAverageSpeed(distance: Float): Float {
        if (startTime == 0L) return 0f

        val timeInSeconds = (System.currentTimeMillis() - startTime) / 1000f
        if (timeInSeconds == 0f) return 0f

        val avgSpeedMs = distance / timeInSeconds

        return avgSpeedMs * 3.6f
    }


    private fun getCurrentTime(): String{
        return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }



    private fun startStopService() {
        if (!isServiceRunning) {
            startLocationService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_play)
            timer?.cancel()
        }
        isServiceRunning = !isServiceRunning

    }

    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener {
            when (it.id) {
                R.id.fStartStop -> {
                    startStopService()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun locationUpdates() = with(binding){
        model.locationUpdates.observe(viewLifecycleOwner){
            val distance = "Distance: ${String.format("%.1f", it.distance)} m"

            val instantSpeedKmh = it.velocity * 3.6f
            val velocity = "Speed: ${String.format("%.1f", instantSpeedKmh)} km/h"

            val avgSpeedKmh = getAverageSpeed(it.distance)
            val avgVelocity = "Average Speed: ${String.format("%.1f", avgSpeedKmh)} km/h"

            tvDistance.text = distance
            tvSpeed.text = velocity
            tvAvgSpeed.text = avgVelocity

            Log.d("SpeedDebug", "Instant: $instantSpeedKmh km/h, Avg: $avgSpeedKmh km/h")

        }
    }

    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }

    private fun startLocationService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()
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
                DialogManager.Listener {
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
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
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



    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, i: Intent?) {
            if (i?.action == LocationService.LOC_MODEL_INTENT){
                val locModel = i.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
                model.locationUpdates.value = locModel
            }
        }

    }

    private fun registerLocReceiver(){
        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        ContextCompat.registerReceiver(
            requireActivity(),
            receiver,
            locFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}
