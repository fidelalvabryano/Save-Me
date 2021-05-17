package com.harasio.savemeapp.ui.location


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.internal.impl.net.pablo.PlaceResult
import com.google.android.libraries.places.internal.it


import com.harasio.savemeapp.databinding.FragmentLocationBinding


class LocationFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private val LOCATION_PERMISSION_REQUEST = 1
    lateinit var googleMap: GoogleMap
    private lateinit var currlocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()

        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.mapView.getMapAsync(this)
        currlocation = Location("dummyprovider")
        fusedLocationProviderClient = activity?.let {
            LocationServices.getFusedLocationProviderClient(
                    it
            )
        }!!





        return root
    }




    private fun getLocationAccess() {
        if (context?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled()
            fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location :Location ->
                        if(location != null)
                        {
                            currlocation = location
                            var latLng : LatLng = LatLng(currlocation.latitude,currlocation.longitude)
                            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16F))
                        }

                    }
        }
        else
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST) }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (context?.let {
                            ActivityCompat.checkSelfPermission(
                                    it,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        } != PackageManager.PERMISSION_GRANTED && context?.let {
                            ActivityCompat.checkSelfPermission(
                                    it,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        } != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled()
            }
            else {
                Toast.makeText(context, "User has not granted location access permission", Toast.LENGTH_LONG).show()
                activity?.finish()
            }
        }
    }







    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null

    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }



    override fun onMapReady(map : GoogleMap) {

        map.let{
            googleMap = it
        }

        getLocationAccess()

    }

}
