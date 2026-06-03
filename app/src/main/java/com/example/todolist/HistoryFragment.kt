package com.example.todolist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        super.onViewCreated(view, savedInstanceState) // Cukup satu baris ini saja

        // Gunakan adapter yang sama, tapi tanpa interaksi klik complete
        historyAdapter = TaskAdapter(
            emptyList(),
            onFlagColorChanged = { _, _ -> }, // Di riwayat warna bendera dibekukan
            onTaskCompleted = { _, _ -> }     // Tidak ada aksi saat selesai karena sudah selesai
        )

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        // 1. Sembunyikan Bottom Navigation Bar
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        bottomNav.visibility = View.GONE

        // 2. Logika saat tombol kembali (Back) diklik
        binding.btnBackHistory.setOnClickListener {
            // Kembali ke halaman Home (Tugas)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            // Munculkan Bottom Navigation Bar kembali
            bottomNav.visibility = View.VISIBLE
        }

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

        // Memastikan Navbar muncul lagi jika fragment ini dihancurkan/ditutup (misal lewat tombol back HP)
        val bottomNav = activity?.findViewById<View>(R.id.bottomNav)
        bottomNav?.visibility = View.VISIBLE

        _binding = null
    }
}