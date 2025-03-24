package com.example.echoesapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Replace with your layout file name

        auth = FirebaseAuth.getInstance()
        loginButton = findViewById(R.id.login_button) // Make sure these IDs are in your layout
        signupButton = findViewById(R.id.signin_button)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            // User is logged in, navigate to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Prevent the user from coming back to MainActivity without logging out
        } else {
            // User is not logged in, show login/signup options
            loginButton.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            signupButton.setOnClickListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
        }
    }
}