package com.Muhaimen.i210888

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import org.json.JSONException

class MainActivity12 : AppCompatActivity() {
    private val ip = "192.168.100.8"
    private val NOTIFICATION_CHANNEL_ID = "ReviewNotification"

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

        // Get the user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("id", "")

        // Check if userId is not null or empty before proceeding
        if (userId.isNullOrEmpty()) {
            Log.e(TAG, "User ID is null or empty")
            Toast.makeText(this, "User ID is null or empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Send review data to the server
        val url = "http://$ip/submit_review.php"

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle response from server
                if (response == "success") {
                    // Review submitted successfully
                    updateMentorRating(mentorId, newRating)
                    showReviewSubmissionNotification()
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
                params["userId"] = userId ?: ""
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

    private fun showReviewSubmissionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Review Notification", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notification for review submission"
                enableLights(true)
                lightColor = Color.GREEN
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification2_foreground)
            .setContentTitle("Review Submitted")
            .setContentText("Your review has been successfully submitted.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }


    companion object {
        private const val TAG = "MainActivity12"
    }
}
