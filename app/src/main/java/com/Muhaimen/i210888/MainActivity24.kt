package com.Muhaimen.i210888
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Muhaimen.i210888.bookAdapter
import com.Muhaimen.i210888.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class MainActivity24 : AppCompatActivity() {
    private val ip = "192.168.100.8"
    private lateinit var bookedSessionList: ArrayList<Map<String, Any>>
    private lateinit var bookSessionAdapter: bookAdapter
    private val TAG = "MainActivity24"
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main24)

        setupRecyclerView()
        setupButtonListeners()

        // Retrieve current user ID
        currentUserId = getCurrentUserId()

        // Fetch booked sessions for the current user
        fetchBookedSessions()
    }

    private fun setupRecyclerView() {
        bookedSessionList = ArrayList()
        bookSessionAdapter = bookAdapter(bookedSessionList, this)
        val recyclerView: RecyclerView = findViewById(R.id.bookedSessionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = bookSessionAdapter
    }

    private fun setupButtonListeners() {
        findViewById<ImageButton>(R.id.back14).setOnClickListener {
            onBackPressed()
        }
    }

    private fun getCurrentUserId(): String {
        // Retrieve current user ID from SharedPreferences or wherever it's stored
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userId", "") ?: ""
    }

    private fun fetchBookedSessions() {
        val url = "http://$ip/get_booked_sessions.php"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.POST, url, null,
            Response.Listener<JSONArray> { response ->
                try {
                    for (i in 0 until response.length()) {
                        val bookingMap = response.getJSONObject(i).toMap()
                        bookedSessionList.add(bookingMap)
                    }
                    bookSessionAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON: ${e.message}")
                }
            },
            Response.ErrorListener { error: VolleyError ->
                Log.e(TAG, "Volley error: ${error.message}")
                // Handle error
            }
        )

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = get(key)
            map[key] = value
        }
        return map
    }
}
