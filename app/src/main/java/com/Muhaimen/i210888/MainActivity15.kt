package com.Muhaimen.i210888

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class UserData(
    var userId: String = "", // Add userId field
    var name: String = "",
    var email: String = "",
    var contactNumber: String = "",
    var country: String = "",
    var city: String = "",
    var password: String = "",
    var profilePicture: String? = ""
) {
    // Default constructor
    constructor() : this("", "", "", "", "", "", "", null)
}


class MainActivity15 : AppCompatActivity() {
    private val ip = "192.168.100.8"
    private lateinit var userAdapter: UserAdapter
    private val userList: ArrayList<UserData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContentView(R.layout.activity_main15)

        // for immersive mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Set up click listeners for navigation buttons
        findViewById<ImageButton>(R.id.back10).setOnClickListener {
            onBackPressed()
        }

        findViewById<ImageButton>(R.id.home4).setOnClickListener {
            // Handle click to go to home activity
            val intent = Intent(this, MainActivity8::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.search4).setOnClickListener {
            // Handle click to go to search activity
            val intent = Intent(this, MainActivity9::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.add4).setOnClickListener {
            // Handle click to go to add activity
            val intent = Intent(this, MainActivity13::class.java)
            startActivity(intent)
        }

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.userRecyclerView)
        userAdapter = UserAdapter(this, userList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        // Fetch user data from the server
        fetchUserData()
    }

    private fun fetchUserData() {
        val url = "http://$ip/get_users.php" // Replace with your server URL

        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Handle the JSON response
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                        val userId = jsonObject.getString("userId")
                        val userName = jsonObject.getString("userName")
                        val userEmail = jsonObject.getString("userEmail")

                        // Create UserData object and add it to the userList
                        val userData = UserData(userId, userName, userEmail)
                        userList.add(userData)
                    }
                    // Notify adapter of data change
                    userAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                error.printStackTrace()
            })

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest)
    }
}
