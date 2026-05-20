package com.steptracker.nativeapp.ui.language

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.steptracker.nativeapp.R

data class LanguageItem(
    val code: String,
    val name: String,
    val flag: String,
    var isSelected: Boolean = false
)

class LanguageAdapter(
    private val onItemClick: (LanguageItem) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private val items = mutableListOf<LanguageItem>()

    fun submitList(list: List<LanguageItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setSelected(code: String) {
        items.forEachIndexed { index, item ->
            val wasSelected = item.isSelected
            item.isSelected = item.code == code
            if (wasSelected != item.isSelected) notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView as MaterialCardView
        private val tvFlag: TextView = itemView.findViewById(R.id.tv_flag)
        private val tvLanguageName: TextView = itemView.findViewById(R.id.tv_language_name)
        private val ivRadio: ImageView = itemView.findViewById(R.id.iv_radio)

        fun bind(item: LanguageItem) {
            tvFlag.text = item.flag
            tvLanguageName.text = item.name

            ivRadio.setImageResource(
                if (item.isSelected) R.drawable.ic_radio_selected
                else R.drawable.ic_radio_unselected
            )

            val context = card.context
            card.strokeColor = if (item.isSelected) {
                context.getColor(R.color.colorPrimary)
            } else {
                context.getColor(R.color.gray_200)
            }

            card.setOnClickListener { onItemClick(item) }
        }
    }
}
