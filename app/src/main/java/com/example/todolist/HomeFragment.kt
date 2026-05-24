package com.example.todolist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.FragmentHomeBinding
import com.example.todolist.model.TaskListResponse
import com.example.todolist.model.TaskRequest
import com.example.todolist.model.TaskResponse
import com.example.todolist.network.ApiService
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        taskAdapter = TaskAdapter(emptyList())
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter

        // Tombol Tambah Tugas ditekan
        binding.fabAdd.setOnClickListener {
            showAddTaskDialog()
        }

        // Ambil data tugas dari Laravel saat halaman Home dibuka
        fetchTasks()
    }

    private fun fetchTasks() {
        Toast.makeText(requireContext(), "Memuat daftar tugas...", Toast.LENGTH_SHORT).show()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.6:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        api.getTasks().enqueue(object : Callback<TaskListResponse> {
            override fun onResponse(call: Call<TaskListResponse>, response: Response<TaskListResponse>) {
                if (response.isSuccessful) {
                    val tasks = response.body()?.data ?: emptyList()

                    Toast.makeText(requireContext(), "Dapat ${tasks.size} tugas!", Toast.LENGTH_SHORT).show()

                    if (tasks.isNotEmpty()) {
                        binding.imgEmpty.visibility = View.GONE
                        binding.tvEmptyTitle.visibility = View.GONE
                        binding.tvEmptySubtitle.visibility = View.GONE
                        binding.rvTasks.visibility = View.VISIBLE

                        taskAdapter.updateData(tasks)
                    } else {
                        binding.rvTasks.visibility = View.GONE
                        binding.imgEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTitle.visibility = View.VISIBLE
                        binding.tvEmptySubtitle.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "Error dari Server: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<TaskListResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("TASK_API", "Gagal load data: ${t.message}")
            }
        })
    }

    private fun showAddTaskDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_task, null)
        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED

        val btnSubmit = view.findViewById<ImageView>(R.id.btnSubmitTask)
        val etTitle = view.findViewById<EditText>(R.id.etTaskTitle)

        btnSubmit.setOnClickListener {
            val taskTitle = etTitle.text.toString().trim()

            if (taskTitle.isEmpty()) {
                Toast.makeText(requireContext(), "Judul tugas tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ID User sementara diset 1
            val userId = 1

            val taskData = TaskRequest(
                title = taskTitle,
                user_id = userId,
                category = "Tugas"
            )

            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.6:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(ApiService::class.java)

            api.sendTaskData(taskData).enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Sukses disimpan ke Database!", Toast.LENGTH_SHORT).show()

                        bottomSheetDialog.dismiss() // Tutup popup
                        fetchTasks() // REFRESH DAFTAR TUGAS SETELAH MENAMBAHKAN BARU!

                    } else {
                        val kodeError = response.code()
                        Toast.makeText(requireContext(), "Gagal! Kode Error: $kodeError", Toast.LENGTH_LONG).show()
                        Log.e("TASK_API", "Error Server: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Koneksi Bermasalah!", Toast.LENGTH_SHORT).show()
                    Log.e("TASK_API", "Koneksi Gagal: ${t.message}")
                }
            })
        }

        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}