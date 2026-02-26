package com.arjtech.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.arjtech.weatherapp.ui.ForecastScreen
import com.arjtech.weatherapp.ui.theme.WeatherAppTheme
import com.arjtech.weatherapp.viewmodel.ForecastViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

// MainActivity is the main activity of the app. It extends ComponentActivity, which is a base class for activities in Jetpack Compose.
// This is the entry point of your app.
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient       // used to get the users current location
    private lateinit var viewModel: ForecastViewModel               // calls the ForecastViewModel that manages the weather data, User location and Api calls

    override fun onCreate(savedInstanceState: Bundle?) {            // this function starts when the app starts.
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ForecastViewModel::class.java]      // this creates our viewModal. ViewModel survives configuration changes (like screen rotation).

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)           // This prepares Google’s location system.. Now our app will request location.

        requestLocationPermission()                     // You cannot access location without permission.

        // Now the Compose UI begins.
        setContent {
            WeatherAppTheme {               // WeatherAppTheme-> applies your theme.
                ForecastScreen(viewModel)   // ForecastScreen(viewModel) → opens your weather screen
            }
        }
    }

    private fun requestLocationPermission() {           // This shows permission popup.
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),      // ACCESS_FINE_LOCATION → precise GPS
            100
        )
    }

    override fun onRequestPermissionsResult(            // This function runs after user clicks: Allow or Deny.
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // checks is this the correct request. Did user allow permission.
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()        // if Yes, then getCurrentLocation().
        }
    }

    // It tells the Android that “Don’t warn about permission — we already checked it.”
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->     // This tries to get user’s last known GPS location.
            if (location != null) {

                val lat = location.latitude
                val lon = location.longitude

                viewModel.setUserLocation(lat, lon)   // ✅ ADD THIS
            }

        }
    }
}