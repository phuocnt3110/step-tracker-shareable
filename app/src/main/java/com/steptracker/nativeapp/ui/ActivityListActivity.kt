package com.steptracker.nativeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.data.ActivityRecord
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.launch

class ActivityListActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var repository: DataRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        
        repository = DataRepository(this)
        
        setupToolbar()
        setupRecyclerView()
        loadActivities()

        // Register back press callback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
    
    private fun setupToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            supportActionBar?.title = getString(R.string.recent_activities)
        }
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadActivities() {
        lifecycleScope.launch {
            repository.getActivitiesSync().let { activities ->
                recyclerView.adapter = ActivityListAdapter(activities) { activityId ->
                    ActivityDetailBottomSheet.newInstance(activityId)
                        .show(supportFragmentManager, "activity_detail")
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

class ActivityListAdapter(
    private val activities: List<ActivityRecord>,
    private val onItemClick: ((Long) -> Unit)? = null
) : RecyclerView.Adapter<ActivityListAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivActivityIcon: ImageView = view.findViewById(R.id.ivActivityIcon)
        val tvActivityType: TextView = view.findViewById(R.id.tvActivityType)
        val tvActivityDate: TextView = view.findViewById(R.id.tvActivityDate)
        val tvActivitySteps: TextView = view.findViewById(R.id.tvActivitySteps)
        val tvActivityCalories: TextView = view.findViewById(R.id.tvActivityCalories)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val activity = activities[position]
        holder.tvActivityType.text = activity.type.replaceFirstChar { it.uppercase() }
        holder.tvActivityDate.text = "${activity.date}, ${activity.startTime?.toLocalTime()}"
        holder.tvActivitySteps.text = context.getString(R.string.steps_format, activity.steps)
        holder.tvActivityCalories.text = context.getString(R.string.kcal_km_format, activity.kcal, activity.km)
        
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(activity.id)
        }
    }
    
    override fun getItemCount() = activities.size
}
