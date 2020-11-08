package com.adrian.zarza.turisty.placeDetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.adrian.zarza.turisty.R
import com.adrian.zarza.turisty.database.PlaceDatabase
import com.adrian.zarza.turisty.databinding.FragmentDetailPlaceBinding
import com.google.android.material.snackbar.Snackbar

class PlaceDetailFragment : Fragment() {

    private lateinit var placeDetailViewModel: PlaceDetailViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentDetailPlaceBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_detail_place, container, false)

        val application = requireNotNull(this.activity).application
        //val arguments = PlaceDetailFragmentArgs.fromBundle(requireArguments())

        val dataSource = PlaceDatabase.getInstance(application).placeDatabaseDao
        val viewModelFactory = PlaceDetailViewModelFactory(0L, dataSource)//arguments.placeKey

        placeDetailViewModel = ViewModelProviders.of(this, viewModelFactory).get(PlaceDetailViewModel::class.java)

        binding.viewModel = placeDetailViewModel

        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        placeDetailViewModel.navigateToPlaceFragment.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
//                this.findNavController().navigate(
//                    TaskDetailFragmentDirections.actionTaskDetailToTaskFragment())
                placeDetailViewModel.doneNavigating()
            }
        })

        placeDetailViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.warning_empty),
                    Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                placeDetailViewModel.doneShowingSnackbar()
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.eliminar -> placeDetailViewModel.onClearItem()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

}