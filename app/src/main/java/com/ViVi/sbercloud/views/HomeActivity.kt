package com.ViVi.sbercloud.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ViVi.sbercloud.databinding.ActivityHomeBinding
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.OkHttpClient

class HomeActivity : AppCompatActivity()  {
    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val okHttpClient = OkHttpClient()

        btn.setOnClickListener {
            val str = intent.getStringExtra("token");
            Toast.makeText(this@HomeActivity, "Token: $str", Toast.LENGTH_SHORT).show()
        }
    }
}