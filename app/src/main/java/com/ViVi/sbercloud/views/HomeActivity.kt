package com.ViVi.sbercloud.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ViVi.sbercloud.R
import com.ViVi.sbercloud.databinding.ActivityHomeBinding
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class HomeActivity : AppCompatActivity()  {
    lateinit var binding: ActivityHomeBinding
    private val okHttpClient = OkHttpClient()
    private var token : String? = null
    private var servers = mutableListOf<String>()
    private var filter : String = "max"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra("token")

        binding.btnToken.setOnClickListener {
            Toast.makeText(this@HomeActivity, "Token: $token", Toast.LENGTH_SHORT).show()
        }

        setup_servers_button()
        setup_filter_button()
    }

    private fun setup_servers_button() {
        val listPopupWindowButton = binding.listPopupButton
        val listPopupWindow = ListPopupWindow(this@HomeActivity, null, R.attr.listPopupWindowStyle)
        listPopupWindow.anchorView = listPopupWindowButton
        listPopupWindow.setOnItemClickListener {
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long ->

            val intent = Intent(this@HomeActivity, ServerActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("filter", filter)
            intent.putExtra("serverID", servers[position])
            startActivity(intent)
            listPopupWindow.dismiss()
        }

        listPopupWindowButton.setOnClickListener { v: View? ->
            getMetrics(listPopupWindow)
        }
    }

    private fun setup_filter_button(){
        val listPopupWindowButton = binding.filterPopupButton
        val listPopupWindow = ListPopupWindow(this@HomeActivity, null, R.attr.listPopupWindowStyle)
        listPopupWindow.anchorView = listPopupWindowButton
        val items = listOf("min", "max", "avg")
        val adapter = ArrayAdapter(this@HomeActivity, R.layout.list_popup_window_item, items)
        listPopupWindow.setAdapter(adapter)

        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            when(position){
                0 -> filter = "min"
                1 -> filter = "max"
                2 -> filter = "sum"
            }
            listPopupWindow.dismiss()
        }

        listPopupWindowButton.setOnClickListener { v: View? -> listPopupWindow.show() }
    }
    private fun getMetrics(listPopupWindow: ListPopupWindow) {
        val url = "https://ces.ru-moscow-1.hc.sbercloud.ru/V1.0/0b96564a738027302fc7c01d7b2ff92b/metrics?namespace=SYS.ECS"
        val request = okhttp3.Request.Builder()
                .header("X-Auth-Token", token.toString())
                .url(url)
                .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                syncToast("2Request failed: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                val serverSet = mutableSetOf<String>()
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
                            serverSet.add(id)
                        }

                        if (serverSet.isNotEmpty()) {
                            servers = serverSet.toMutableList()
                            val adapter = ArrayAdapter(this@HomeActivity, R.layout.list_popup_window_item, servers)
                            listPopupWindow.setAdapter(adapter)
                            this@HomeActivity.runOnUiThread {
                                listPopupWindow.show()
                            }
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
    private fun syncToast(message: String) {
        this@HomeActivity.runOnUiThread {
            Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
