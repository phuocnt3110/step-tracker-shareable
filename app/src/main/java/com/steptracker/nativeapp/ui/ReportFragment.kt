package com.steptracker.nativeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.data.DailyData
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.launch

class ReportFragment : Fragment() {
    private lateinit var viewModel: ReportViewModel
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var tvTotalSteps: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalCalories: TextView
    private lateinit var tvDailyAverage: TextView
    private lateinit var btnDay: com.google.android.material.button.MaterialButton
    private lateinit var btnWeek: com.google.android.material.button.MaterialButton
    private lateinit var btnMonth: com.google.android.material.button.MaterialButton
    private lateinit var btnYear: com.google.android.material.button.MaterialButton
    
    private var currentPeriod = "week"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = DataRepository(requireContext())
        viewModel = ViewModelProvider(this, ReportViewModel.Factory(repo))[ReportViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        barChart = view.findViewById(R.id.barChart)
        lineChart = view.findViewById(R.id.lineChart)
        tvTotalSteps = view.findViewById(R.id.tvTotalSteps)
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance)
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories)
        tvDailyAverage = view.findViewById(R.id.tvDailyAverage)
        btnDay = view.findViewById(R.id.btnDay)
        btnWeek = view.findViewById(R.id.btnWeek)
        btnMonth = view.findViewById(R.id.btnMonth)
        btnYear = view.findViewById(R.id.btnYear)
        
        setupPeriodButtons()
        setupCharts()
        observeData()
    }
    
    private fun setupPeriodButtons() {
        btnDay.setOnClickListener { selectPeriod("day") }
        btnWeek.setOnClickListener { selectPeriod("week") }
        btnMonth.setOnClickListener { selectPeriod("month") }
        btnYear.setOnClickListener { selectPeriod("year") }
    }
    
    private fun selectPeriod(period: String) {
        currentPeriod = period
        updateButtonStyles()
        viewModel.setPeriod(period)
    }
    
    private fun updateButtonStyles() {
        val buttons = mapOf(
            "day" to btnDay,
            "week" to btnWeek,
            "month" to btnMonth,
            "year" to btnYear
        )
        
        buttons.forEach { (key, btn) ->
            if (key == currentPeriod) {
                btn.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.emerald_500))
                btn.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                btn.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_100))
                btn.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_600))
            }
        }
    }
    
    private fun setupCharts() {
        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.apply {
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }
        
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            xAxis.apply {
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false
        }
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.periodData.collect { data ->
                        updateStats(data)
                        updateBarChart(data)
                    }
                }
                
                launch {
                    viewModel.periodComparison.collect { comparison ->
                        updateLineChart(comparison)
                    }
                }
            }
        }
    }
    
    private fun updateStats(data: List<DailyData>) {
        val totalSteps = data.sumOf { it.currentSteps }
        val totalDistance = data.sumOf { it.km }
        val totalCalories = data.sumOf { it.kcal }
        val avgDaily = if (data.isNotEmpty()) totalSteps / data.size else 0
        
        tvTotalSteps.text = totalSteps.toString()
        tvTotalDistance.text = String.format("%.1f km", totalDistance)
        tvTotalCalories.text = "$totalCalories kcal"
        tvDailyAverage.text = avgDaily.toString()
    }
    
    private fun updateBarChart(data: List<DailyData>) {
        val entries = data.mapIndexed { index, day ->
            BarEntry(index.toFloat(), day.currentSteps.toFloat())
        }
        
        val dataSet = BarDataSet(entries, "Steps").apply {
            color = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.emerald_500)
            setDrawValues(false)
        }
        
        barChart.data = BarData(dataSet)
        barChart.invalidate()
    }
    
    private fun updateLineChart(comparison: List<Pair<Float, Float>>) {
        val currentEntries = comparison.mapIndexed { index, data ->
            Entry(index.toFloat(), data.first)
        }
        
        val previousEntries = comparison.mapIndexed { index, data ->
            Entry(index.toFloat(), data.second)
        }
        
        val currentDataSet = LineDataSet(currentEntries, "This Month").apply {
            color = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.emerald_500)
            setDrawCircles(true)
            lineWidth = 2f
        }
        
        val previousDataSet = LineDataSet(previousEntries, "Last Month").apply {
            color = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_400)
            enableDashedLine(10f, 5f, 0f)
            setDrawCircles(false)
            lineWidth = 2f
        }
        
        lineChart.data = LineData(currentDataSet, previousDataSet)
        lineChart.invalidate()
    }
}
