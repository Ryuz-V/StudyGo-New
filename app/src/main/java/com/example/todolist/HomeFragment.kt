package com.example.todolist

import android.app.DatePickerDialog
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
import com.example.todolist.model.TaskListResponse
import com.example.todolist.network.ApiService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.Locale
import android.content.Context

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    // Ubah nama variabel agar tidak ada warning "should not contain underscores"
    private val baseUrl = "http://192.168.1.8:8000/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskAdapter = TaskAdapter(emptyList())
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        binding.fabAdd.setOnClickListener {
            showAddTaskDialog()
        }

        // Pastikan R.anim.pulse_anim benar-benar ada di folder res/anim/
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
        Log.d("DEBUG_TOKEN", "Token yang dikirim: $token")

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan!", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance

        api.getTasks("Bearer $token").enqueue(object : Callback<List<TaskData>> {
            override fun onResponse(call: Call<List<TaskData>>, response: Response<List<TaskData>>) {
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()
                    Log.d("API_SUCCESS", "Data diterima: ${tasks.size} task") // Log sukses

                    if (tasks.isNotEmpty()) {
                        binding.imgEmpty.visibility = View.GONE
                        binding.rvTasks.visibility = View.VISIBLE
                        taskAdapter.updateData(tasks)
                    } else {
                        binding.rvTasks.visibility = View.GONE
                        binding.imgEmpty.visibility = View.VISIBLE
                    }
                } else {
                    // INI BAGIAN PENTING UNTUK DEBUG
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Code: ${response.code()}, Body: $errorBody")
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TaskData>>, t: Throwable) {
                Log.e("API_FAILURE", "Error: ${t.message}") // Tambahkan log untuk onFailure
                Toast.makeText(requireContext(), "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddTaskDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        // Menggunakan false untuk menghilangkan warning "Avoid passing null as the view root"
        val view = layoutInflater.inflate(R.layout.bottom_sheet_task, null, false)
        bottomSheetDialog.setContentView(view)

        val etTitle = view.findViewById<EditText>(R.id.etTaskTitle)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val btnCalendar = view.findViewById<ImageView>(R.id.btnCalendar)
        val btnSubmit = view.findViewById<FloatingActionButton>(R.id.btnSubmitTask)

        var selectedDate = ""

        // ==========================================
        // LOGIKA POP-UP KATEGORI
        // ==========================================
        tvCategory.setOnClickListener { v -> // Ubah 'it' menjadi 'v' agar lebih spesifik tipe View-nya
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

        // ==========================================
        // LOGIKA KALENDER
        // ==========================================
        btnCalendar.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Menggunakan Locale.getDefault() agar tidak muncul warning bug format
                val formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonth + 1)
                val formattedDay = String.format(Locale.getDefault(), "%02d", selectedDay)
                selectedDate = "$selectedYear-$formattedMonth-$formattedDay"

                btnCalendar.setColorFilter(Color.parseColor("#4285F4"))
            }, year, month, day)

            datePickerDialog.show()
        }

        // ==========================================
        // LOGIKA SUBMIT
        // ==========================================
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

            // AMBIL TOKEN DARI SHAREDPREFERENCES
            val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
            val token = sharedPref.getString("token", "") ?: ""

            val api = RetrofitClient.instance

            // MASUKKAN TOKEN KE DALAM PEMANGGILAN API
            api.sendTaskData("Bearer $token", taskData).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Task berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        bottomSheetDialog.dismiss()
                        fetchTasks() // Refresh list task
                    } else {
                        Toast.makeText(requireContext(), "Gagal: ${response.code()}", Toast.LENGTH_LONG).show()
                        btnSubmit.isEnabled = true
                        btnSubmit.alpha = 1.0f
                    }
                }

                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.alpha = 1.0f
                }
            })
        }

        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}