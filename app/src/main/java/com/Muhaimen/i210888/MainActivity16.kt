package com.Muhaimen.i210888


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
    private var currentUserId: String = "" // Assuming this is stored in SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main16)

        recyclerView = findViewById(R.id.userRV)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter( this,chatList) { chatId ->
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
        val url = "http://$ip/find_user_id.php"
        val params = HashMap<String, String>()
        params["userName"] = userName

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
            Response.Listener { response ->
                receiverId = response.getString("id")
                receiverId?.let {
                    readMessage(it)
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
        val params = HashMap<String, String>()
        params["senderId"] = currentUserId
        params["receiverId"] = receiverId

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
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
            val params = HashMap<String, String>()
            params["senderId"] = currentUserId
            params["receiverId"] = receiverId ?: ""
            params["message"] = message
            params["time"] = currentTime
            params["type"] = "message"

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, JSONObject(params as Map<*, *>?),
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
        val params = HashMap<String, String>()
        params["senderId"] = currentUserId
        params["receiverId"] = receiverId ?: ""
        params["message"] = imageUri.toString()
        params["time"] = currentTime
        params["type"] = "image"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
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
        val params = HashMap<String, String>()
        params["chatId"] = chatId
        params["editable"] = "yes"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
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
