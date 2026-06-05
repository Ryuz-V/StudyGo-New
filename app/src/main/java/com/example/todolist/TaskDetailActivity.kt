package com.example.todolist

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TaskDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvDeadline = findViewById<TextView>(R.id.tvDetailDeadline)

        val containerDetailSubtasks = findViewById<LinearLayout>(R.id.containerDetailSubtasks)
        val btnAddDetailSubtask = findViewById<LinearLayout>(R.id.btnAddDetailSubtask)

        btnBack.setOnClickListener { finish() }

        // Menerima data dari Intent
        val taskId = intent.getIntExtra("TASK_ID", 0)
        val title = intent.getStringExtra("TASK_TITLE") ?: ""
        val category = intent.getStringExtra("TASK_CATEGORY") ?: "Lainnya"
        val deadline = intent.getStringExtra("TASK_DATE") ?: "Tidak diatur"

        val subtaskTitles = intent.getStringArrayListExtra("SUBTASK_TITLES") ?: arrayListOf()
        val subtaskStatus = intent.getIntegerArrayListExtra("SUBTASK_STATUS") ?: arrayListOf()

        // Pasang data utama
        tvTitle.text = title
        tvCategory.text = category
        tvDeadline.text = deadline

        // 1. TAMPILKAN SUBTASK YANG SUDAH ADA DARI SERVER
        for (i in 0 until subtaskTitles.size) {
            val subtaskView = layoutInflater.inflate(R.layout.item_detail_subtask, null)
            val tvSubtaskTitle = subtaskView.findViewById<TextView>(R.id.tvSubtaskTitle)
            val imgSubtaskCheck = subtaskView.findViewById<ImageView>(R.id.imgSubtaskCheck)

            tvSubtaskTitle.text = subtaskTitles[i]
            var isCompleted = subtaskStatus[i] == 1

            // Tampilan awal (Dicoret / Tidak)
            updateSubtaskUI(isCompleted, tvSubtaskTitle, imgSubtaskCheck)

            // Logika Klik (Centang & Coret)
            subtaskView.setOnClickListener {
                isCompleted = !isCompleted
                updateSubtaskUI(isCompleted, tvSubtaskTitle, imgSubtaskCheck)
            }

            containerDetailSubtasks.addView(subtaskView)
        }

        // 2. LOGIKA TOMBOL "TAMBAH TUGAS SAMPINGAN" (Live Save ke Database)
        btnAddDetailSubtask.setOnClickListener {
            val inputSubtaskView = layoutInflater.inflate(R.layout.item_add_subtask, null)
            val btnRemove = inputSubtaskView.findViewById<ImageView>(R.id.btnRemoveSubtask)
            val etSubtask = inputSubtaskView.findViewById<EditText>(R.id.etSubtaskName)

            btnRemove.setOnClickListener {
                containerDetailSubtasks.removeView(inputSubtaskView)
            }

            containerDetailSubtasks.addView(inputSubtaskView)
            etSubtask.requestFocus()

            // LOGIKA AUTO-SAVE: Kirim data saat user menekan "ENTER/SELESAI" di Keyboard
            etSubtask.setOnEditorActionListener { _, actionId, event ->
                val subtaskTitle = etSubtask.text.toString().trim()

                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                    if (subtaskTitle.isNotEmpty() && taskId != 0) {
                        // Kunci EditText agar tidak bisa diedit saat sedang loading
                        etSubtask.isEnabled = false

                        val sharedPref = getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
                        val token = sharedPref.getString("token", "") ?: ""

                        // PENGIRIMAN DATA MENGGUNAKAN DATA CLASS YANG BENAR
                        val requestBody = com.example.todolist.model.SubtaskRequest(subtaskTitle)

                        // Panggil API untuk menyimpan ke Laravel
                        network.RetrofitClient.instance.addSubtask("Bearer $token", taskId, requestBody)
                            .enqueue(object : retrofit2.Callback<com.example.todolist.model.TaskResponse> {
                                override fun onResponse(
                                    call: retrofit2.Call<com.example.todolist.model.TaskResponse>,
                                    response: retrofit2.Response<com.example.todolist.model.TaskResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@TaskDetailActivity, "Subtask tersimpan", Toast.LENGTH_SHORT).show()
                                        // Hilangkan tombol X dan buat teks menjadi permanen
                                        btnRemove.visibility = View.GONE
                                        etSubtask.background = null
                                    } else {
                                        etSubtask.isEnabled = true
                                        // MENAMPILKAN KODE ERROR DARI LARAVEL JIKA GAGAL
                                        Toast.makeText(this@TaskDetailActivity, "Gagal Error: ${response.code()}", Toast.LENGTH_LONG).show()
                                    }
                                }

                                override fun onFailure(call: retrofit2.Call<com.example.todolist.model.TaskResponse>, t: Throwable) {
                                    etSubtask.isEnabled = true
                                    Toast.makeText(this@TaskDetailActivity, "Koneksi terputus", Toast.LENGTH_LONG).show()
                                }
                            })
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    // Fungsi kecil untuk mengatur centang dan coretan
    private fun updateSubtaskUI(isCompleted: Boolean, tv: TextView, img: ImageView) {
        if (isCompleted) {
            img.setImageResource(R.drawable.complete)
            tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tv.setTextColor(Color.parseColor("#A0A0A0")) // Warna redup
        } else {
            img.setImageResource(R.drawable.ic_circle_outline)
            tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            tv.setTextColor(Color.parseColor("#333333")) // Warna tegas
        }
    }
}