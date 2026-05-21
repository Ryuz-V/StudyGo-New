package com.example.todolist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import de.hdodenhof.circleimageview.CircleImageView

class Profil : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val ivProfile = findViewById<CircleImageView>(R.id.ivProfilePicture)
        val tvLogout = findViewById<TextView>(R.id.tvLogout)

        // 1. Ambil data dari SharedPreferences (yang disimpan saat login)
        val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val nama = sharedPref.getString("nama", "User Profile")
        val email = sharedPref.getString("email", "email@example.com")
        val fotoUrl = sharedPref.getString("foto", "")

        // 2. Tampilkan data ke UI
        tvName.text = nama
        tvEmail.text = email

        // Jika fotoUrl tidak kosong, muat menggunakan Glide
        if (!fotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(fotoUrl)
                .into(ivProfile)
        }

        // 3. Logika untuk Logout
        tvLogout.setOnClickListener {
            // Hapus data dari SharedPreferences
            sharedPref.edit().clear().apply()

            // Logout dari Google Client
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)

            googleSignInClient.signOut().addOnCompleteListener {
                // Setelah logout, kembali ke halaman Login
                val intent = Intent(this, login::class.java)
                // Bersihkan history agar tidak bisa diba   ck ke profil
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}