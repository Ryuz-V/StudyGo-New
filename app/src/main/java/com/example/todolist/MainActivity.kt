package com.example.todolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Tampilkan halaman Tugas (Home) saat aplikasi baru dibuka
        replaceFragment(HomeFragment())

        // 2. Tombol Navigasi Tugas ditekan
        binding.navTugas.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        // 3. Tombol Navigasi Kalender ditekan
        binding.navKalender.setOnClickListener {
            replaceFragment(CalendarFragment())
        }
    }

    // Fungsi ala Senior Dev untuk menukar halaman (Fragment)
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Pasang animasi biar perpindahannya halus
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)

        // Ganti wadah kosong dengan Fragment yang dipilih
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}