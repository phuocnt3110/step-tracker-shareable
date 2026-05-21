package com.steptracker.nativeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.data.Achievement
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.launch

class AchievementFragment : Fragment() {
    private lateinit var viewModel: AchievementViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCompletedPercent: TextView
    
    private var currentCategory = "all"
    private lateinit var categoryMap: Map<String, String>
    
    private fun initCategoryMap() {
        categoryMap = mapOf(
            "all" to getString(R.string.all),
            "dailySteps" to getString(R.string.daily_steps),
            "totalSteps" to getString(R.string.total_steps_ach),
            "weeklySteps" to getString(R.string.weekly_steps),
            "consecutiveDays" to getString(R.string.streaks),
            "totalDistance" to getString(R.string.distance),
            "caloriesBurned" to getString(R.string.calories),
            "activeTime" to getString(R.string.active_time),
            "special" to getString(R.string.special)
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = DataRepository(requireContext())
        viewModel = ViewModelProvider(this, AchievementViewModel.Factory(repo))[AchievementViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_achievement, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initCategoryMap()
        
        recyclerView = view.findViewById(R.id.recyclerView)
        chipGroup = view.findViewById(R.id.chipGroup)
        tvProgress = view.findViewById(R.id.tvProgress)
        progressBar = view.findViewById(R.id.progressBar)
        tvCompletedPercent = view.findViewById(R.id.tvCompletedPercent)
        
        setupRecyclerView()
        setupChips()
        observeData()
    }
    
    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
    }
    
    private fun setupChips() {
        chipGroup.isSingleSelection = true
        chipGroup.isSelectionRequired = true
        
        categoryMap.forEach { (key, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = key == "all"
                tag = key
            }
            chipGroup.addView(chip)
        }
        
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds.first())
                currentCategory = selectedChip?.tag as? String ?: "all"
                updateAchievementList()
            }
        }
    }
    
    private var allAchievements = listOf<Achievement>()
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.achievements.collect { achievements ->
                    allAchievements = achievements
                    updateAchievementList()
                    
                    val unlockedCount = achievements.count { it.unlocked }
                    val totalCount = achievements.size
                    tvProgress.text = "$unlockedCount/$totalCount ${getString(R.string.unlocked)}"
                    val percent = if (totalCount > 0) (unlockedCount * 100 / totalCount) else 0
                    progressBar.progress = percent
                    tvCompletedPercent.text = "$percent% ${getString(R.string.completed)}"
                }
            }
        }
    }
    
    private fun updateAchievementList() {
        val filtered = if (currentCategory == "all") {
            allAchievements
        } else {
            allAchievements.filter { it.category == currentCategory }
        }
        recyclerView.adapter = AchievementAdapter(filtered, this)
    }
}

class AchievementAdapter(
    private val achievements: List<Achievement>,
    private val fragment: AchievementFragment
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val tvProgress: TextView = view.findViewById(R.id.tvProgress)
        val ivLock: ImageView = view.findViewById(R.id.ivLock)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]
        val context = holder.itemView.context
        
        holder.tvTitle.text = achievement.title
        holder.tvDesc.text = achievement.description
        holder.tvProgress.text = "${achievement.current}/${achievement.threshold}"
        
        val progress = (achievement.current.toFloat() / achievement.threshold * 100).toInt()
        holder.progressBar.progress = progress.coerceIn(0, 100)
        
        // Parse color and set icon tint
        val color = try {
            android.graphics.Color.parseColor(achievement.color)
        } catch (e: Exception) {
            ContextCompat.getColor(context, R.color.emerald_500)
        }
        
        holder.ivIcon.setColorFilter(color)
        
        if (!achievement.unlocked) {
            holder.ivLock.visibility = View.VISIBLE
            holder.itemView.alpha = 0.6f
        } else {
            holder.ivLock.visibility = View.GONE
            holder.itemView.alpha = 1.0f
        }

        // Show rewarded ad when clicking unlocked achievement
        holder.itemView.setOnClickListener {
            if (achievement.unlocked) {
                showRewardedAd(achievement)
            }
        }
    }

    private fun showRewardedAd(achievement: Achievement) {
        fragment.activity?.let { activity ->
            Toast.makeText(activity, "Reward earned!", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun getItemCount() = achievements.size
}
