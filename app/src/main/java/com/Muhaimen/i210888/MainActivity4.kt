package com.Muhaimen.i210888

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity4 : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private val NOTIFICATION_CHANNEL_ID = "RegistrationNotification"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        requestQueue = Volley.newRequestQueue(this)

        val items = arrayOf("Select Country", "Pakistan", "India", "Afghanistan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner: Spinner = findViewById(R.id.spinner)
        spinner.adapter = adapter

        val items2 = arrayOf("Select City", "Islamabad", "Karachi", "Lahore")
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, items2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner2: Spinner = findViewById(R.id.spinner2)
        spinner2.adapter = adapter2

        val button = findViewById<TextView>(R.id.signup)
        button.setOnClickListener {
            val name = findViewById<EditText>(R.id.nameEditText).text.toString()
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val contactNumber = findViewById<EditText>(R.id.contactNumberEditText).text.toString()
            val country = spinner.selectedItem.toString()
            val city = spinner2.selectedItem.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            // Make network request using Volley
            saveUser(name, email, contactNumber, country, city, password)
        }

        val button2 = findViewById<TextView>(R.id.loginBtn)
        button2.setOnClickListener {
            startActivity(Intent(this, Main3Activity::class.java))
        }
    }

    private fun saveUser(name: String, email: String, contactNumber: String, country: String, city: String, password: String) {
        val ip = "192.168.100.8"
        val url = "http://$ip/save_user.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,
            Response.Listener { response ->
                // Handle the response
                if (response.contains("New record created successfully")) {
                    // Data saved successfully
                    // Save user details in SharedPreferences
                    saveUserToSharedPreferences(name, email, contactNumber, country, city, password)

                    // Show registration success notification
                    showRegistrationSuccessNotification()
                } else {
                    // Data saving failed
                    // You can handle the failure here, such as showing an error message
                }
            },
            Response.ErrorListener { error ->
                // Handle error
            }) {

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
                params["email"] = email
                params["contactNumber"] = contactNumber
                params["country"] = country
                params["city"] = city
                params["password"] = password
                return params
            }
        }

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest)
    }

    private fun saveUserToSharedPreferences(name: String, email: String, contactNumber: String, country: String, city: String, password: String) {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("contactNumber", contactNumber)
        editor.putString("country", country)
        editor.putString("city", city)
        editor.putString("password", password)
        editor.apply()
        Log.d("MainActivity", "User data saved to SharedPreferences: $name, $email, $contactNumber, $country, $city, $password")
    }

    private fun showRegistrationSuccessNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Registration Notification", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notification for registration success"
                enableLights(true)
                lightColor = Color.GREEN
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification2_foreground)
            .setContentTitle("Registration Successful")
            .setContentText("Your account has been successfully registered.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }

}
