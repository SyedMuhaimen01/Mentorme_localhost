package com.Muhaimen.i210888

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import java.io.InputStream
import com.bumptech.glide.request.target.Target
import org.json.JSONException
import org.json.JSONObject

class MainActivity22 : AppCompatActivity() {

    private lateinit var countrySpinner: Spinner
    private lateinit var citySpinner: Spinner
    private lateinit var requestQueue: RequestQueue
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private val ip = "192.168.100.8"
    private val UPDATE_URL = "http://$ip/update_user_data.php"
    private val UPLOAD_URL = "http://$ip/uploadImage.php"
    private val FETCH_IMAGE_URL = "http://$ip/getImage.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main22)

        requestQueue = Volley.newRequestQueue(this)
        val country = arrayOf("Pakistan", "India", "China")
        val city = arrayOf("Islamabad", "Karachi", "Lahore")

        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, country)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, city)

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        countrySpinner = findViewById(R.id.spinner)
        citySpinner = findViewById(R.id.spinner2)

        countrySpinner.adapter = adapter1
        citySpinner.adapter = adapter2

        val backButton = findViewById<ImageButton>(R.id.back13)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val updateButton = findViewById<TextView>(R.id.update)
        updateButton.setOnClickListener {
            val inputName = findViewById<EditText>(R.id.nameEditText).text.toString()
            val inputEmail = findViewById<EditText>(R.id.emailEditText).text.toString()
            val inputContactNumber = findViewById<EditText>(R.id.contactNumberEditText).text.toString()
            val inputCountry = countrySpinner.selectedItem.toString()
            val inputCity = citySpinner.selectedItem.toString()

            updateUserData(inputName, inputEmail, inputContactNumber, inputCountry, inputCity)
        }

        val selectImageButton = findViewById<ShapeableImageView>(R.id.profileImageView)
        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        fetchProfileImage()
        loadUserData()
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



    private fun updateUserData(name: String, email: String, contactNumber: String, country: String, city: String) {
        val stringRequest = object : StringRequest(
            Method.POST,
            UPDATE_URL,
            Response.Listener<String> { response ->
                if (response.contains("User data updated successfully")) {
                    Toast.makeText(this, "User data updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = getUserIdFromSharedPreferences() ?: ""
                params["name"] = name
                params["email"] = email
                params["contactNumber"] = contactNumber
                params["country"] = country
                params["city"] = city
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun uploadImage() {
        val userId = getUserIdFromSharedPreferences() ?: ""
        imageUri?.let { uri ->
            val byteArray = getFileDataFromUri(uri)
            byteArray?.let { data ->
                val encodedImage = Base64.encodeToString(data, Base64.DEFAULT)
                val stringRequest = object : StringRequest(
                    Method.POST, UPLOAD_URL,
                    Response.Listener { response ->
                        // Assuming response contains the URL of the uploaded image
                        updateProfilePicture(response)
                        // Save the image URL in SharedPreferences
                        saveProfilePictureToSharedPreferences(response)
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["image"] = encodedImage
                        params["id"] = userId
                        return params
                    }
                }
                requestQueue.add(stringRequest)
            }
        }
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

    private fun loadUserData() {
        val userId = getUserIdFromSharedPreferences()
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val city = sharedPreferences.getString("city", "")
        val country = sharedPreferences.getString("country", "")
        val contactNumber = sharedPreferences.getString("contactNumber", "")
        val imageUrl = sharedPreferences.getString("profilePicture", "")

        Log.d("ImageLoading", "User image URL: $imageUrl")

        findViewById<EditText>(R.id.nameEditText).setText(name)
        findViewById<EditText>(R.id.emailEditText).setText(email)
        findViewById<EditText>(R.id.contactNumberEditText).setText(contactNumber)

        val countryIndex = (countrySpinner.adapter as ArrayAdapter<String>).getPosition(country)
        val cityIndex = (citySpinner.adapter as ArrayAdapter<String>).getPosition(city)

        countrySpinner.setSelection(countryIndex)
        citySpinner.setSelection(cityIndex)

        // Load the image using Glide if the image URL is not null or empty
        if (!imageUrl.isNullOrEmpty()) {
            // Concatenate the base URL with the image URL retrieved from SharedPreferences
            val fullImageUrl = "http://$ip/$imageUrl"

            Log.d("ImageLoading", "Full Image URL: $fullImageUrl")

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
                .into(findViewById(R.id.profileImageView))
        } else {
            Log.d("ImageLoading", "Image URL is null or empty")
        }
    }


    private fun updateProfilePicture(imageUrl: String) {
        val id = getUserIdFromSharedPreferences() ?: ""
        val stringRequest = object : StringRequest(
            Method.POST,
            UPDATE_URL,
            Response.Listener<String> { response ->
                if (response.contains("User data updated successfully")) {
                    saveProfilePictureToSharedPreferences(imageUrl)
                    Toast.makeText(this, "User data updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                params["profilePicture"] = imageUrl
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun getFileDataFromUri(uri: Uri): ByteArray? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        return inputStream?.buffered()?.use { it.readBytes() }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data?.data

            val imageView = findViewById<ShapeableImageView>(R.id.profileImageView)
            val radius = 100f
            imageView.shapeAppearanceModel = imageView.shapeAppearanceModel
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

            imageView.setImageURI(imageUri)

            uploadImage()
        }
    }

    private fun getUserIdFromSharedPreferences(): String? {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        return sharedPreferences.getString("id", null)
    }

    private fun saveProfilePictureToSharedPreferences(imageUrl: String) {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profilePicture", imageUrl)
        editor.apply()
    }
}
