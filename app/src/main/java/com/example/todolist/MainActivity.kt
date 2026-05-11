package com.example.todolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.todolist.databinding.ActivityMainBinding
import java.util.Calendar // Ini wajib ada untuk fungsi waktu

class MainActivity : AppCompatActivity() {

    // Hanya boleh ada SATU deklarasi binding
    private lateinit var binding: ActivityMainBinding
}