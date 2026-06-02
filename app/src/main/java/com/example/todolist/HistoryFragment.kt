package com.example.todolist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.FragmentHistoryBinding
import com.example.todolist.model.TaskData
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gunakan adapter yang sama, tapi tanpa interaksi klik complete
        historyAdapter = TaskAdapter(
            emptyList(),
            onFlagColorChanged = { _, _ -> }, // Di riwayat warna bendera dibekukan
            onTaskCompleted = { _, _ -> }     // Tidak ada aksi saat selesai karena sudah selesai
        )

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        fetchCompletedTasks()
    }

    private fun fetchCompletedTasks() {
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        if (token.isNullOrEmpty()) return

        RetrofitClient.instance.getCompletedTasks("Bearer $token").enqueue(object : Callback<List<TaskData>> {
            override fun onResponse(call: Call<List<TaskData>>, response: Response<List<TaskData>>) {
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()

                    if (tasks.isNotEmpty()) {
                        binding.rvHistory.visibility = View.VISIBLE
                        binding.imgEmptyHistory.visibility = View.GONE
                        binding.tvEmptyHistory.visibility = View.GONE
                        historyAdapter.updateData(tasks)
                    } else {
                        binding.rvHistory.visibility = View.GONE
                        binding.imgEmptyHistory.visibility = View.VISIBLE
                        binding.tvEmptyHistory.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<List<TaskData>>, t: Throwable) {
                Log.e("HISTORY_ERROR", "Gagal load riwayat: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}