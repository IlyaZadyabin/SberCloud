package com.ViVi.sbercloud.views

/** include your package here **/

/** fix missing imports **/
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ViVi.sbercloud.databinding.ActivitySignInBinding
import kotlinx.android.synthetic.main.activity_sign_in.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SignInActivity : AppCompatActivity() {
    lateinit var signInUsername: String
    lateinit var signInPassword: String
    lateinit var signInInputsArray: Array<EditText>
    lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        signInInputsArray = arrayOf(etSignInUsername, etSignInPassword)

        btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    private fun notEmpty(): Boolean = signInUsername.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signInUser() {
        signInUsername = etSignInUsername.text.toString().trim()
        signInPassword = etSignInPassword.text.toString().trim()
        val okHttpClient = OkHttpClient()

        if (notEmpty()) {
            val url = "https://iam.ru-moscow-1.hc.sbercloud.ru/v3/auth/tokens"
            val jsonObject = JSONObject(
                    "{\n" +
                            "    \"auth\": {\n" +
                            "        \"identity\": {\n" +
                            "            \"methods\": [\n" +
                            "                \"password\"\n" +
                            "            ],\n" +
                            "            \"password\": {\n" +
                            "                \"user\": {\n" +
                            "                    \"name\": \"$signInUsername\",\n" +
                            "                    \"password\": \"$signInPassword\",\n" +
                            "                    \"domain\": {\n" +
                            "                        \"name\": \"$signInUsername\"\n" +
                            "                    }\n" +
                            "                }\n" +
                            "            }\n" +
                            "        },\n" +
                            "        \"scope\": {\n" +
                            "            \"project\": {\n" +
                            "                \"name\": \"ru-moscow-1\",\n" +
                            "                \"domain\": {\n" +
                            "                    \"name\": \"RU-Moscow\"\n" +
                            "                }\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            "}"
            )

            val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = okhttp3.Request.Builder()
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .url(url)
                    .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    this@SignInActivity.runOnUiThread {
                        Toast.makeText(this@SignInActivity, "Sign in failed: $e", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val a = response.headers["X-Subject-Token"]
                        if (a != null) {
                            val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                            intent.putExtra("token", a)
                            startActivity(intent)
                            this@SignInActivity.runOnUiThread {
                                Toast.makeText(this@SignInActivity, "Sign in successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            this@SignInActivity.runOnUiThread {
                                Toast.makeText(this@SignInActivity, "Wrong credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        this@SignInActivity.runOnUiThread {
                            Toast.makeText(this@SignInActivity, "Sign in failed: $e", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        }
    }
}