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

    // VARIABEL BARU: Untuk menyimpan semua tugas & kategori saat ini
    private var allTasks: List<TaskData> = emptyList()
    private var currentCategory: String = "Semuanya" // Atau "Wishlist" sesuai teks di XML kamu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskAdapter = TaskAdapter(
            emptyList(),
            onFlagColorChanged = { task, newColor ->
                updateTaskColorToServer(task.id, newColor)
            },
            onTaskCompleted = { task, position ->
                taskAdapter.removeTask(position)
                completeTaskOnServer(task.id)

                // Hapus juga dari allTasks agar data tetap sinkron
                val updatedList = allTasks.toMutableList()
                updatedList.removeAll { it.id == task.id }
                allTasks = updatedList

                if (taskAdapter.itemCount == 0) {
                    showEmptyState(true)
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

        // Panggil fungsi untuk mengaktifkan klik pada kategori
        setupCategoryFilters()

        fetchTasks()
    }

    // FUNGSI BARU: Mengaktifkan klik di menu kategori atas
    private fun setupCategoryFilters() {
        val categoryContainer = binding.categoryScroll.getChildAt(0) as ViewGroup

        // 1. Set warna awal saat aplikasi baru dibuka (Semuanya = Terpilih)
        for (i in 0 until categoryContainer.childCount) {
            val tv = categoryContainer.getChildAt(i) as TextView

            if (tv.text.toString() == currentCategory) {
                // STATE TERPILIH: Background transparan, Teks warna utama
                tv.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2033DBCC"))
                tv.setTextColor(Color.parseColor("#33dbcc"))
            } else {
                // STATE TIDAK TERPILIH: Background warna utama, Teks putih
                tv.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#33dbcc"))
                tv.setTextColor(Color.parseColor("#FFFFFF"))
            }

            // 2. Logika saat kategori diklik
            tv.setOnClickListener {
                // A. Kembalikan semua kategori ke State Tidak Terpilih (Warna Utama Solid)
                for (j in 0 until categoryContainer.childCount) {
                    val unselectedTv = categoryContainer.getChildAt(j) as TextView
                    unselectedTv.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#33dbcc"))
                    unselectedTv.setTextColor(Color.parseColor("#FFFFFF"))
                }

                // B. Ubah kategori yang sedang diklik menjadi State Terpilih (Transparan)
                tv.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2033DBCC"))
                tv.setTextColor(Color.parseColor("#33dbcc"))

                // C. Simpan kategori saat ini dan jalankan filter data
                currentCategory = tv.text.toString()
                filterTasks()
            }
        }
    }

    // FUNGSI BARU: Logika memfilter tugas
    private fun filterTasks() {
        val filteredList = if (currentCategory == "Semuanya" || currentCategory == "Wishlist") {
            allTasks
        } else {
            // Cocokkan nama kategori tugas dengan kategori yang diklik
            allTasks.filter { it.category?.name?.equals(currentCategory, ignoreCase = true) == true }
        }

        taskAdapter.updateData(filteredList)

        // Cek apakah hasil filter kosong
        showEmptyState(filteredList.isEmpty())
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvTasks.visibility = View.GONE
            binding.imgEmpty.visibility = View.VISIBLE
            binding.tvEmptyTitle.visibility = View.VISIBLE
            binding.tvEmptySubtitle.visibility = View.VISIBLE
        } else {
            binding.rvTasks.visibility = View.VISIBLE
            binding.imgEmpty.visibility = View.GONE
            binding.tvEmptyTitle.visibility = View.GONE
            binding.tvEmptySubtitle.visibility = View.GONE
        }
    }

    private fun fetchTasks() {
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        if (token.isNullOrEmpty()) return

        RetrofitClient.instance.getTasks("Bearer $token").enqueue(object : Callback<List<TaskData>> {
            override fun onResponse(call: Call<List<TaskData>>, response: Response<List<TaskData>>) {
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()

                    // Simpan ke allTasks, lalu jalankan filter
                    allTasks = tasks
                    filterTasks()
                }
            }
            override fun onFailure(call: Call<List<TaskData>>, t: Throwable) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
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
            popupMenu.menu.add("IPS")
            popupMenu.menu.add("Sejarah")
            popupMenu.menu.add("Informatika")
            popupMenu.menu.add("B. Inggris")
            popupMenu.menu.add("B. Indonesia")
            popupMenu.menu.add("PKN")
            popupMenu.menu.add("Agama")
            popupMenu.menu.add("Seni Budaya")
            popupMenu.menu.add("Penjas")
            popupMenu.menu.add("Lainnya")

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

            // LOGIKA DEFAULT "Lainnya"
            var selectedCatName = tvCategory.text.toString()
            if (selectedCatName == "Tidak Ada Kategori" || selectedCatName.isEmpty()) {
                selectedCatName = "Lainnya"
            }

            // Ubah nama kategori menjadi ID untuk dikirim ke Laravel
            val categoryId = getCategoryIdFromName(selectedCatName)

            val taskData = TaskRequest(
                category_id = categoryId, // MENGIRIM ID BUKAN STRING
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

    // FUNGSI BARU: Mengubah teks kategori menjadi ID Database
    private fun getCategoryIdFromName(name: String): Int {
        return when (name) {
            "Lainnya" -> 1
            "Matematika" -> 2
            "IPA" -> 3
            "IPS" -> 4
            "Sejarah" -> 5
            "Informatika" -> 6
            "B. Inggris" -> 7
            "B. Indonesia" -> 8
            "PKN" -> 9
            "Agama" -> 10
            "Seni Budaya" -> 11
            "Penjas" -> 12
            else -> 1 // Default jika tidak ketemu
        }
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
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {}
            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}