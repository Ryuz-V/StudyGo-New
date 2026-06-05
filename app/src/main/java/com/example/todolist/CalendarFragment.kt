package com.example.todolist

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.FragmentCalendarBinding
import com.example.todolist.model.TaskRequest
import com.example.todolist.model.TaskResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private var isWeekView = false

    // Simpan data kalender saat ini untuk navigasi bulan & tgl terpilih
    private val currentCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Tampilkan bulan & tahun saat ini di TextView
        updateHeaderBulan()

        // 2. Logika Panah GANTI BULAN KIRI (Bulan Sebelumnya)
        binding.btnBulanKiri.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1) // Reset ke tanggal 1
            binding.calendarView.date = currentCalendar.timeInMillis
            updateHeaderBulan()
        }

        // 3. Logika Panah GANTI BULAN KANAN (Bulan Selanjutnya)
        binding.btnBulanKanan.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            currentCalendar.set(Calendar.DAY_OF_MONTH, 1) // Reset ke tanggal 1
            binding.calendarView.date = currentCalendar.timeInMillis
            updateHeaderBulan()
        }

        // 4. Update teks bulan kalau user nge-swipe/klik tanggal di kalender
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            currentCalendar.set(year, month, dayOfMonth) // TANGGAL TERPILIH TERSIMPAN DI SINI
            updateHeaderBulan()
        }

        // 5. Logika Panah Toggle (Sembunyikan/Munculkan Kalender)
        binding.btnToggleCalendar.setOnClickListener {
            isWeekView = !isWeekView

            if (isWeekView) {
                binding.calendarContainer.visibility = View.GONE
                binding.weekViewLayout.visibility = View.VISIBLE
                binding.btnToggleCalendar.rotation = 180f
            } else {
                binding.calendarContainer.visibility = View.VISIBLE
                binding.weekViewLayout.visibility = View.GONE
                binding.btnToggleCalendar.rotation = 0f
            }
        }

        // 6. Jalankan Animasi Pulse pada FAB
        try {
            val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_anim)
            binding.pulseRing.startAnimation(pulseAnimation)
        } catch (e: Exception) {
            android.util.Log.e("ANIMATION_ERROR", "Animasi pulse_anim tidak ditemukan")
        }

        // ========================================================
        // 7. LOGIKA TOMBOL ADD TASK (Membawa Tanggal Kalender)
        // ========================================================
        binding.fabAdd.setOnClickListener {
            // Format tanggal yang sedang dipilih di kalender menjadi YYYY-MM-DD
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val tanggalTerpilih = sdf.format(currentCalendar.time)

            // Buka bottom sheet dan lempar tanggal tersebut
            showAddTaskDialog(tanggalTerpilih)
        }
    }

    // Fungsi otomatis untuk memformat dan menampilkan teks bulan (misal: "MEI 2026")
    private fun updateHeaderBulan() {
        // PERBAIKAN: Menggunakan Locale.Builder() sesuai standar Java terbaru
        val myLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()
        val formatBulan = SimpleDateFormat("MMMM  yyyy", myLocale)
        val teksBulan = formatBulan.format(currentCalendar.time).uppercase()
        binding.tvBulanTahun.text = teksBulan
    }

    // ========================================================
    // FUNGSI BOTTOM SHEET TASK (Diadaptasi dari HomeFragment)
    // ========================================================
    private fun showAddTaskDialog(defaultDate: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_task, null, false)
        bottomSheetDialog.setContentView(view)

        val etTitle = view.findViewById<EditText>(R.id.etTaskTitle)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val btnCalendar = view.findViewById<ImageView>(R.id.btnCalendar)
        val btnSubtask = view.findViewById<ImageView>(R.id.btnSubtask)
        val containerSubtasks = view.findViewById<LinearLayout>(R.id.containerSubtasks)
        val btnSubmit = view.findViewById<FloatingActionButton>(R.id.btnSubmitTask)

        // Otomatis isi variabel selectedDate dengan tanggal dari kalender
        var selectedDate = defaultDate

        // Nyalakan ikon kalender menjadi biru karena sudah ada tanggal yang terpilih
        btnCalendar.setColorFilter(Color.parseColor("#4285F4"))

        val subtaskListStrings = mutableListOf<String>()

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

        // User tetap bisa mengganti tanggal jika berubah pikiran
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

        // Logika memunculkan baris subtask
        btnSubtask.setOnClickListener {
            val subtaskView = layoutInflater.inflate(R.layout.item_add_subtask, null)
            val btnRemove = subtaskView.findViewById<ImageView>(R.id.btnRemoveSubtask)
            val etSubtask = subtaskView.findViewById<EditText>(R.id.etSubtaskName)

            btnRemove.setOnClickListener {
                containerSubtasks.removeView(subtaskView)
            }

            containerSubtasks.addView(subtaskView)
            etSubtask.requestFocus()
        }

        // Logika kirim ke server
        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Apa rencana hari ini?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            subtaskListStrings.clear()
            for (i in 0 until containerSubtasks.childCount) {
                val childView = containerSubtasks.getChildAt(i)
                val etSubtask = childView.findViewById<EditText>(R.id.etSubtaskName)
                val subtaskText = etSubtask.text.toString().trim()
                if (subtaskText.isNotEmpty()) {
                    subtaskListStrings.add(subtaskText)
                }
            }

            btnSubmit.isEnabled = false
            btnSubmit.alpha = 0.5f

            var selectedCatName = tvCategory.text.toString()
            if (selectedCatName == "Tidak Ada Kategori" || selectedCatName.isEmpty()) {
                selectedCatName = "Lainnya"
            }

            val categoryId = getCategoryIdFromName(selectedCatName)

            val taskData = TaskRequest(
                category_id = categoryId,
                title = title,
                description = "",
                deadline = selectedDate, // Tanggal kalender langsung masuk ke sini
                status = "Pending",
                priority = "Medium",
                subtasks = subtaskListStrings
            )

            val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
            val token = sharedPref.getString("token", "") ?: ""

            RetrofitClient.instance.sendTaskData("Bearer $token", taskData).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Task untuk $selectedDate ditambahkan!", Toast.LENGTH_SHORT).show()
                        bottomSheetDialog.dismiss()
                        // Tidak perlu fetchTasks() karena CalendarFragment belum ada RecyclerView
                        // Nanti akan otomatis ke-fetch saat kembali ke HomeFragment
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
            else -> 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}