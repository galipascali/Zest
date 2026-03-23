package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Step

class StepDetailAdapter(private val steps: List<Step>) :
    RecyclerView.Adapter<StepDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.tvStepNumber)
        val instruction: TextView = view.findViewById(R.id.tvInstruction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.step_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = steps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = steps[position]
        holder.number.text = (position + 1).toString()
        holder.instruction.text = step.text.replace("\n", " ")
    }
}
