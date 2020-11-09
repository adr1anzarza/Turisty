package com.adrian.zarza.turisty.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.adrian.zarza.turisty.R
import com.adrian.zarza.turisty.database.PlaceDatabase
import com.adrian.zarza.turisty.databinding.FragmentMapsBinding
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*
import kotlin.collections.ArrayList

class MapsFragment : Fragment(), OnMapReadyCallback {

    lateinit var mapViewModel: MapsViewModel

    private var mMap: GoogleMap? = null

    // New variables for Current Place picker
    private val TAG = "MapsActivity"
    var lstPlaces: ListView? = null
    private var mPlacesClient: PlacesClient? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    // The geographical location where the device is currently located. That is, the last-known location retrieved by the Fused Location Provider.
    private var mLastKnownLocation: Location? = null

    // A default location (Sydney, Australia) and default zoom to use when location permission is not granted.
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted = false

    // Used for selecting the Current Place.
    private val M_MAX_ENTRIES = 5
    private lateinit var mLikelyPlaceNames: ArrayList<String>
    private lateinit var mLikelyPlaceAddresses: ArrayList<String>
    private lateinit var mLikelyPlaceAttributions: ArrayList<String>
    private lateinit var mLikelyPlaceLatLngs: ArrayList<LatLng>

    //Selected location
    private lateinit var mSelectedDirection: String
    private lateinit var mSelectedLatLng: LatLng
    private lateinit var bundle: Bundle

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMapsBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_maps, container, false)

        val application = requireNotNull(this.activity).application
        val arguments = MapsFragmentArgs.fromBundle(requireArguments())
        val dataSource = PlaceDatabase.getInstance(application).placeDatabaseDao
        mSelectedLatLng = mDefaultLocation
        mSelectedDirection = "Sydney"

        bundle = bundleOf("latlong" to mSelectedLatLng,"direction" to mSelectedDirection)
        //Instance of the VMF
        val viewModelFactory = MapsViewModelFactory(dataSource, application)

        //Reference to the VM
        mapViewModel = ViewModelProviders.of(this, viewModelFactory).get(MapsViewModel::class.java)

        setHasOptionsMenu(true)

        binding.lifecycleOwner = this

        binding.viewModel = mapViewModel

        val mapFragment : SupportMapFragment? =  childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Set up the views
        lstPlaces = binding.listPlaces

        // Initialize the Places client
        val apiKey = getString(R.string.google_maps_key)
        Places.initialize(requireContext(), apiKey)
        mPlacesClient = Places.createClient(requireContext())
        mFusedLocationProviderClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }

        //Observers

        mapViewModel.navigateToPlaceDetailFragment.observe(viewLifecycleOwner, Observer { navigate ->
            navigate?.let {
                this.findNavController().navigate(R.id.action_mapsFragment_to_placeDetailFragment,bundle)
                mapViewModel.onMapsFragmentNavigated()
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_menu, menu)
        return super.onCreateOptionsMenu(menu!!,inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_geolocate -> {
                pickCurrentPlace()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        //Default latlong
        mSelectedLatLng = sydney
        mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Enable the zoom controls for the map
        mMap?.uiSettings?.isZoomControlsEnabled = true
        // Prompt the user for permission.
        getLocationPermission()

        mMap?.setOnMapClickListener(OnMapClickListener { point ->
            mMap?.clear()
            val marker = MarkerOptions().position(LatLng(point.latitude, point.longitude)).title("Nueva búsqueda")
            mMap?.addMarker(marker)
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String?>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    private fun getLocationPermission() {
        mLocationPermissionGranted = false
        if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            activity?.let {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentPlaceLikelihoods() {
        val placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG)

        val request = FindCurrentPlaceRequest.builder(placeFields).build()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val placeResponse = mPlacesClient!!.findCurrentPlace(request)
        activity?.let {
            placeResponse.addOnCompleteListener(it,
                OnCompleteListener<FindCurrentPlaceResponse> { task ->
                    if (task.isSuccessful) {
                        val response = task.result
                        // Set the count, handling cases where less than 5 entries are returned.
                        val count: Int
                        count = if (response.placeLikelihoods.size < M_MAX_ENTRIES) {
                            response.placeLikelihoods.size
                        } else {
                            M_MAX_ENTRIES
                        }
                        mLikelyPlaceNames = arrayListOf()
                        mLikelyPlaceAddresses = arrayListOf()
                        mLikelyPlaceAttributions = arrayListOf()
                        mLikelyPlaceLatLngs = arrayListOf()

                        for ((i, placeLikelihood) in response.placeLikelihoods.withIndex() ) {
                            val currPlace = placeLikelihood.place
                            mLikelyPlaceNames.add(i,currPlace.name!!)//currPlace.name!!
                            mLikelyPlaceAddresses.add(i,currPlace.address!!)
                            mLikelyPlaceAttributions.add(i, (if (currPlace.attributions == null) "" else TextUtils.join(" ", currPlace.attributions!!))!!)
                            mLikelyPlaceLatLngs.add(i, currPlace.latLng!!)
                            //[i] = currPlace.latLng!!
                            val currLatLng = mLikelyPlaceLatLngs[i].toString()
                            Log.i(TAG, String.format("Place " + currPlace.name
                                    + " has likelihood: " + placeLikelihood.likelihood
                                    + " at " + currLatLng))
                            if (i > count)
                                break
                        }
                        fillPlacesList()
                    } else {
                        val exception = task.exception
                        if (exception is ApiException) {
                            Log.e(TAG, "Place not found: " + exception.statusCode)
                        }
                    }
                })
        }
    }

    private fun getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient!!.lastLocation
                activity?.let {
                    locationResult.addOnCompleteListener(it, OnCompleteListener<Location> { task ->
                        if (task.isSuccessful){
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.result
                            mLastKnownLocation?.let { location ->

                                Log.d(TAG, "Latitude: " + location?.latitude)
                                Log.d(TAG, "Longitude: " + location?.longitude)

                                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        location?.latitude?.let { latitude ->
                                            LatLng(latitude,
                                                    location.longitude)
                                        }, DEFAULT_ZOOM.toFloat()))
                                mMap!!.clear()
                                val marker = MarkerOptions()
                                        .position(LatLng(location.latitude, location.longitude))
                                        .title("You")
                                mMap!!.addMarker(marker)
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            mMap!!.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM.toFloat()))
                        }
                        getCurrentPlaceLikelihoods()
                    })
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    private fun pickCurrentPlace() {
        if (mMap == null) {
            return
        }
        if (mLocationPermissionGranted) {
            getDeviceLocation()
        } else {
            Log.i(TAG, "The user did not grant location permission.")

            mMap!!.addMarker(MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)))
            getLocationPermission()
        }
    }

    private val listClickedHandler = OnItemClickListener { parent, v, position, id -> // position will give us the index of which place was selected in the array
        val markerLatLng = mLikelyPlaceLatLngs[position]
        var markerSnippet = mLikelyPlaceAddresses[position]
        markerSnippet = """
            $markerSnippet
            ${mLikelyPlaceAttributions[position]}
            """.trimIndent()

        mMap!!.clear()
        mMap!!.addMarker(MarkerOptions()
                .title(mLikelyPlaceNames[position])
                .position(markerLatLng)
                .snippet(markerSnippet))
        Toast.makeText(context, "Title:" + mLikelyPlaceNames[position]
                + "adress: " + mLikelyPlaceAddresses[position]
                + "latlong: " + mLikelyPlaceLatLngs[position], Toast.LENGTH_SHORT).show()

        mSelectedDirection = mLikelyPlaceNames[position]
        mSelectedLatLng = mLikelyPlaceLatLngs[position]

        bundle = bundleOf("direction" to mSelectedDirection)

        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng))
    }

    private fun fillPlacesList() {
        val placesAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, mLikelyPlaceNames)
        lstPlaces!!.adapter = placesAdapter
        lstPlaces!!.onItemClickListener = listClickedHandler
    }

}