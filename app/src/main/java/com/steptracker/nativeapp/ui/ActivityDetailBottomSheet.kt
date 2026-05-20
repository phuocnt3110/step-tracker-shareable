package com.steptracker.nativeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.data.StepTrackerDatabase
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class ActivityDetailBottomSheet : BottomSheetDialogFragment() {
    
    companion object {
        private const val ARG_ACTIVITY_ID = "activity_id"
        
        fun newInstance(activityId: Long): ActivityDetailBottomSheet {
            return ActivityDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ACTIVITY_ID, activityId)
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_activity_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val activityId = arguments?.getLong(ARG_ACTIVITY_ID) ?: -1
        
        // Initialize with default zero values
        resetToDefaultValues(view)
        
        if (activityId <= 0) return
        
        lifecycleScope.launch {
            val db = StepTrackerDatabase.getDatabase(requireContext())
            val activity = db.activityDao().getById(activityId) ?: return@launch
            
            // Bind data - only update if activity exists
            view.findViewById<TextView>(R.id.tvTitle).text = activity.type.replaceFirstChar { it.uppercase() }
            view.findViewById<TextView>(R.id.tvDate).text = activity.date.toString()
            
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            view.findViewById<TextView>(R.id.tvTimeRange).text = 
                "${activity.startTime?.format(timeFormatter)} - ${activity.endTime?.format(timeFormatter)}"
            
            view.findViewById<TextView>(R.id.tvSteps).text = activity.steps.toString()
            view.findViewById<TextView>(R.id.tvCalories).text = "${activity.kcal} kcal"
            view.findViewById<TextView>(R.id.tvDistance).text = "%.2f km".format(activity.km)
            view.findViewById<TextView>(R.id.tvDuration).text = 
                activity.durationMinutes?.let { mins -> "${mins} min" } ?: "0 min"
            view.findViewById<TextView>(R.id.tvAvgSpeed).text = "%.1f km/h".format(activity.averageSpeed)
            view.findViewById<TextView>(R.id.tvMaxSpeed).text = "%.1f km/h".format(activity.highestSpeed)
            
            // View full detail button
            view.findViewById<View>(R.id.btnViewFull).setOnClickListener {
                val intent = android.content.Intent(requireContext(), ActivityDetailActivity::class.java).apply {
                    putExtra("activity_id", activity.id)
                }
                startActivity(intent)
                dismiss()
            }
        }
        
        // Close button
        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }
    
    private fun resetToDefaultValues(view: View) {
        view.findViewById<TextView>(R.id.tvTitle).text = getString(R.string.no_activity)
        view.findViewById<TextView>(R.id.tvDate).text = "--/--/----"
        view.findViewById<TextView>(R.id.tvTimeRange).text = "--:-- - --:--"
        view.findViewById<TextView>(R.id.tvSteps).text = "0"
        view.findViewById<TextView>(R.id.tvCalories).text = "0 kcal"
        view.findViewById<TextView>(R.id.tvDistance).text = "0.0 km"
        view.findViewById<TextView>(R.id.tvDuration).text = "0 min"
        view.findViewById<TextView>(R.id.tvAvgSpeed).text = "0.0 km/h"
        view.findViewById<TextView>(R.id.tvMaxSpeed).text = "0.0 km/h"
    }
}
