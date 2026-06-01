package com.example.todolist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.todolist.databinding.ActivityMainBinding
import androidx.core.view.GravityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Tampilkan halaman Tugas (Home) pertama kali
        replaceFragment(HomeFragment())
        updateBottomNav("Tugas")

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

        // 4. Tombol Navigasi Akun ditekan
        binding.navAkun.setOnClickListener {
            replaceFragment(ProfileFragment())
            updateBottomNav("Akun")
        }

        // 5. Tombol Sidebar (navSort) ditekan untuk membuka Drawer
        binding.navSort.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // ==========================================
        //         LOGIC SIDEBAR MENU LOGOUT
        // ==========================================
        binding.menuLogout.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
        }
    }

    // FUNGSI 1: Untuk menukar halaman
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    // FUNGSI 2: Untuk mengatur warna dan teks menu yang aktif
    private fun updateBottomNav(activeMenu: String) {
        binding.iconTugas.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textTugas.visibility = View.GONE

        binding.iconKalender.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textKalender.visibility = View.GONE

        binding.iconAkun.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textAkun.visibility = View.GONE

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