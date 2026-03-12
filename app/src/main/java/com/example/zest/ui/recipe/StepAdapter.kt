package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.R
import com.example.zest.model.Step

class StepAdapter(
    private val steps: MutableList<Step>,
    private val onListChanged: () -> Unit
) : RecyclerView.Adapter<StepAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stepPosition = view.findViewById<TextView>(R.id.stepPosition)
        val instruction = view.findViewById<EditText>(R.id.etInstruction)
        val delete = view.findViewById<ImageView>(R.id.deleteStep)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.step_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = steps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = steps[position]
        val pos = holder.adapterPosition

        holder.stepPosition.text = (position + 1).toString()
        holder.instruction.setText(step.text)

        holder.delete.setOnClickListener {
            if (pos != RecyclerView.NO_POSITION) {
                steps.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, steps.size)
                onListChanged()
            }
        }
    }
}