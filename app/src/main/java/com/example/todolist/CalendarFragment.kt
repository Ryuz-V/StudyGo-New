package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private var isWeekView = false

    // Simpan data kalender saat ini untuk navigasi bulan
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
            binding.calendarView.date = currentCalendar.timeInMillis
            updateHeaderBulan()
        }

        // 3. Logika Panah GANTI BULAN KANAN (Bulan Selanjutnya)
        binding.btnBulanKanan.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            binding.calendarView.date = currentCalendar.timeInMillis
            updateHeaderBulan()
        }

        // 4. Update teks bulan kalau user nge-swipe kalendernya langsung
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            currentCalendar.set(year, month, dayOfMonth)
            updateHeaderBulan()
        }

// 5. Logika Panah Toggle (Sembunyikan/Munculkan Kalender)
        binding.btnToggleCalendar.setOnClickListener {
            isWeekView = !isWeekView

            if (isWeekView) {
                binding.calendarContainer.visibility = View.GONE // PERUBAHAN DI SINI
                binding.weekViewLayout.visibility = View.VISIBLE
                binding.btnToggleCalendar.rotation = 180f
            } else {
                binding.calendarContainer.visibility = View.VISIBLE // PERUBAHAN DI SINI
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
    }

    // Fungsi otomatis untuk memformat dan menampilkan teks bulan (misal: "MEI 2026")
    private fun updateHeaderBulan() {
        // Format bahasa Indonesia (ID) agar nama bulannya pas
        val formatBulan = SimpleDateFormat("MMMM  yyyy", Locale("id", "ID"))
        val teksBulan = formatBulan.format(currentCalendar.time).uppercase()
        binding.tvBulanTahun.text = teksBulan
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}