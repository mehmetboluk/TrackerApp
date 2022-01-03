package com.mehmetboluk.trackerapp.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mehmetboluk.trackerapp.R
import com.mehmetboluk.trackerapp.databinding.FragmentTrackingBinding
import com.mehmetboluk.trackerapp.db.Run
import com.mehmetboluk.trackerapp.other.Constants.ACTION_PAUSE_SERVICE
import com.mehmetboluk.trackerapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.mehmetboluk.trackerapp.other.Constants.ACTION_STOP_SERVICE
import com.mehmetboluk.trackerapp.other.TrackerUtils
import com.mehmetboluk.trackerapp.service.Polyline
import com.mehmetboluk.trackerapp.service.TrackingService
import com.mehmetboluk.trackerapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L
    private var menu : Menu? = null
    private var weight = 80F

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentTrackingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveDb()
        }
        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscibeToObservers()

    }

    private fun subscibeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackerUtils.getFormattedStopwatchTime(currentTimeInMillis, true)
            binding.tvTimer.setText(formattedTime)
        })
    }

    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommentToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommentToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L){
            this.menu?.get(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") {_,_ ->
                stopRun()
            }
            .setNegativeButton("No"){ dialogInterface,_ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommentToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if (!isTracking){
            binding.btnToggleRun.text = "START"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "STOP"
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    15f
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for (positiom in polyline){
                bounds.include(positiom)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveDb(){
        map?.snapshot { bmp ->
            var distanceInMeter = 0
            for(polyline in pathPoints){
                distanceInMeter += TrackerUtils.calculatePolyLineLengthDistance(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeter / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()
            val run = Run(bmp,dateTimeStamp, avgSpeed,distanceInMeter,currentTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines(){
        for(polyline in pathPoints){
            val polylineOption = PolylineOptions()
                .color(Color.RED)
                .width(8f)
                .addAll(polyline)
            map?.addPolyline(polylineOption)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastPosition = pathPoints.last()[pathPoints.last().size - 2]
            val lastPosition = pathPoints.last().last()
            val polylineOption = PolylineOptions()
                .color(Color.RED)
                .width(8f)
                .add(preLastPosition)
                .add(lastPosition)
            map?.addPolyline(polylineOption)
        }
    }

    private fun sendCommentToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }

    /**
     * GoogleMaps lifecycle
     */
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}