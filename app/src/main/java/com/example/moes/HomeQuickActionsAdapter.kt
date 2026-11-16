package com.example.moes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeQuickActionsAdapter(
    private val onActionClick: (QuickAction) -> Unit
) : RecyclerView.Adapter<HomeQuickActionsAdapter.ViewHolder>() {

    private var items = listOf<QuickAction>()

    fun submitList(newItems: List<QuickAction>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quick_action_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivActionIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvActionTitle)

        fun bind(action: QuickAction) {
            tvTitle.text = action.title
            ivIcon.setImageResource(action.iconResId)
            itemView.setOnClickListener { onActionClick(action) }
        }
    }
}
