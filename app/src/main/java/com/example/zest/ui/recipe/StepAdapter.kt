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
import android.text.Editable
import android.text.TextWatcher

class StepAdapter(
    private val steps: MutableList<Step>,
    private val onListChanged: () -> Unit
) : RecyclerView.Adapter<StepAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stepPosition = view.findViewById<TextView>(R.id.stepPosition)
        val instruction = view.findViewById<EditText>(R.id.etInstruction)
        val delete = view.findViewById<ImageView>(R.id.deleteStep)
        var instructionWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.step_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = steps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = steps[position]

        holder.stepPosition.text = (position + 1).toString()
        holder.instruction.setText(step.text)

        holder.delete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                steps.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, steps.size)
                onListChanged()
            }
        }

        holder.instruction.removeTextChangedListener(holder.instructionWatcher)
        holder.instructionWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(text: Editable?) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) steps[pos].text = text.toString()
            }
        }
        holder.instruction.addTextChangedListener(holder.instructionWatcher)
    }
}