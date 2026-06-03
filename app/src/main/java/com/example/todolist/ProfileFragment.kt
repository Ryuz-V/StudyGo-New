package com.example.todolist

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.todolist.model.StatsResponse
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var tvCompleted: TextView
    private lateinit var tvPending: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inisialisasi View
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        tvCompleted = view.findViewById<TextView>(R.id.tvTaskCompletedCount)
        tvPending = view.findViewById<TextView>(R.id.tvTaskPendingCount)
        lineChart = view.findViewById(R.id.lineChart)

        // Ambil Nama & Email dari SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        tvName.text = sharedPref.getString("nama", "User")
        tvEmail.text = sharedPref.getString("email", "Belum ada email")

        setupLineChart()

        // Mulai ambil data asli dari Laravel
        fetchStatistics()

        return view
    }

    private fun setupLineChart() {
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(false)
        lineChart.axisRight.isEnabled = false

        // Setup Sumbu X (Hari)
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        val days = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        xAxis.granularity = 1f

        // Setup Sumbu Y (Jumlah)
        val yAxis = lineChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
    }

    private fun fetchStatistics() {
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        if (token.isEmpty()) return

        RetrofitClient.instance.getTaskStats("Bearer $token").enqueue(object : Callback<StatsResponse> {
            override fun onResponse(call: Call<StatsResponse>, response: Response<StatsResponse>) {
                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats != null && stats.success) {
                        // 1. Update Angka Ringkasan
                        tvCompleted.text = stats.completed_count.toString()
                        tvPending.text = stats.pending_count.toString()

                        // 2. Update Grafik Garis
                        updateChartData(stats.chart_data)
                    }
                }
            }

            override fun onFailure(call: Call<StatsResponse>, t: Throwable) {
                Log.e("API_STATS_ERROR", "Gagal load statistik: ${t.message}")
            }
        })
    }

    private fun updateChartData(chartData: List<Int>) {
        val entries = ArrayList<Entry>()

        // Looping data array dari Laravel untuk dimasukkan ke grafik
        for (i in chartData.indices) {
            entries.add(Entry(i.toFloat(), chartData[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, "Tugas Selesai")
        dataSet.color = Color.parseColor("#33dbcc")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        dataSet.lineWidth = 2f
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(Color.parseColor("#33dbcc"))
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Wajib agar grafik me-refresh gambarnya
    }
}