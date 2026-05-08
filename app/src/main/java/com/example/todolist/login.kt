package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton // Pastikan ini ter-import

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Hubungkan tombol dari XML ke Kotlin menggunakan ID-nya
        val btnGoogle = findViewById<MaterialButton>(R.id.btnGoogleLogin)

        // 2. Pasang pendeteksi klik (Click Listener)
        btnGoogle.setOnClickListener {
            // Memunculkan pesan kecil (Toast) di bawah layar
            Toast.makeText(this, "Mencoba login...", Toast.LENGTH_SHORT).show()

            // Perintah untuk pindah dari halaman Login ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Perintah finish() menutup halaman login agar saat user
            // menekan tombol 'Back' di HP, mereka tidak kembali ke halaman login.
            finish()
        }
    }
}