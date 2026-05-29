package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OnBoarding : AppCompatActivity() {
    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Hubungkan variabel dengan elemen di XML
        val ivOnboarding = findViewById<ImageView>(R.id.ivOnboarding)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val btnNext = findViewById<MaterialButton>(R.id.btnNext)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)

        // Fungsi untuk memperbarui tampilan berdasarkan 'currentStep'
        fun updateUI() {
            when (currentStep) {
                0 -> {
                    ivOnboarding.setImageResource(R.drawable.onboarding_1)
                    tvTitle.text = "Selamat Datang di StudyGo"
                    tvDescription.text = "StudyGo hadir untuk membantu kamu mengatur jadwal belajar dan tugas dengan lebih mudah, rapi, dan terarah."
                    btnNext.text = "Kenalan Yuk"
                }
                1 -> {
                    ivOnboarding.setImageResource(R.drawable.onboarding_2)
                    tvTitle.text = "Teman Belajar Harianmu"
                    tvDescription.text = "Dapatkan pengingat yang tepat waktu agar kamu tetap fokus, tidak lupa tugas, dan lebih siap menjalani hari."
                    btnNext.text = "Lanjut"
                }
                2 -> {
                    ivOnboarding.setImageResource(R.drawable.onboarding_3)
                    tvTitle.text = "Mulai Lebih Teratur"
                    tvDescription.text = "Yuk mulai perjalanan belajarmu bersama StudyGo, supaya semua tugas dan jadwal bisa selesai dengan lebih tenang."
                    btnNext.text = "Mulai Sekarang"
                }
            }
        }

        // Panggil fungsi pertama kali agar tampilan sinkron dengan step 0
        updateUI()

        // Jika tombol Selanjutnya ditekan
        btnNext.setOnClickListener {
            if (currentStep < 2) { // 2 adalah slide terakhir (0, 1, 2)
                currentStep++      // Naikkan langkah
                updateUI()         // Perbarui gambar dan teks
            } else {
                // Jika sudah di slide terakhir, pindah ke halaman Login
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish() // Tutup halaman onboarding
            }
        }
        
        tvSkip.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}