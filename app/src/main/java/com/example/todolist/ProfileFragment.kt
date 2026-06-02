package com.example.todolist

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ProfileFragment : Fragment() {

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 1. Inisialisasi View
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val tvCompleted = view.findViewById<TextView>(R.id.tvTaskCompletedCount)
        val tvPending = view.findViewById<TextView>(R.id.tvTaskPendingCount)
        lineChart = view.findViewById(R.id.lineChart)

        // 2. Ambil data User dari SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("SesiPengguna", Context.MODE_PRIVATE)
        tvName.text = sharedPref.getString("nama", "User")
        tvEmail.text = sharedPref.getString("email", "Belum ada email")

        // 3. Set Setup Dasar Chart
        setupLineChart()

        // 4. Masukkan Data Dummy (Nanti kita ganti dengan API Laravel)
        setDummyChartData()

        // Sementara kita isi angka manual, nanti ambil dari Laravel
        tvCompleted.text = "6"
        tvPending.text = "4"

        return view
    }

    private fun setupLineChart() {
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(false)
        lineChart.axisRight.isEnabled = false

        // Setup Sumbu X (Hari dalam Seminggu)
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        val days = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        xAxis.granularity = 1f

        // Setup Sumbu Y (Jumlah Tugas)
        val yAxis = lineChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
    }

    private fun setDummyChartData() {
        val entries = ArrayList<Entry>()
        // Format: Entry(Index Sumbu X, Jumlah Selesai)
        entries.add(Entry(0f, 0f)) // Min: 0
        entries.add(Entry(1f, 2f)) // Sen: 2
        entries.add(Entry(2f, 5f)) // Sel: 5
        entries.add(Entry(3f, 3f)) // Rab: 3
        entries.add(Entry(4f, 6f)) // Kam: 6
        entries.add(Entry(5f, 1f)) // Jum: 1
        entries.add(Entry(6f, 4f)) // Sab: 4

        val dataSet = LineDataSet(entries, "Tugas Selesai")
        dataSet.color = Color.parseColor("#33dbcc")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f
        dataSet.lineWidth = 2f
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(Color.parseColor("#33dbcc"))
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Membuat garis melengkung halus

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Refresh chart
    }
}