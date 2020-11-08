package com.adrian.zarza.turisty.place

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adrian.zarza.turisty.R
import com.adrian.zarza.turisty.database.PlaceDatabase
import com.adrian.zarza.turisty.databinding.FragmentPlacesBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class PlacesFragment : Fragment() {

    lateinit var placeViewModel: PlaceViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentPlacesBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_places, container, false)

        // Reference to the application context
        val application = requireNotNull(this.activity).application

        //Reference to DAO database
        val dataSource = PlaceDatabase.getInstance(application).placeDatabaseDao

        //Instance of the VMF
        val viewModelFactory = PlaceViewModelFactory(dataSource, application)

        //Reference to the VM
        placeViewModel = ViewModelProviders.of(this, viewModelFactory).get(PlaceViewModel::class.java)

        val adapter = PlaceAdapter(PlaceAdapter.PlaceListener { taskId ->
            placeViewModel.onTaskClicked(taskId)
        })

        binding.placeList.adapter = adapter

        binding.lifecycleOwner = this

        binding.viewModel = placeViewModel

        setHasOptionsMenu(true)

        val manager = LinearLayoutManager(activity)
        binding.placeList.layoutManager = manager

        placeViewModel.places.observe(viewLifecycleOwner, Observer { taskList ->
            taskList?.let {
                adapter.submitList(taskList)
            }
        })

        placeViewModel.navigateToMapsFragment.observe(viewLifecycleOwner, Observer { navigate ->
            navigate?.let {
//                this.findNavController().navigate(PlacesFragmentDirections
//                        .actionTaskFragmentToTaskDetailFragment(navigate))
                placeViewModel.onMapsFragmentNavigated()
            }
        })

        placeViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.list_empty),
                        Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                placeViewModel.doneShowingSnackbar()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> Toast.makeText(context,"Holis", Toast.LENGTH_SHORT).show()
            R.id.action_close -> {
                AuthUI.getInstance()
                        .signOut(requireContext())
                        .addOnCompleteListener { task:  Task<Void> ->
                            activity?.finish() }
            }

            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}