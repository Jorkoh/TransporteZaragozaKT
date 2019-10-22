package com.jorkoh.transportezaragozakt.destinations.search

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.hideKeyboard
import com.jorkoh.transportezaragozakt.destinations.map.MapFragment.Companion.ZARAGOZA_BOUNDS
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsFragmentArgs
import com.jorkoh.transportezaragozakt.destinations.toLatLng
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.search_destination_nearby_stops.*
import kotlinx.android.synthetic.main.search_destination_nearby_stops.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NearbyStopsFragment : Fragment() {

    private val searchVM: SearchViewModel by sharedViewModel()

    private var fusedLocationClient: FusedLocationProviderClient? = null

    // Avoid leaks
    private val locationCallback = WeakLocationCallback(object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult?.let {
                for (location in locationResult.locations) {
                    searchVM.position.postValue(location)
                }
            }
        }
    })

    private val openStop: (StopDetailsFragmentArgs) -> Unit = { info ->
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            activity?.currentFocus?.hideKeyboard()
            findNavController().navigate(
                SearchFragmentDirections.actionSearchToStopDetails(
                    info.stopType,
                    info.stopId
                )
            )
        }
    }

    private val nearbyStopsAdapter = StopWithDistanceAdapter(openStop)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchVM.nearbyStops.observe(viewLifecycleOwner, Observer { nearbyStops ->
            updateEmptyViewVisibility(nearbyStops.isEmpty())
            nearbyStopsAdapter.setNewStops(nearbyStops)
            nearbyStopsAdapter.filter.filter(searchVM.query.value)
        })
        searchVM.query.observe(viewLifecycleOwner, Observer { query ->
            nearbyStopsAdapter.filter.filter(query) { flag ->
                // If the list went from actually filtered to initial state scroll back up to the top
                if (query == "" && flag == 1) {
                    (view?.search_recycler_view_nearby_stops?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                        0,
                        0
                    )
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.search_destination_nearby_stops, container, false)

        rootView.search_recycler_view_nearby_stops.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = nearbyStopsAdapter
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        setupLocationStuff()
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationStuff() {
        runWithPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            options = QuickPermissionsOptions(
                handleRationale = true,
                rationaleMessage = getString(R.string.location_rationale),
                handlePermanentlyDenied = false,
                permissionsDeniedMethod = {
                    updateEmptyViewVisibility(true)
                }
            )
        ) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            // Since this is only active while the nearby stops viewpager is on the screen it shouldn't be too heavy on the battery
            fusedLocationClient?.requestLocationUpdates(
                LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(20)
                    .setSmallestDisplacement(15f),
                locationCallback,
                null
            )
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                if (location != null && ZARAGOZA_BOUNDS.contains(location.toLatLng())) {
                    searchVM.position.postValue(location)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        val newVisibility = if (isEmpty) {
            View.VISIBLE
        } else {
            View.GONE
        }
        view?.no_search_result_animation_nearby_stops?.visibility = newVisibility
        view?.no_search_result_text_nearby_stops?.visibility = newVisibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid leaks
        search_recycler_view_nearby_stops?.adapter = null
    }
}