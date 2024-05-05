package com.Muhaimen.i210888

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.*

import org.json.JSONArray
import org.json.JSONException

class MainActivity14 : AppCompatActivity() {
    private var mentorId: String? = null
    private lateinit var requestQueue: RequestQueue
    private lateinit var profileImage: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var book: TextView
    private lateinit var timeslot1: TextView
    private lateinit var timeslot2: TextView
    private lateinit var timeslot3: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var sessionPriceTextView: TextView
    private lateinit var mentorNameTextView: TextView
    private lateinit var selectedDate: String
    private var selectedTime: String = ""

    private val TAG = MainActivity14::class.java.simpleName
    private var isInitialized = false // Flag to track initialization

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main14)

        if (!isInitialized) {
            initializeViews()
            isInitialized = true
        }

        // Extract mentor details if available
        val mentorName = intent.getStringExtra("mentorName")
        val mentorProfileImageUri = intent.getStringExtra("mentorProfileImage")

        mentorProfileImageUri?.let {
            loadImage(Uri.parse(it))
        }

        mentorName?.let {
            CoroutineScope(Dispatchers.Main).launch {
                getMentorDetailsFromServer(it)
            }
        }

        book.setOnClickListener {
            bookAppointment(mentorId)
        }
    }

    private fun initializeViews() {
        requestQueue = Volley.newRequestQueue(this)

        backButton = findViewById(R.id.back9)
        book = findViewById(R.id.book)
        timeslot1 = findViewById(R.id.timeslot1)
        timeslot2 = findViewById(R.id.timeslot2)
        timeslot3 = findViewById(R.id.timeslot3)
        ratingTextView = findViewById(R.id.rating)
        sessionPriceTextView = findViewById(R.id.sessionprice)
        mentorNameTextView = findViewById(R.id.nameEditText)
        profileImage = findViewById(R.id.profileImage)

        backButton.setOnClickListener {
            onBackPressed()
        }

        timeslot1.setOnClickListener {
            selectTime("10:00 AM")
        }

        timeslot2.setOnClickListener {
            selectTime("11:00 AM")
        }

        timeslot3.setOnClickListener {
            selectTime("12:00 PM")
        }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // month is 0-based, so add 1 for the actual month
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        }
    }

    private fun selectTime(time: String) {
        selectedTime = time
        timeslot1.isSelected = time == "10:00 AM"
        timeslot2.isSelected = time == "11:00 AM"
        timeslot3.isSelected = time == "12:00 PM"
    }

    private suspend fun getMentorDetailsFromServer(mentorName: String) {
        val url = "http://192.168.100.8/get_mentor.php?name=$mentorName"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                processMentorDetails(response)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error retrieving mentor details: ${error.message}", error)
                Toast.makeText(this@MainActivity14, "Error retrieving mentor details: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        withContext(Dispatchers.IO) {
            requestQueue.add(stringRequest)
        }
    }


    private fun processMentorDetails(response: String) {
        try {
            val mentorsArray = JSONArray(response)
            if (mentorsArray.length() > 0) {
                val mentorObject = mentorsArray.getJSONObject(0)
                mentorId = mentorObject.getString("id")
                val name = mentorObject.getString("name")
                val rating = mentorObject.getString("rating")
                val sessionPrice = mentorObject.getString("sessionPrice")
                val imagePath = mentorObject.getString("imagePath")

                mentorNameTextView.text = name
                ratingTextView.text = rating
                sessionPriceTextView.text = sessionPrice

                loadImage(Uri.parse(imagePath))
            } else {
                Toast.makeText(this@MainActivity14, "No mentors found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun loadImage(uri: Uri) {
        val baseUrl = "http://192.168.100.8/" // Base URL for the images
        val completeImageUrl = baseUrl + uri
        Glide.with(this)
            .load(completeImageUrl)
            .apply(RequestOptions().transform(CircleCrop()))
            .into(profileImage)
    }

    private fun bookAppointment(mentorId: String?) {
        val sharedPreferences = getSharedPreferences("users", MODE_PRIVATE)
        val userId = sharedPreferences.getString("id", "")

        if (userId.isNullOrEmpty() || mentorId.isNullOrEmpty()) {
            Toast.makeText(this, "User ID or Mentor ID is null or empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (!::selectedDate.isInitialized || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select both date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://192.168.100.8/insert_bookings.php"
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                if (response.contains("Booking inserted successfully")) {
                    Toast.makeText(this, "Appointment booked successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to book appointment", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error booking appointment: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId
                params["mentorId"] = mentorId
                params["date"] = selectedDate
                params["time"] = selectedTime
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
