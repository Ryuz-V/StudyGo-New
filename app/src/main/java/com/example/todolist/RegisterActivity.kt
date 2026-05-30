package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todolist.model.LoginResponse
import com.example.todolist.model.RegisterRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<TextInputEditText>(R.id.etRegName)
        val etEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etRegPasswordConfirm)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegisterSubmit)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        tvBackToLogin.setOnClickListener {
            finish() // Menutup halaman register dan kembali ke login
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // 1. Validasi Input
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text = "Loading..."

            val requestData = RegisterRequest(name, email, password, confirmPassword)

            // 2. Panggil API Laravel menggunakan RetrofitClient
            network.RetrofitClient.instance.register(requestData).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Daftar"

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@RegisterActivity, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_SHORT).show()

                        // 3. Pindah ke Halaman Login (Sesuai Permintaanmu!)
                        val intent = Intent(this@RegisterActivity, Login::class.java)
                        startActivity(intent)
                        finishAffinity() // Membersihkan stack agar tidak bisa back ke Register
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registrasi Gagal. Email mungkin sudah terdaftar.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Daftar"
                    Toast.makeText(this@RegisterActivity, "Error Koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}