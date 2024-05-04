package com.Muhaimen.i210888

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity25 : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main25)

        // Set up click listener for home button
        val homeButton: ImageButton = findViewById(R.id.home)
        homeButton.setOnClickListener {
            // Navigate to the last activity
            onBackPressed()
        }

        // Start the service
        startService()
    }

    private fun startService() {
        startService(FirebaseUpdateService.getIntent(this))
    }
}
