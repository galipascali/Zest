package com.example.zest.ui.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.databinding.StepDetailItemBinding
import com.example.zest.model.Step

class StepDetailAdapter(private val steps: List<Step>) :
    RecyclerView.Adapter<StepDetailAdapter.ViewHolder>() {

    class ViewHolder(val binding: StepDetailItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StepDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = steps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = steps[position]
        holder.binding.tvStepNumber.text = (position + 1).toString()
        holder.binding.tvInstruction.text = step.text.replace("\n", " ")
    }
}