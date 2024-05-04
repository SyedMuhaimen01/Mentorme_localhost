package com.Muhaimen.i210888

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import org.json.JSONException

data class Review(
    val id: String,
    val userId: String,
    val mentorId: String,
    val description: String,
    val rating: Float
)

class MainActivity12 : AppCompatActivity() {
    private val ip = "192.168.100.8"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main12)

        // Retrieve mentor details from the previous activity
        val mentorName = intent.getStringExtra("mentorName")
        val mentorDescription = intent.getStringExtra("mentorDescription")
        val mentorProfileImageUri = intent.getStringExtra("mentorProfileImage")
        val mentorId = intent.getStringExtra("mentorId").toString()

        // Set mentor details in the appropriate fields
        findViewById<TextView>(R.id.nameEditText).text = mentorName

        val imageView = findViewById<ImageView>(R.id.profileImageView)
        val requestOptions = RequestOptions().transform(CircleCrop())

        if (!mentorProfileImageUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(mentorProfileImageUri))
                .apply(requestOptions)
                .into(imageView)
        }

        // Back button click listener
        findViewById<ImageButton>(R.id.back7).setOnClickListener {
            onBackPressed()
        }

        // Submit button click listener
        findViewById<TextView>(R.id.submit).setOnClickListener {
            submitReview(mentorId)
        }

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { _, ratingValue, _ ->
            // No need to update mentor rating immediately, it will be updated upon review submission
        }
    }

    private fun submitReview(mentorId: String) {
        val reviewDescription = findViewById<TextView>(R.id.reviewDescription).text.toString()
        val newRating = findViewById<RatingBar>(R.id.ratingBar).rating

        // Send review data to the server
        val url = "http://$ip/submit_review.php"

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response from server
                if (response == "success") {
                    // Review submitted successfully
                    updateMentorRating(mentorId, newRating)
                    onBackPressed()
                } else {
                    // Failed to submit review
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                // Handle errors
                Log.e(TAG, "Error submitting review: ${error.message}")
                Toast.makeText(this, "Error submitting review", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["mentorId"] = mentorId
                params["description"] = reviewDescription
                params["rating"] = newRating.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun updateMentorRating(mentorId: String, newRating: Float) {
        // Send mentor rating update request to the server
        val url = "http://$ip/update_mentor_rating.php"

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response from server
                if (response == "success") {
                    // Rating updated successfully
                } else {
                    // Failed to update rating
                    Toast.makeText(this, "Failed to update mentor rating", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                // Handle errors
                Log.e(TAG, "Error updating mentor rating: ${error.message}")
                Toast.makeText(this, "Error updating mentor rating", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["mentorId"] = mentorId
                params["newRating"] = newRating.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    companion object {
        private const val TAG = "MainActivity12"
    }
}
