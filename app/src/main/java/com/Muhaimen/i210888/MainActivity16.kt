package com.Muhaimen.i210888

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
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
                    receiverId = response.getString("id")
                    receiverId?.let {
                        readMessage(it)
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
        val url = "http://$ip/read_message.php"
        val params = JSONObject().apply {
            put("senderId", currentUserId)
            put("receiverId", receiverId)
        }

        // Add log message
        Log.d("NetworkRequest", "Fetching messages for receiverId: $receiverId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            Response.Listener { response ->
                chatList.clear()
                val jsonArray = response.getJSONArray("messages")
                for (i in 0 until jsonArray.length()) {
                    val message = jsonArray.getJSONObject(i)
                    val chat = Chat(
                        message.getString("chatId"),
                        message.getString("senderId"),
                        message.getString("receiverId"),
                        message.getString("message"),
                        message.getString("time"),
                        message.getString("type"),
                        message.getString("editable")
                    )
                    chatList.add(chat)
                }
                adapter.notifyDataSetChanged()
                if (chatList.isNotEmpty()) {
                    recyclerView.scrollToPosition(chatList.size - 1)
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, "Error reading messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun sendMessage() {
        val messageField = findViewById<EditText>(R.id.msgFeild)
        val message = messageField.text.toString().trim()
        if (message.isNotEmpty()) {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val url = "http://$ip/send_message.php"
            val params = JSONObject().apply {
                put("senderId", currentUserId)
                put("receiverId", receiverId ?: "")
                put("message", message)
                put("time", currentTime)
                put("type", "message")
            }

            // Add log message
            Log.d("NetworkRequest", "Sending message: $message")

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, params,
                Response.Listener { response ->
                    Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
                    messageField.text.clear()
                },
                Response.ErrorListener { error ->
                    Toast.makeText(applicationContext, "Error sending message: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )

            Volley.newRequestQueue(this).add(jsonObjectRequest)
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
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val url = "http://$ip/send_message.php"
        val params = JSONObject().apply {
            put("senderId", currentUserId)
            put("receiverId", receiverId ?: "")
            put("message", imageUri.toString())
            put("time", currentTime)
            put("type", "image")
        }

        // Add log message
        Log.d("NetworkRequest", "Sending image message with URI: $imageUri")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, params,
            Response.Listener { response ->
                Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, "Error sending image: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
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
                Toast.makeText(applicationContext, response.getString("message"), Toast.LENGTH_SHORT).show()
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
