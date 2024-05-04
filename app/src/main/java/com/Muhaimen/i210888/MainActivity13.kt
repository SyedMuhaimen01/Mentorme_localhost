package com.Muhaimen.i210888

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream
import android.util.Base64
import com.google.android.material.imageview.ShapeableImageView
import java.util.*

class MainActivity13 : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var sharedPreferences: SharedPreferences

    private val ip = "192.168.100.8"
    private val UPLOAD_URL = "http://$ip/save_mentor.php"
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var spinner: Spinner
    private lateinit var selectedImageUri: Uri

    private val TAG = MainActivity13::class.java.simpleName

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main13)

        Log.d(TAG, "onCreate")

        requestQueue = Volley.newRequestQueue(this)
        sharedPreferences = getSharedPreferences("MentorDetails", MODE_PRIVATE)

        nameEditText = findViewById(R.id.nameEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        spinner = findViewById(R.id.spinner4)

        val items = arrayOf("Available", "Not Available")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val backButton = findViewById<ImageButton>(R.id.back8)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val buttonHome = findViewById<ImageButton>(R.id.home3)
        buttonHome.setOnClickListener {
            startActivity(Intent(this, MainActivity8::class.java))
        }

        val buttonSearch = findViewById<ImageButton>(R.id.search3)
        buttonSearch.setOnClickListener {
            startActivity(Intent(this, MainActivity9::class.java))
        }

        val buttonChat = findViewById<ImageButton>(R.id.chat)
        buttonChat.setOnClickListener {
            startActivity(Intent(this, MainActivity15::class.java))
        }

        val buttonMyProfile = findViewById<ImageButton>(R.id.myprofile)
        buttonMyProfile.setOnClickListener {
            startActivity(Intent(this, MainActivity21::class.java))
        }
        val selectImageButton = findViewById<ShapeableImageView>(R.id.uploadimage)
        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        val uploadButton = findViewById<TextView>(R.id.upload)
        uploadButton.setOnClickListener {
            Log.d(TAG, "Upload button clicked")
            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val availability = spinner.selectedItem.toString()
            val rating = "0"
            val sessionPrice = "120"
            val title = "Mentor"

            // Save mentor details in SharedPreferences
            saveMentorDetails(name, description, availability, rating, sessionPrice, title)

            // Make network request using Volley
            saveMentor(name, description, availability, rating, sessionPrice, title)

            // Put mentor details as extras and start the new activity
            val intent = Intent(this, MainActivity14::class.java).apply {
                putExtra("mentorName", name)
                putExtra("mentorDescription", description)
                putExtra("mentorAvailability", availability)
                putExtra("mentorRating", rating)
                putExtra("mentorSessionPrice", sessionPrice)
                putExtra("mentorTitle", title)
                putExtra("mentorImageUri", selectedImageUri.toString())
            }
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        Log.d(TAG, "Opening file chooser")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
        } else {
            // Handle case where selected image URI is null
            Log.e(TAG, "Error: Selected image URI is null.")
            Toast.makeText(this@MainActivity13, "Error: Selected image URI is null.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveMentorDetails(name: String, description: String, availability: String, rating: String, sessionPrice: String, title: String) {
        Log.d(TAG, "Saving mentor details to SharedPreferences")
        sharedPreferences.edit().apply {
            putString("name", name)
            putString("description", description)
            putString("availability", availability)
            putString("rating", rating)
            putString("sessionPrice", sessionPrice)
            putString("title", title)
            putString("imagePath", selectedImageUri.toString()) // Save image URI as string
            apply()
        }
    }


    private fun saveMentor(name: String, description: String, availability: String, rating: String, sessionPrice: String, title: String) {
        // Convert the selected image to a byte array
        val imageStream = contentResolver.openInputStream(selectedImageUri)
        val imageBytes = imageStream?.readBytes()

        // Make sure imageBytes is not null
        imageBytes?.let { bytes ->
            // Convert image bytes to Base64
            val imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)

            // Make network request to save mentor details to the server
            val stringRequest = object : StringRequest(
                Request.Method.POST,
                UPLOAD_URL,
                Response.Listener { response ->
                    // Handle the mentor data save response
                    if (response.contains("New record created successfully")) {
                        // Data saved successfully
                        startActivity(Intent(this@MainActivity13, MainActivity14::class.java))
                        finish()
                    } else {
                        // Data saving failed
                        Toast.makeText(this@MainActivity13, "Failed to save mentor data", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    // Handle error
                    Log.e(TAG, "Error saving mentor data: ${error.message}", error)
                    Toast.makeText(this@MainActivity13, "Error saving mentor data: ${error.message}", Toast.LENGTH_SHORT).show()
                }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["name"] = name
                    params["description"] = description
                    params["availability"] = availability
                    params["rating"] = rating
                    params["sessionPrice"] = sessionPrice
                    params["title"] = title
                    params["imagePath"] = imageBase64 // Use Base64 encoded image
                    return params
                }
            }

            // Add the mentor data save request to the RequestQueue
            requestQueue.add(stringRequest)
        } ?: run {
            // Handle case where imageBytes is null
            Log.e(TAG, "Error: Unable to read image bytes.")
            Toast.makeText(this@MainActivity13, "Error: Unable to read image bytes.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
