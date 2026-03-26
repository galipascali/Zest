package com.example.zest.ui.recipe

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zest.databinding.StepItemBinding
import com.example.zest.model.Step

class StepAdapter(
    private val steps: MutableList<Step>,
    private val onListChanged: () -> Unit
) : RecyclerView.Adapter<StepAdapter.ViewHolder>() {

    class ViewHolder(val binding: StepItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var instructionWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StepItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = steps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = steps[position]
        val b = holder.binding

        b.stepPosition.text = (position + 1).toString()
        b.etInstruction.setText(step.text)

        b.deleteStep.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                steps.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, steps.size)
                onListChanged()
            }
        }

        b.etInstruction.removeTextChangedListener(holder.instructionWatcher)
        holder.instructionWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) { steps[pos].text = text.toString(); onListChanged() }
            }
        }
        b.etInstruction.addTextChangedListener(holder.instructionWatcher)
    }
}