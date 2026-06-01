package com.example.todolist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.model.TaskData

class TaskAdapter(
    private var taskList: List<TaskData>,
    private val onFlagColorChanged: (TaskData, String) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val imgFlag: ImageView = itemView.findViewById(R.id.imgFlag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // Menggunakan String Template untuk mencegah error Type Mismatch
        holder.tvTitle.text = "${task.title}"

        // Ambil warna flag dari data model
        val colorHex = task.flagColor ?: "#BDBDBD"
        holder.imgFlag.setColorFilter(Color.parseColor(colorHex))

        holder.imgFlag.setOnClickListener { view ->
            showFlagPopup(view, holder.imgFlag, task)
        }
    }

    private fun showFlagPopup(anchorView: View, targetImageView: ImageView, task: TaskData) {
        val inflater = LayoutInflater.from(anchorView.context)
        val popupView = inflater.inflate(R.layout.layout_popup_flag, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

        fun updateColor(hexColor: String) {
            targetImageView.setColorFilter(Color.parseColor(hexColor))
            onFlagColorChanged(task, hexColor)
            popupWindow.dismiss()
        }

        popupView.findViewById<ImageView>(R.id.flagRed).setOnClickListener { updateColor("#FF5252") }
        popupView.findViewById<ImageView>(R.id.flagYellow).setOnClickListener { updateColor("#FFCA28") }
        popupView.findViewById<ImageView>(R.id.flagPurple).setOnClickListener { updateColor("#AB47BC") }
        popupView.findViewById<ImageView>(R.id.flagBlue).setOnClickListener { updateColor("#42A5F5") }
        popupView.findViewById<ImageView>(R.id.flagGreen).setOnClickListener { updateColor("#66BB6A") }

        popupWindow.showAsDropDown(anchorView, 0, 10)
    }

    override fun getItemCount(): Int = taskList.size

    fun updateData(newData: List<TaskData>) {
        taskList = newData
        notifyDataSetChanged()
    }
}