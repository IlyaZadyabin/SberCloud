package com.ViVi.sbercloud.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ViVi.sbercloud.databinding.ActivityHomeBinding
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class HomeActivity : AppCompatActivity()  {
    lateinit var binding: ActivityHomeBinding
    private val okHttpClient = OkHttpClient()
    private var token : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra("token")
        binding.btnToken.setOnClickListener {
            Toast.makeText(this@HomeActivity, "Token: $token", Toast.LENGTH_SHORT).show()
        }
        binding.btnAllServers.setOnClickListener {
            getMetrics()
        }
    }

    private fun syncToast(message: String) {
        this@HomeActivity.runOnUiThread {
            Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMetrics() {

        val servers = mutableSetOf<String>()

        val url = "https://ces.ru-moscow-1.hc.sbercloud.ru/V1.0/0b96564a738027302fc7c01d7b2ff92b/metrics?namespace=SYS.ECS"
        val formBody = FormBody.Builder()
                .build()
        val request = okhttp3.Request.Builder()
                .header("X-Auth-Token", token.toString())
                .url(url)
                .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                syncToast("2Request failed: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        val jsonData: String = response.body!!.string()
                        val Jobject = JSONObject(jsonData)
                        val Jarray = Jobject.getJSONArray("metrics")

                        for (i in 0 until Jarray.length()) {
                            val item = Jarray.getJSONObject(i)
                            val dimensions = item.getJSONArray("dimensions")
                            val instance_id = dimensions.getJSONObject(0)
                            val id = instance_id.getString("value")
                            servers.add(id)
                        }

                        if (servers.isNotEmpty()) {
                            val str = servers.toString()
                            syncToast("Servers: $str")
                        } else {
                            syncToast("1Request failed")
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
}
