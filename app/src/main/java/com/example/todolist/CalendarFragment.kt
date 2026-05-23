package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // Status apakah sedang mode minggu (kecil) atau bulan (besar)
    private var isWeekView = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Menggunakan ViewBinding untuk memanggil ID dengan mudah
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Logika saat panah ditekan
        binding.btnToggleCalendar.setOnClickListener {
            isWeekView = !isWeekView // Balik statusnya

            if (isWeekView) {
                // Sembunyikan Kalender Besar, Munculkan Kalender Kecil
                binding.calendarView.visibility = View.GONE
                binding.weekViewLayout.visibility = View.VISIBLE

                // Putar icon panah menghadap ke bawah
                binding.btnToggleCalendar.rotation = 180f
            } else {
                // Munculkan Kalender Besar, Sembunyikan Kalender Kecil
                binding.calendarView.visibility = View.VISIBLE
                binding.weekViewLayout.visibility = View.GONE

                // Putar icon panah kembali ke atas
                binding.btnToggleCalendar.rotation = 0f
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}