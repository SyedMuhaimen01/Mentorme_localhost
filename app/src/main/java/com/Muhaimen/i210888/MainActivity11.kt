package com.Muhaimen.i210888

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import org.json.JSONException

class MainActivity11 : AppCompatActivity() {

    private lateinit var mentorId: String
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main11)

        // Retrieve mentor details from intent
        name = intent.getStringExtra("mentorName") ?: ""
        val mentorDescription = intent.getStringExtra("mentorDescription")
        val mentorProfileImageUriString = intent.getStringExtra("mentorProfileImage")
        mentorId = intent.getStringExtra("mentorId") ?: ""
        val mentorSessionPrice = intent.getDoubleExtra("mentorsessionPrice", 0.0)
        val mentorTitle = intent.getStringExtra("mentorTitle")

        // Set mentor details in the appropriate fields
        findViewById<TextView>(R.id.nameEditText).text = name
        findViewById<TextView>(R.id.TitleEditText).text = mentorTitle
        findViewById<TextView>(R.id.descriptionEditText).text = mentorDescription

        // Set rounded corners for profile image
        val imageView = findViewById<ImageView>(R.id.profileImage)
        val requestOptions = RequestOptions().transform(CircleCrop())
        val baseUrl = "http://192.168.100.8/" // Base URL for the images
        val completeImageUrl = baseUrl + mentorProfileImageUriString
        // Load mentor profile image using Glide
        val mentorProfileImageUri = mentorProfileImageUriString?.let { Uri.parse(it) }
        if (mentorProfileImageUri != null) {
            Glide.with(this)
                .load(completeImageUrl)
                .apply(requestOptions)
                .into(imageView)
        }

        // Set up back button click listener
        val backButton = findViewById<ImageButton>(R.id.back6)
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Set up button click listeners for other actions
        val bookSessionButton = findViewById<TextView>(R.id.bookSession)
        bookSessionButton.setOnClickListener {
            navigateToMainActivity14(name, mentorDescription, mentorProfileImageUriString, mentorSessionPrice)
        }

        val reviewButton = findViewById<TextView>(R.id.review)
        reviewButton.setOnClickListener {
            navigateToMainActivity12(name, mentorDescription, mentorProfileImageUriString)
        }

        val communityButton = findViewById<TextView>(R.id.community)
        communityButton.setOnClickListener {
            navigateToMainActivity17(name)
        }

        // Load mentor's rating
        loadMentorRating()
    }

    private fun navigateToMainActivity14(mentorName: String?, mentorDescription: String?, mentorProfileImageUriString: String?, mentorSessionPrice: Double) {
        val intent = Intent(this, MainActivity14::class.java).apply {
            putExtra("mentorName", mentorName)
            putExtra("mentorDescription", mentorDescription)
            putExtra("mentorProfileImage", mentorProfileImageUriString)
            putExtra("mentorsessionPrice", mentorSessionPrice)
            putExtra("mentorId", mentorId)
        }
        startActivity(intent)
    }

    private fun navigateToMainActivity12(mentorName: String?, mentorDescription: String?, mentorProfileImageUriString: String?) {
        val intent = Intent(this, MainActivity12::class.java).apply {
            putExtra("mentorName", mentorName)
            putExtra("mentorDescription", mentorDescription)
            putExtra("mentorProfileImage", mentorProfileImageUriString)
            putExtra("mentorId", mentorId)
        }
        startActivity(intent)
    }

    private fun navigateToMainActivity17(mentorName: String?) {
        val intent = Intent(this, MainActivity17::class.java).apply {
            putExtra("mentorName", mentorName)
            putExtra("mentorId", mentorId)
        }
        startActivity(intent)
    }

    private fun loadMentorRating() {
        val url = "http://192.168.100.8/get_mentor.php?name=$name"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    if (response.length() > 0) {
                        // Get the first mentor's rating
                        val firstMentor = response.getJSONObject(0)
                        val mentorRating = firstMentor.getDouble("rating")
                        findViewById<TextView>(R.id.rating).text = mentorRating.toString()
                    } else {
                        // Handle case when no mentors are found
                        findViewById<TextView>(R.id.rating).text = "No rating available"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // Handle JSON parsing error
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                // Handle error
            })

        Volley.newRequestQueue(this).add(request)
    }


}
