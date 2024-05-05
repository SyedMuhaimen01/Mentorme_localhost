package com.Muhaimen.i210888

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.json.JSONArray
import org.json.JSONException

class Mentor(
    val id: String,
    val name: String,
    val title: String,
    val description: String,
    var imagePath: String,
    val sessionPrice: Double,
    val availability: String,
    val rating: Double
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0.0,
        "",
        0.0
    )
}

class MainActivity10 : AppCompatActivity() {

    private lateinit var resultMentorList: ArrayList<Mentor>
    private lateinit var resultMentorAdapter: searchAdapter
    private val ip = "192.168.100.8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main10)

        setupSpinner()
        setupRecyclerView()
        setupButtonListeners()

        // Retrieve mentor name from previous activity
        val mentorName = intent.getStringExtra("mentorName") ?: ""

        // Initially fetch mentor data by name
        retrieveAndSetMentorData(mentorName)
    }

    private fun setupSpinner() {
        val items = arrayOf("All", "Filter1", "Filter2") // Add your filter options here
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner: Spinner = findViewById(R.id.spinner3)
        spinner.adapter = adapter
    }

    private fun setupRecyclerView() {
        resultMentorList = ArrayList()
        resultMentorAdapter = searchAdapter(resultMentorList, this)
        val recyclerView: RecyclerView = findViewById(R.id.searchResult)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = resultMentorAdapter
    }

    private fun setupButtonListeners() {
        // Setup button listeners as per your requirements
        findViewById<ImageButton>(R.id.back5).setOnClickListener {
            onBackPressed()
        }

        findViewById<ImageButton>(R.id.home2).setOnClickListener {
            startActivity(Intent(this, MainActivity8::class.java))
        }

        findViewById<ImageButton>(R.id.add3).setOnClickListener {
            startActivity(Intent(this, MainActivity13::class.java))
        }

        findViewById<ImageButton>(R.id.myprofile).setOnClickListener {
            startActivity(Intent(this, MainActivity21::class.java))
        }

        findViewById<ImageButton>(R.id.chat).setOnClickListener {
            startActivity(Intent(this, MainActivity15::class.java))
        }

        findViewById<ImageButton>(R.id.search).setOnClickListener {
            startActivity(Intent(this, MainActivity9::class.java))
        }
    }

    private fun retrieveAndSetMentorData(name: String) {
        Log.d(TAG, "Retrieving mentor data for name: $name")
        val url = "http://$ip/get_mentor.php?name=$name" // Append name as a query parameter

        val stringRequest = object : StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d(TAG, "Mentor data retrieved successfully: $response")
                processMentorData(response)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error retrieving mentor data: ${error.message}", error)
                Toast.makeText(this, "Error retrieving mentor data: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {}

        Volley.newRequestQueue(this).add(stringRequest)
    }


    private fun processMentorData(response: String) {
        try {
            val mentorList = ArrayList<Mentor>()
            val mentorsArray = JSONArray(response)
            for (i in 0 until mentorsArray.length()) {
                val mentorObject = mentorsArray.getJSONObject(i)
                val mentor = Mentor(
                    mentorObject.getString("id"),
                    mentorObject.getString("name"),
                    mentorObject.getString("title"),
                    mentorObject.getString("description"),
                    mentorObject.getString("imagePath"),
                    mentorObject.getDouble("sessionPrice"),
                    mentorObject.getString("availability"),
                    mentorObject.getDouble("rating")
                )
                mentorList.add(mentor)
            }
            resultMentorList.clear()
            resultMentorList.addAll(mentorList)
            resultMentorAdapter.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun retrieveMentorImage(mentor: Mentor) {
        val imageName = mentor.imagePath // Assuming imagePath contains only the image name
        val imageUrl = "http://$ip/Images/$imageName"
        mentor.imagePath = imageUrl // Update the image path in the mentor object

        // Call the adapter's method to notify it about the updated image path
        resultMentorAdapter.notifyImageUpdated(mentor)
    }

}
