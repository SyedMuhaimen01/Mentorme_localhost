package com.Muhaimen.i210888
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var contactNumber: String = "",
    var country: String = "",
    var city: String = "",
    var password: String = "",
    var profilePicture: String? = ""
)

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

    // Inside fetchUserData() method
    // Inside fetchUserData() method
    // Inside fetchUserData() method
    private fun fetchUserData() {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("id", "")

        Log.d(TAG, "Fetching user data for userId: $userId")

        val url = "http://$ip/get_allusers.php?id=$userId" // Pass the current user ID

        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Log the response received from the server
                Log.d(TAG, "Response from server: $response")

                // Handle the JSON response
                try {
                    val jsonArray = JSONArray(response)

                    // Clear existing data in the list
                    userList.clear()

                    // Parse the JSON array and populate userList
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val user = UserData(
                            userId = jsonObject.getString("id"),
                            name = jsonObject.getString("name"),
                            email = jsonObject.optString("email", ""),
                            contactNumber = jsonObject.optString("contactNumber", ""),
                            country = jsonObject.optString("country", ""),
                            city = jsonObject.optString("city", ""),
                            password = jsonObject.optString("password", ""),
                            profilePicture = jsonObject.optString("profilePicture", null)
                        )
                        userList.add(user)
                    }

                    // Notify the adapter that the data set has changed
                    userAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    // Handle JSON parsing error
                    Log.e(TAG, "Error parsing JSON: ${e.message}")
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
