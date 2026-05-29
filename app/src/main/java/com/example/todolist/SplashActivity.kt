package com.example.todolist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Memberikan waktu delay 1.5 detik agar logonya sempat terlihat oleh user
        Handler(Looper.getMainLooper()).postDelayed({

            // CEK APAKAH USER SUDAH LOGIN
            val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                // JIKA SUDAH LOGIN: Lempar langsung ke MainActivity (Kalender/Tugas)
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // JIKA BELUM LOGIN: Lempar ke halaman Onboarding
                startActivity(Intent(this, OnBoarding::class.java))
            }

            // Hancurkan halaman Splash ini agar tidak bisa dikembalikan pakai tombol "Back"
            finish()

        }, 1500) // 1500 = 1,5 detik. Bisa kamu ganti sesuai selera.
    }
}