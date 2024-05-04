package com.Muhaimen.i210888

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class Main3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val loginButton = findViewById<TextView>(R.id.loginButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        val forgotPasswordButton = findViewById<TextView>(R.id.forgotpwd)
        forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, MainActivity6::class.java))
            finish()
        }

        val signupButton = findViewById<TextView>(R.id.signupBtn)
        signupButton.setOnClickListener {
            startActivity(Intent(this, MainActivity4::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        val ip = "192.168.100.8"
        val url = "http://$ip/usersLogin.php"

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getBoolean("success")
                    val message = jsonObject.getString("message")

                    if (success) {
                        val id = jsonObject.getString("id")
                        val name = jsonObject.getString("name")
                        val email = jsonObject.getString("email")
                        val password = jsonObject.getString("password")
                        val city = jsonObject.getString("city")
                        val country = jsonObject.getString("country")
                        val imageUrl = jsonObject.optString("profilePicture")
                        val contactNumber = jsonObject.getString("contactNumber")

                        saveUserToSharedPreferences(id, name, email, password, city, country, imageUrl, contactNumber)
                        setLoggedIn(true)

                        startActivity(Intent(this, MainActivity8::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("LoginActivity", "JSONException: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: " + error.message, Toast.LENGTH_SHORT).show()
                Log.e("LoginActivity", "Volley Error: ${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["password"] = password
                return params
            }
        }

        // Add the request to the RequestQueue.
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)

        // Log message for request sent
        Log.d("LoginActivity", "Login request sent")
    }

    private fun saveUserToSharedPreferences(id: String, name: String, email: String, password: String, city: String, country: String, imageUrl: String, contactNumber: String) {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("id", id)
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.putString("city", city)
        editor.putString("country", country)
        editor.putString("imageUrl", imageUrl)
        editor.putString("contactNumber", contactNumber)
        editor.apply()
        Log.d("MainActivity", "User data saved to SharedPreferences: $id, $name, $email, $password, $city, $country, $imageUrl, $contactNumber")
    }

    private fun setLoggedIn(isLoggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }
}
