package com.example.todolist

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Tampilkan halaman Tugas (Home) pertama kali
        replaceFragment(HomeFragment())
        updateBottomNav("Tugas") // Set menu Tugas aktif

        // 2. Tombol Navigasi Tugas ditekan
        binding.navTugas.setOnClickListener {
            replaceFragment(HomeFragment())
            updateBottomNav("Tugas")
        }

        // 3. Tombol Navigasi Kalender ditekan
        binding.navKalender.setOnClickListener {
            replaceFragment(CalendarFragment())
            updateBottomNav("Kalender")
        }

        // Nanti kalau ada ProfilFragment, tinggal tambahkan di sini
        // binding.navAkun.setOnClickListener { ... }
    }

    // FUNGSI 1: Untuk menukar halaman tanpa transisi
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // NOTE: Baris animasi fade_in & fade_out SUDAH DIHAPUS agar instan!

        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    // FUNGSI 2: Untuk mengatur warna dan teks menu yang aktif
    private fun updateBottomNav(activeMenu: String) {
        // Reset semua menu ke warna abu-abu dan sembunyikan teksnya
        binding.iconTugas.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textTugas.visibility = View.GONE

        binding.iconKalender.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textKalender.visibility = View.GONE

        binding.iconAkun.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textAkun.visibility = View.GONE

        // Ubah warna ikon menjadi hijau dan munculkan teks HANYA pada menu yang dipilih
        when (activeMenu) {
            "Tugas" -> {
                binding.iconTugas.setColorFilter(Color.parseColor("#33dbcc"))
                binding.textTugas.visibility = View.VISIBLE
            }
            "Kalender" -> {
                binding.iconKalender.setColorFilter(Color.parseColor("#33dbcc"))
                binding.textKalender.visibility = View.VISIBLE
            }
            "Akun" -> {
                binding.iconAkun.setColorFilter(Color.parseColor("#33dbcc"))
                binding.textAkun.visibility = View.VISIBLE
            }
        }
    }
}