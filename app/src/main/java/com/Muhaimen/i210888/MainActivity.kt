package com.Muhaimen.i210888

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("users", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences?.getBoolean("isLoggedIn", false) ?: false
        if (isLoggedIn) {
            navigateToHome()
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToSignup() {
        Handler().postDelayed({
            val intent = Intent(this@MainActivity, MainActivity4::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }

    private fun navigateToLogin() {
        Handler().postDelayed({
            val intent = Intent(this@MainActivity, Main3Activity::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }

    private fun navigateToHome() {
        Handler().postDelayed({
            val intent = Intent(this@MainActivity, MainActivity8::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }
}
