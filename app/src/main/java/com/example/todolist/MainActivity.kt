package com.example.todolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Hubungkan (inflate) desain XML ke Kotlin menggunakan Binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // 2. Tampilkan desainnya ke layar
        setContentView(binding.root)

        // Nah, nanti logika tombol dan lain-lain kamu tulis di bawah sini
    }
}