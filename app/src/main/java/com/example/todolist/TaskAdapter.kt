package com.example.todolist

import android.graphics.Color
import android.os.Handler
import android.os.Looper
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
    private val onFlagColorChanged: (TaskData, String) -> Unit,
    private val onTaskCompleted: (TaskData, Int) -> Unit // WAJIB ADA: Jembatan untuk menghapus task
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvTaskDate) // TAMBAHAN BARU
        val imgFlag: ImageView = itemView.findViewById(R.id.imgFlag)
        val imgCheckbox: ImageView = itemView.findViewById(R.id.imgCheckbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        holder.tvTitle.text = "${task.title}"
        if (!task.deadline.isNullOrEmpty()) {
            try {
                // Asumsi format dari Laravel adalah YYYY-MM-DD
                val parts = task.deadline.split("-")
                if (parts.size >= 3) {
                    val year = parts[0]
                    val month = parts[1]
                    val day = parts[2]
                    // Ambil 2 digit pertama dari day saja (jaga-jaga kalau ada jam/waktu di belakangnya)
                    val cleanDay = day.substring(0, 2)

                    holder.tvDate.text = "$cleanDay-$month"
                    holder.tvDate.visibility = View.VISIBLE
                } else {
                    holder.tvDate.visibility = View.GONE
                }
            } catch (e: Exception) {
                holder.tvDate.visibility = View.GONE
            }
        } else {
            // Jika tidak ada deadline (misalnya tugas dibuat tanpa memilih kalender)
            holder.tvDate.visibility = View.GONE
        }
        val colorHex = task.flagColor ?: "#BDBDBD"
        holder.imgFlag.setColorFilter(Color.parseColor(colorHex))

        holder.imgFlag.setOnClickListener { view ->
            showFlagPopup(view, holder.imgFlag, task)
        }

        // --- LOGIKA TAMPILAN CHECKBOX ---
        if (task.is_completed == 1) {
            holder.imgCheckbox.setImageResource(R.drawable.complete)
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTitle.setTextColor(Color.parseColor("#A0A0A0"))
            holder.imgCheckbox.isEnabled = false
        } else {
            holder.imgCheckbox.setImageResource(R.drawable.ic_circle_outline)
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTitle.setTextColor(Color.parseColor("#333333"))
            holder.imgCheckbox.isEnabled = true

            // Efek animasi 1 detik sebelum hilang
            holder.imgCheckbox.setOnClickListener {
                holder.imgCheckbox.setImageResource(R.drawable.complete)
                holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                holder.tvTitle.setTextColor(Color.parseColor("#A0A0A0"))

                Handler(Looper.getMainLooper()).postDelayed({
                    onTaskCompleted(task, holder.adapterPosition)
                }, 1000)
            }
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

    fun removeTask(position: Int) {
        if (position in taskList.indices) {
            val mutableList = taskList.toMutableList()
            mutableList.removeAt(position)
            taskList = mutableList
            notifyItemRemoved(position)
            // Fix bug index bergeser di RecyclerView
            notifyItemRangeChanged(position, taskList.size)
        }
    }
}