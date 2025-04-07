package com.example.echoesapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loadingDialog: Dialog
    private lateinit var backArrowImageView: ImageView
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var registerLink: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = FirebaseAuth.getInstance()

        loadingDialog = createLoadingDialog(this)
        backArrowImageView = findViewById(R.id.backArrow_imageView)
        emailEditText = findViewById(R.id.email_editText)
        passwordEditText = findViewById(R.id.password_editText)
        loginButton = findViewById(R.id.login_button)
        emailInputLayout = findViewById(R.id.email_inputLayout)
        passwordInputLayout = findViewById(R.id.password_inputLayout)
        registerLink = findViewById(R.id.register_link)

        backArrowImageView.setOnClickListener {
            finish()
        }

        loginButton.setOnClickListener {
            loginUser()
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun createLoadingDialog(context: Context): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    private fun loginUser() {
        loadingDialog.show()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        emailInputLayout.error = null
        passwordInputLayout.error = null

        if (email.isEmpty()) {
            emailInputLayout.error = "Email cannot be empty"
            loadingDialog.dismiss()
            return
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "Password cannot be empty"
            loadingDialog.dismiss()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    loadingDialog.dismiss()
                    Log.d("Login", "signInWithEmail:success")
                    Toast.makeText(baseContext, "Login successful.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    loadingDialog.dismiss()
                    Log.w("Login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    emailInputLayout.error = "Invalid Credentials"
                    passwordInputLayout.error = "Invalid Credentials"
                }
            }
    }
}