package com.example.todolist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolist.model.SocialLoginRequest
import com.example.todolist.model.LoginResponse
import com.example.todolist.network.ApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class login : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    // Menangani hasil setelah user memilih akun Google
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        // LOG PENTING: Untuk mengecek apakah Google Sign-in dibatalkan atau error dari awal
        Log.d("LOGIN_DEBUG", "Hasil Result Code: ${result.resultCode}")

        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // 1. Berhasil mendapatkan akun Google
                val account = task.getResult(ApiException::class.java)!!

                val nama = account.displayName ?: "User"
                val email = account.email ?: ""
                val fotoUrl = account.photoUrl?.toString() ?: ""
                val accountId = account.id ?: "" // Diambil untuk provider_id

                // 2. Siapkan data untuk dikirim ke Laravel
                val loginData = SocialLoginRequest(
                    email = email,
                    name = nama,
                    provider_name = "google",
                    provider_id = accountId,
                    avatar = fotoUrl
                )

                // 3. Inisialisasi Retrofit
                // PENTING: Ganti 10.0.2.2 dengan IP Laptop kamu jika kamu run pakai HP asli dan kabel!
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.1.11:8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ApiService::class.java)

                // 4. Kirim ke Laravel
                api.sendSocialLoginData(loginData).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            // API Laravel merespons dengan sukses
                            val token = response.body()?.data?.token
                            Log.d("LOGIN_API", "Berhasil login! Token: $token")

                            // Simpan data & token ke SharedPreferences
                            val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
                            sharedPref.edit().apply {
                                putBoolean("isLoggedIn", true)
                                putString("nama", nama)
                                putString("email", email)
                                putString("foto", fotoUrl)
                                putString("token", token) // Token disimpan untuk API lain nanti
                                apply()
                            }

                            Toast.makeText(this@login, "Selamat datang di Tugasin, $nama", Toast.LENGTH_SHORT).show()

                            // Pindah ke MainActivity SETELAH data berhasil disimpan di database Laravel
                            val intent = Intent(this@login, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            // Gagal dari sisi server (misal validasi salah)
                            Toast.makeText(this@login, "Gagal menyimpan ke server", Toast.LENGTH_SHORT).show()
                            Log.e("LOGIN_API", "Gagal: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        // Gagal koneksi (misal server mati atau salah IP)
                        Toast.makeText(this@login, "Koneksi Error. Pastikan server aktif.", Toast.LENGTH_SHORT).show()
                        Log.e("LOGIN_API", "Koneksi Error: ${t.message}")
                    }
                })

            } catch (e: ApiException) {
                Toast.makeText(this@login, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                Log.e("LOGIN_DEBUG", "ApiException Code: ${e.statusCode}")
            }
        } else {
            // LOG PENTING: Menangkap kalau user batal milih akun atau ada error konfigurasi SHA1
            Log.e("LOGIN_DEBUG", "Login dibatalkan atau gagal dengan Result Code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cek jika user sudah login sebelumnya
        val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Konfigurasi Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // .requestIdToken("MASUKKAN_WEB_CLIENT_ID_DARI_GOOGLE_CLOUD_DISINI.apps.googleusercontent.com") // Buka komentar ini nanti kalau diminta token oleh backend
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogle = findViewById<MaterialButton>(R.id.btnGoogleLogin)

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }
}