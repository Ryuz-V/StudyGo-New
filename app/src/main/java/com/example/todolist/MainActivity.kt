package com.example.todolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.todolist.databinding.ActivityMainBinding
import java.util.Calendar // Ini wajib ada untuk fungsi waktu

class MainActivity : AppCompatActivity() {

    // Hanya boleh ada SATU deklarasi binding
    private lateinit var binding: ActivityMainBinding

    // Hanya boleh ada SATU fungsi onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Logika untuk Sapaan Berdasarkan Waktu
        val greeting = getGreetingMessage()
        binding.tvName.text = greeting // Menyapa "Selamat Pagi,", dll

        // 2. Simulasi Data User (Sesuai dengan namamu)
        val dummyProfileUrl = "https://i.pravatar.cc/150?img=11"
        val dummyUserName = "Jathniel Urdha Herauwan"

        // 3. Set Nama User ke UI
        binding.tvUserName.text = dummyUserName

        // 4. Load gambar profil pakai Glide
        Glide.with(this)
            .load(dummyProfileUrl)
            .circleCrop()
            .into(binding.imgProfile)
    }

    // Fungsi untuk mengecek jam
    private fun getGreetingMessage(): String {
        val c = Calendar.getInstance()
        val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

        return when (timeOfDay) {
            in 0..10 -> "Selamat Pagi👋"   // Jam 00:00 - 10:59
            in 11..14 -> "Selamat Siang👋" // Jam 11:00 - 14:59
            in 15..17 -> "Selamat Sore👋"  // Jam 15:00 - 17:59
            else -> "Selamat Malam👋"      // Jam 18:00 - 23:59
        }
    }
}