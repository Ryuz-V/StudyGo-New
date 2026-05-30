package com.example.todolist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolist.model.LoginResponse
import com.example.todolist.model.ManualLoginRequest
import com.example.todolist.model.SocialLoginRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    // Menangani hasil setelah user memilih akun Google
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("LOGIN_DEBUG", "Hasil Result Code: ${result.resultCode}")

        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // 1. Berhasil mendapatkan akun Google
                val account = task.getResult(ApiException::class.java)!!

                val nama = account.displayName ?: "User"
                val email = account.email ?: ""
                val fotoUrl = account.photoUrl?.toString() ?: ""
                val accountId = account.id ?: ""

                // Ambil ID Token untuk dikirim ke backend Laravel demi keamanan
                val idToken = account.idToken
                Log.d("LOGIN_DEBUG", "Google ID Token: $idToken")

                // 2. Siapkan data untuk dikirim ke Laravel
                // (Jika model SocialLoginRequest sudah kamu update, kamu bisa menambahkan idToken ke dalamnya)
                val loginData = SocialLoginRequest(
                    email = email,
                    name = nama,
                    provider_name = "google",
                    provider_id = accountId,
                    avatar = fotoUrl
                )

                // 3. Panggil API melalui RetrofitClient Singleton
                RetrofitClient.instance.sendSocialLoginData(loginData).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            val token = response.body()?.data?.token
                            Log.d("LOGIN_API", "Berhasil login! Token: $token")

                            // Simpan data & token ke SharedPreferences
                            val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
                            sharedPref.edit().apply {
                                putBoolean("isLoggedIn", true)
                                putString("nama", nama)
                                putString("email", email)
                                putString("foto", fotoUrl)
                                putString("token", token)
                                apply()
                            }

                            Toast.makeText(this@Login, "Selamat datang di Tugasin, $nama", Toast.LENGTH_SHORT).show()

                            // Pindah ke MainActivity
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Login, "Gagal menyimpan ke server", Toast.LENGTH_SHORT).show()
                            Log.e("LOGIN_API", "Gagal: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@Login, "Koneksi Error. Pastikan server aktif.", Toast.LENGTH_SHORT).show()
                        Log.e("LOGIN_API", "Koneksi Error: ${t.message}")
                    }
                })

            } catch (e: ApiException) {
                Toast.makeText(this@Login, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                Log.e("LOGIN_DEBUG", "ApiException Code: ${e.statusCode}")
            }
        } else {
            Log.e("LOGIN_DEBUG", "Login dibatalkan atau gagal dengan Result Code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Mengatur padding insets layout agar tidak tertutup status bar
        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Cek jika user sudah login sebelumnya (Session Management)
        val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Inisialisasi komponen UI Login Manual
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLoginManual = findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val btnGoogle = findViewById<MaterialButton>(R.id.btnGoogleLogin)

        // Navigasi ke Halaman Register
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Logika Tombol Login Manual
        btnLoginManual.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLoginManual.isEnabled = false
            btnLoginManual.text = "Loading..."

            val loginData = ManualLoginRequest(email, password)

            // Panggil API Login Manual via RetrofitClient
            RetrofitClient.instance.login(loginData).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    btnLoginManual.isEnabled = true
                    btnLoginManual.text = "Login"

                    if (response.isSuccessful && response.body()?.success == true) {
                        val token = response.body()?.data?.token
                        val nama = response.body()?.data?.user?.name ?: "User"

                        sharedPref.edit().apply {
                            putBoolean("isLoggedIn", true)
                            putString("nama", nama)
                            putString("email", email)
                            putString("token", token)
                            apply()
                        }

                        Toast.makeText(this@Login, "Selamat Datang, $nama", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Login, MainActivity::class.java))
                        finish()
                    } else {
                        // KITA UBAH BAGIAN INI AGAR ERRORNYA JELAS!
                        val errorBody = response.errorBody()?.string()
                        Log.e("LOGIN_ERROR", "Kode: ${response.code()}, Body: $errorBody")
                        Toast.makeText(this@Login, "Gagal Login (Kode: ${response.code()})", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    btnLoginManual.isEnabled = true
                    btnLoginManual.text = "Login"
                    Toast.makeText(this@Login, "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // ==========================================
        // KONFIGURASI GOOGLE SIGN IN
        // ==========================================
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Catatan: masukkan Web Client ID dari Firebase/Google Console ke strings.xml jika ingin memakai idToken
            // .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }
}