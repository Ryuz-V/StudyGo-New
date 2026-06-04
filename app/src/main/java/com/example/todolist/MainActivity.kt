package com.example.todolist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
        //         LOGIC SIDEBAR MENU
        // ==========================================
        binding.history.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            replaceFragment(HistoryFragment())

            // Matikan semua warna indikator bawah saat di mode Riwayat
            updateBottomNav("None")
        }

        // JIKA MENU LOGOUT DITEKAN
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
        // 1. Reset semua instan: Ikon abu-abu, Teks hilang, Kotak hilang
        binding.iconTugas.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textTugas.visibility = View.GONE
        binding.bgTugas.visibility = View.GONE

        binding.iconKalender.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textKalender.visibility = View.GONE
        binding.bgKalender.visibility = View.GONE

        binding.iconAkun.setColorFilter(Color.parseColor("#C4C4C4"))
        binding.textAkun.visibility = View.GONE
        binding.bgAkun.visibility = View.GONE

        // 2. Aktifkan Ikon & Teks INSTAN (Diam), lalu beri animasi Pop In khusus pada Kotak Background
        when (activeMenu) {
            "Tugas" -> {
                binding.iconTugas.setColorFilter(Color.parseColor("#33dbcc"))
                binding.textTugas.visibility = View.VISIBLE
                binding.bgTugas.visibility = View.VISIBLE
                animatePopIn(binding.bgTugas)
            }
            "Kalender" -> {
                binding.iconKalender.setColorFilter(Color.parseColor("#33dbcc"))
                binding.textKalender.visibility = View.VISIBLE
                binding.bgKalender.visibility = View.VISIBLE
                animatePopIn(binding.bgKalender)
            }
            "Akun" -> {
                binding.iconAkun.setColorFilter(Color.parseColor("#33dbcc"))
                binding.textAkun.visibility = View.VISIBLE
                binding.bgAkun.visibility = View.VISIBLE
                animatePopIn(binding.bgAkun)
            }
        }
    }

    private fun animatePopIn(view: View) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha = 1f

        // Setelah aman dikecilkan, barulah kita minta Android
        // menghitung titik tengahnya dan mulai membesarkannya
        view.post {
            // Pastikan membesar dari tengah persis
            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f

            // Jalankan animasi membesar (Pop)
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator(1.5f)) // Efek pantulan
                .start()
        }
    }
}