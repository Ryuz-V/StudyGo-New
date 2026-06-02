package com.example.todolist

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.FragmentHomeBinding
import com.example.todolist.model.TaskData
import com.example.todolist.model.TaskRequest
import com.example.todolist.model.TaskResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // INISIALISASI ADAPTER YANG BENAR (Mencegah Layar Blank!)
        taskAdapter = TaskAdapter(
            emptyList(),
            onFlagColorChanged = { task, newColor ->
                updateTaskColorToServer(task.id, newColor)
            },
            onTaskCompleted = { task, position ->
                // Hapus dari layar
                taskAdapter.removeTask(position)

                // Laporkan ke Laravel
                completeTaskOnServer(task.id)

                // Jika list habis, tampilkan gambar kosong
                if (taskAdapter.itemCount == 0) {
                    binding.rvTasks.visibility = View.GONE
                    binding.imgEmpty.visibility = View.VISIBLE
                    binding.tvEmptyTitle.visibility = View.VISIBLE
                    binding.tvEmptySubtitle.visibility = View.VISIBLE
                }
            }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        binding.fabAdd.setOnClickListener {
            showAddTaskDialog()
        }

        try {
            val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_anim)
            binding.pulseRing.startAnimation(pulseAnimation)
        } catch (e: Exception) {
            Log.e("ANIMATION_ERROR", "Animasi pulse_anim tidak ditemukan")
        }

        fetchTasks()
    }

    private fun fetchTasks() {
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        if (token.isNullOrEmpty()) return

        val api = RetrofitClient.instance

        api.getTasks("Bearer $token").enqueue(object : Callback<List<TaskData>> {
            override fun onResponse(call: Call<List<TaskData>>, response: Response<List<TaskData>>) {
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()

                    if (tasks.isNotEmpty()) {
                        binding.rvTasks.visibility = View.VISIBLE
                        binding.imgEmpty.visibility = View.GONE
                        binding.tvEmptyTitle.visibility = View.GONE
                        binding.tvEmptySubtitle.visibility = View.GONE

                        taskAdapter.updateData(tasks)
                    } else {
                        binding.rvTasks.visibility = View.GONE
                        binding.imgEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTitle.visibility = View.VISIBLE
                        binding.tvEmptySubtitle.visibility = View.VISIBLE
                    }
                }
            }
            override fun onFailure(call: Call<List<TaskData>>, t: Throwable) {
                Log.e("FETCH_TASKS", "Gagal: ${t.message}")
            }
        })
    }

    private fun showAddTaskDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_task, null, false)
        bottomSheetDialog.setContentView(view)

        val etTitle = view.findViewById<EditText>(R.id.etTaskTitle)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val btnCalendar = view.findViewById<ImageView>(R.id.btnCalendar)
        val btnSubmit = view.findViewById<FloatingActionButton>(R.id.btnSubmitTask)

        var selectedDate = ""

        tvCategory.setOnClickListener { v ->
            val popupMenu = PopupMenu(requireContext(), v)
            popupMenu.menu.add("Matematika")
            popupMenu.menu.add("IPA")
            popupMenu.menu.add("Sejarah")
            popupMenu.menu.add("Bahasa Indonesia")
            popupMenu.menu.add("Pemrograman Web")

            popupMenu.setOnMenuItemClickListener { menuItem ->
                tvCategory.text = menuItem.title
                true
            }
            popupMenu.show()
        }

        btnCalendar.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonth + 1)
                val formattedDay = String.format(Locale.getDefault(), "%02d", selectedDay)
                selectedDate = "$selectedYear-$formattedMonth-$formattedDay"

                btnCalendar.setColorFilter(Color.parseColor("#4285F4"))
            }, year, month, day)
            datePickerDialog.show()
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Apa rencana hari ini?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.alpha = 0.5f

            val taskData = TaskRequest(
                category = tvCategory.text.toString(),
                title = title,
                description = "",
                deadline = selectedDate,
                status = "Pending",
                priority = "Medium"
            )

            val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
            val token = sharedPref.getString("token", "") ?: ""

            RetrofitClient.instance.sendTaskData("Bearer $token", taskData).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Task berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        bottomSheetDialog.dismiss()
                        fetchTasks() // Refresh list otomatis
                    } else {
                        btnSubmit.isEnabled = true
                        btnSubmit.alpha = 1.0f
                    }
                }
                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    btnSubmit.isEnabled = true
                    btnSubmit.alpha = 1.0f
                }
            })
        }
        bottomSheetDialog.show()
    }

    private fun updateTaskColorToServer(taskId: Int, newColor: String) {
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""
        if (token.isEmpty()) return

        RetrofitClient.instance.updateTaskColor("Bearer $token", taskId, newColor).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {}
            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {}
        })
    }

    private fun completeTaskOnServer(taskId: Int) {
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""
        if (token.isEmpty()) return

        RetrofitClient.instance.completeTask("Bearer $token", taskId, 1).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Tugas selesai!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}