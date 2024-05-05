package com.Muhaimen.i210888

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

data class Chat(
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    var message: String = "",
    val time: String = "",
    val type: String = "",
    val editable: String = ""
)

class MainActivity16 : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val ip = "192.168.100.8"
    private lateinit var adapter: ChatAdapter
    private val chatList = ArrayList<Chat>()
    private var receiverId: String? = null
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main16)

        recyclerView = findViewById(R.id.userRV)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(this, chatList) { chatId ->
            // Handle double tap action
            updateChatEditable(chatId)
        }
        recyclerView.adapter = adapter

        val backButton: ImageButton = findViewById(R.id.back11)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val sendButton: ImageButton = findViewById(R.id.send)
        sendButton.setOnClickListener {
            sendMessage()
        }

        val galleryButton: ImageButton = findViewById(R.id.gallery)
        galleryButton.setOnClickListener {
            openGallery()
        }

        val name = intent.getStringExtra("name")
        val nameTextView = findViewById<TextView>(R.id.name)
        nameTextView.text = name

        if (name != null) {
            findUserIdByName(name)
        } else {
            Toast.makeText(this, "Name is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findUserIdByName(userName: String) {
        val url = "http://$ip/find_user_id.php?name=$userName"

        // Add log message
        Log.d("NetworkRequest", "Fetching user data for userName: $userName")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    receiverId = response.optString("id")
                    receiverId?.let {
                        Log.d("NetworkRequest", "Received receiverId: $it")
                        readMessage(it)
                    } ?: run {
                        Toast.makeText(applicationContext, "Receiver ID is null", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, "Error finding user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }


    private fun readMessage(receiverId: String) {
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        val senderId = sharedPreferences.getString("id", "") ?: ""
        val url = "http://$ip/read_message.php?id=$receiverId&senderId=$senderId"

        // Add log message
        Log.d("NetworkRequest", "Fetching messages for receiverId: $receiverId")
        Log.d("NetworkRequest", "Fetching messages for senderId: $senderId")
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                try {
                    // Log the JSON response string
                    Log.d("NetworkRequest", "Response from server: $response")

                    chatList.clear()
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val message = jsonArray.getJSONObject(i)
                        val chat = Chat(
                            message.optString("chatId", ""),
                            message.optString("senderId", ""),
                            message.optString("receiverId", ""),
                            message.optString("message", ""),
                            message.optString("time", ""),
                            message.optString("type", ""),
                            message.optString("editable", "")
                        )
                        chatList.add(chat)
                    }
                    adapter.notifyDataSetChanged()
                    if (chatList.isNotEmpty()) {
                        recyclerView.scrollToPosition(chatList.size - 1)
                    }
                } catch (e: JSONException) {
                    // Handle JSON parsing error
                    Log.e("NetworkRequest", "Error parsing JSON: ${e.message}")
                    Toast.makeText(applicationContext, "Error parsing JSON", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, "Error reading messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(stringRequest)
    }


    private fun sendMessage() {
        val messageField = findViewById<EditText>(R.id.msgFeild)
        val message = messageField.text.toString().trim()
        if (message.isNotEmpty()) {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val url = "http://$ip/send_message.php"

            // Retrieve current user ID from shared preferences
            val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
            val currentUserId = sharedPreferences.getString("id", "") ?: ""

            // Create a JSONObject to hold the message data
            val messageObject = JSONObject().apply {
                put("senderId", currentUserId) // Use the retrieved current user ID
                put("receiverId", receiverId ?: "")
                put("message", message)
                put("time", currentTime)
                put("type", "message")
            }

            // Convert the JSON object to a string
            val jsonString = messageObject.toString()

            // Add log message
            Log.d("NetworkRequest", "Sending message: $jsonString")

            val stringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener<String> { response ->
                    // Log the response from the server
                    Log.d("NetworkRequest", "Response from server: $response")

                    // Handle the response here
                    try {
                        val jsonObject = JSONObject(response)
                        val message = jsonObject.optString("message", "Default message")
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        messageField.text.clear()
                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        Toast.makeText(applicationContext, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    // Handle error
                    Toast.makeText(applicationContext, "Error sending message: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    return jsonString.toByteArray(Charset.forName("utf-8"))
                }
            }

            // Add the request to the RequestQueue
            Volley.newRequestQueue(this).add(stringRequest)
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let {
                sendImageMessage(it)
            }
        }
    }

    private fun sendImageMessage(imageUri: Uri) {
        val userId = getUserIdFromSharedPreferences() ?: ""
        val url = "http://$ip/send_message.php"
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("NetworkRequest", "Sending image message. Image URI: $imageUri")

        try {
            // Read the image data from the URI
            val inputStream = contentResolver.openInputStream(imageUri)
            val imageByteArray = inputStream?.readBytes()
            inputStream?.close()

            // Encode the image data as Base64
            val imageDataBase64 = Base64.encodeToString(imageByteArray, Base64.DEFAULT)

            // Create a JSON object to hold the message data
            val messageObject = JSONObject().apply {
                put("senderId", userId)
                put("receiverId", receiverId ?: "")
                put("message", imageDataBase64) // Send the Base64 encoded image data as the message
                put("time", currentTime)
                put("type", "image")
            }

            val jsonString = messageObject.toString()

            val stringRequest = object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    Log.d("NetworkRequest", "Response from server: $response")
                    try {
                        val jsonObject = JSONObject(response)
                        val message = jsonObject.optString("message", "Default message")
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    } catch (e: JSONException) {
                        Toast.makeText(applicationContext, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(applicationContext, "Error sending image: ${error.message}", Toast.LENGTH_SHORT).show()
                }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return jsonString.toByteArray(Charset.forName("utf-8"))
                }
            }

            // Add the request to the RequestQueue
            Volley.newRequestQueue(this).add(stringRequest)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error reading image data", Toast.LENGTH_SHORT).show()
        }
    }


    // Function to get the image file name from the URI
    @SuppressLint("Range")
    private fun getImageFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "image.jpg"
    }


    private fun getUserIdFromSharedPreferences(): String? {
        // Retrieve current user ID from shared preferences
        val sharedPreferences = getSharedPreferences("users", Context.MODE_PRIVATE)
        return sharedPreferences.getString("id", "")
    }

    private fun getFileDataFromUri(uri: Uri): ByteArray? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }



    private fun updateChatEditable(chatId: String) {
        val url = "http://$ip/update_chat.php"
        val params = JSONObject().apply {
            put("chatId", chatId)
            put("editable", "yes")
        }

        // Add log message
        Log.d("NetworkRequest", "Updating chat editable status for chatId: $chatId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            Response.Listener { response ->
                // Handle response if needed
                Toast.makeText(applicationContext, response.optString("message"), Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, "Error updating chat: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    companion object {
        private const val REQUEST_GALLERY = 100
    }
}
