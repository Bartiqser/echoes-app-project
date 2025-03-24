package com.example.echoesapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Use the correct layout file

        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logout_button)

        logoutButton.setOnClickListener {
            auth.signOut()
            // Navigate back to MainActivityq
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Prevent user from going back to HomeActivity after logout
        }
    }
}