package com.ViVi.sbercloud.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ViVi.sbercloud.R
import com.ViVi.sbercloud.databinding.ActivityServerBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class ServerActivity : AppCompatActivity() {
    lateinit var binding: ActivityServerBinding
    //private val okHttpClient = OkHttpClient()
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private var token : String? = null
    private var serverID : String? = null
    private var filter : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        token = intent.getStringExtra("token")
        serverID = intent.getStringExtra("serverID")
        filter = intent.getStringExtra("filter")
        binding.serverName.text = serverID
        getMetricData(R.id.cpu_usage, "cpu_usage")
        getMetricData(R.id.mem_usedPercent, "mem_usedPercent")
        getMetricData(R.id.load_average5, "load_average5")
    }

    private fun getMetricData(chartID : Int, metric_name : String) {
        val url = "https://ces.ru-moscow-1.hc.sbercloud.ru/V1.0/0b96564a738027302fc7c01d7b2ff92b/metric-data?" +
                "namespace=AGT.ECS&" +
                "metric_name=$metric_name&" +
                "dim.0=instance_id,$serverID&" +
                "from=1614355320000&" +
                "to=1614441720000&" +
                "period=1200&" +
                "filter=$filter"
        val request = okhttp3.Request.Builder()
            .header("X-Auth-Token", token.toString())
            .url(url)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                syncToast("2Request failed: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                val tmpValues = ArrayList<Entry>()
                try {
                    if (response.isSuccessful) {
                        val jsonData: String = response.body!!.string()
                        val Jobject = JSONObject(jsonData)
                        val Jarray = Jobject.getJSONArray("datapoints")

                        for (i in 0 until Jarray.length()) {
                            val item = Jarray.getJSONObject(i)
                            val value = item.getDouble(filter.toString())
                            var time = item.getDouble("timestamp")
                            //time -= 1614357000000
                            time /= 1000*60*60
                            tmpValues.add(Entry(time.toFloat(), value.toFloat()))
                        }
                        this@ServerActivity.runOnUiThread {
                            setLineChartData(tmpValues, chartID)
                        }
                    } else {
                        syncToast("3Request failed")
                    }
                } catch (e: Exception) {
                    syncToast("4Request failed: $e")
                }
            }
        })
    }
    private fun syncToast(message: String) {
        this@ServerActivity.runOnUiThread {
            Toast.makeText(this@ServerActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
    fun setLineChartData(linevalues: ArrayList<Entry>, chartID : Int) {
        val linedataset = LineDataSet(linevalues, "First")

        //linedataset.circleRadius = 10f
        linedataset.valueTextSize = 20F
        linedataset.fillColor = resources.getColor(R.color.yogurt)
        linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        linedataset.setDrawFilled(true);
        linedataset.setDrawValues(false);
        linedataset.setFillAlpha(50);
        linedataset.setDrawCircles(false);
        //We connect our data to the UI Screen
        val chart : com.github.mikephil.charting.charts.LineChart = findViewById(chartID)
        val data = LineData(linedataset)
        chart.data = data
        chart.setBackgroundColor(resources.getColor(R.color.white))
        chart.animateXY(2000, 2000, Easing.EaseInCubic)
        chart.getDescription().setText("");
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.description.isEnabled = false
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.isScaleYEnabled = true
        chart.isScaleXEnabled = true
        chart.xAxis.setSpaceMin(50f);


        //chart.setVisibleYRangeMaximum(6F, YAxis.AxisDependency.LEFT);
        //chart.setVisibleXRangeMaximum(20F); // allow 20 values to be displayed at once on the x-axis, not more
        //chart.moveViewToX(10F)
    }
}
