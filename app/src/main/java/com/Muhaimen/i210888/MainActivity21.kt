package com.Muhaimen.i210888

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.json.JSONException
import org.json.JSONObject

class MainActivity21 : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var locationTextView: TextView
    private val ip = "192.168.100.8"
    private val FETCH_IMAGE_URL = "http://$ip/getImage.php"
    private lateinit var profileImageView: ImageView
    private lateinit var requestQueue: RequestQueue


    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImage ->
            uploadImageToServer(selectedImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main21)
        requestQueue = Volley.newRequestQueue(this)
        usernameTextView = findViewById(R.id.username)
        locationTextView = findViewById(R.id.location)
        profileImageView = findViewById(R.id.profileImageView)

        val buttonEditProfile = findViewById<ImageButton>(R.id.edit2)
        buttonEditProfile.setOnClickListener {
            openGalleryForImage()
        }

        val logoutButton = findViewById<TextView>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()
        }

        val button5 = findViewById<ImageButton>(R.id.edit)
        button5.setOnClickListener {
            val intent5 = Intent(this, MainActivity22::class.java)
            startActivity(intent5)
        }

        val button4 = findViewById<ImageButton>(R.id.home3)
        button4.setOnClickListener {
            onBackPressed()
        }

        // Add button listeners for other buttons
        val buttonHome = findViewById<ImageButton>(R.id.home)
        buttonHome.setOnClickListener {
            startActivity(Intent(this, MainActivity8::class.java))
        }

        val bookedSession = findViewById<TextView>(R.id.bookedSessions)
        bookedSession.setOnClickListener {
            startActivity(Intent(this, MainActivity24::class.java))
        }

        val buttonSearch = findViewById<ImageButton>(R.id.search)
        buttonSearch.setOnClickListener {
            startActivity(Intent(this, MainActivity9::class.java))
        }

        val buttonChat = findViewById<ImageButton>(R.id.chat)
        buttonChat.setOnClickListener {
            startActivity(Intent(this, MainActivity15::class.java))
        }

        val buttonMyProfile = findViewById<ImageButton>(R.id.myprofile)
        buttonMyProfile.setOnClickListener {
            // Already in My Profile
            loadUserProfile() // Load profile data when My Profile button is clicked
        }

        val buttonAdd = findViewById<ImageButton>(R.id.add)
        buttonAdd.setOnClickListener {
            startActivity(Intent(this, MainActivity13::class.java))
        }

        loadUserProfile()
    }

    private fun openGalleryForImage() {
        getContent.launch("image/*")
    }

    private fun uploadImageToServer(imageUri: Uri) {
        // Implement image upload functionality to your server here
    }
    private fun fetchProfileImage() {
        val request = StringRequest(
            Request.Method.POST,
            FETCH_IMAGE_URL,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getString("success")
                    val imageUrl = jsonObject.getString("profilePicture")

                    if (success == "1") {
                        val fullImageUrl = "http://$ip/$imageUrl"

                        Log.d("ImageLoading", "Full Image URL: $fullImageUrl")

                        loadImage(fullImageUrl)
                    } else {
                        Log.e("ImageLoading", "Failed to fetch image")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("ImageLoading", "Error: ${error.message}")
            }
        )

        requestQueue.add(request)
    }
    private fun loadImage(imageUrl: String) {
        Log.d("ImageLoading", "Loading image from URL: $imageUrl")
        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("ImageLoading", "Image loading failed: $e")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("ImageLoading", "Image loaded successfully")
                    return false
                }
            })
            .into(findViewById(R.id.profileImageView))
    }
    private fun loadUserProfile() {
        val userId = getUserIdFromSharedPreferences()
        val url = "http://$ip/getImage.php?id=$userId" // Replace with your server URL for fetching user profile

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val username = jsonObject.getString("name")
                    val location = jsonObject.getString("city")
                    val imageUrl = jsonObject.getString("profilePicture")

                    usernameTextView.text = username
                    locationTextView.text = location

                    // Load user image if available
                    // Load the image using Glide if the image URL is not null or empty
                    if (!imageUrl.isNullOrEmpty()) {
                        val fullImageUrl = "http://$ip/$imageUrl"
                        Glide.with(this)
                            .load(fullImageUrl)
                            .circleCrop()
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e("ImageLoading", "Image loading failed: $e")
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.d("ImageLoading", "Image loaded successfully")
                                    return false
                                }
                            })
                            .into(profileImageView)
                    } else {
                        Log.d("ImageLoading", "Image URL is null or empty")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("UserProfileLoading", "Error: ${error.message}")
            }
        )

        requestQueue.add(request)
    }

    private fun logoutUser() {
        val intent = Intent(this, Main3Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun getUserIdFromSharedPreferences(): String? {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("id", null)
        Log.d("MainActivity21", "User ID from SharedPreferences: $id")
        return id
    }
}
