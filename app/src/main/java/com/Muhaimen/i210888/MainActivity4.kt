package com.Muhaimen.i210888
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.net.URLEncoder

class MainActivity4 : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue

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
            val name = findViewById<TextView>(R.id.nameEditText).text.toString()
            val email = findViewById<TextView>(R.id.emailEditText).text.toString()
            val contactNumber = findViewById<TextView>(R.id.contactNumberEditText).text.toString()
            val country = spinner.selectedItem.toString()
            val city = spinner2.selectedItem.toString()
            val password = findViewById<TextView>(R.id.passwordEditText).text.toString()

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
                    // You can perform additional actions here if needed
                    // For example, you can show a success message or navigate to another activity
                    startActivity(Intent(this@MainActivity4, MainActivity5::class.java))
                    finish()
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
}
