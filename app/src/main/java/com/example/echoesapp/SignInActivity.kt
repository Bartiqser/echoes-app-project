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
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var loadingDialog: Dialog
    private lateinit var loginLink: TextView
    private lateinit var backArrowImageView: ImageView
    private lateinit var emailEditText: TextInputEditText
    private lateinit var confirmEmailEditText: TextInputEditText
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var confirmEmailInputLayout: TextInputLayout
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadingDialog = createLoadingDialog(this)
        loginLink = findViewById(R.id.login_link)
        backArrowImageView = findViewById(R.id.backArrow_imageView)
        emailEditText = findViewById(R.id.email_editText)
        confirmEmailEditText = findViewById(R.id.confirmEmail_editText)
        usernameEditText = findViewById(R.id.username_editText)
        passwordEditText = findViewById(R.id.password_editText)
        registerButton = findViewById(R.id.signup_button)
        emailInputLayout = findViewById(R.id.email_inputLayout)
        confirmEmailInputLayout = findViewById(R.id.confirmEmail_inputLayout)
        usernameInputLayout = findViewById(R.id.username_inputLayout)
        passwordInputLayout = findViewById(R.id.password_inputLayout)

        backArrowImageView.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            registerUser()
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser() {
        loadingDialog.show()
        val email = emailEditText.text.toString().trim()
        val confirmEmail = confirmEmailEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        emailInputLayout.error = null
        confirmEmailInputLayout.error = null
        usernameInputLayout.error = null
        passwordInputLayout.error = null

        if (email != confirmEmail) {
            confirmEmailInputLayout.error = "Emails do not match"
            loadingDialog.dismiss()
            return
        }

        if (username.isEmpty()) {
            usernameInputLayout.error = "Username cannot be empty"
            loadingDialog.dismiss()
            return
        }

        if (!isValidPassword(password)) {
            passwordInputLayout.error = "Password must be at least 6 characters, contain 1 number, and 1 capital letter"
            loadingDialog.dismiss()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Registration", "createUserWithEmail:success")
                    val userId = auth.currentUser?.uid

                    val userMap = hashMapOf(
                        "userId" to userId,
                        "username" to username,
                        "email" to email,
                        "avatar_url" to "avatars/default_avatar.png"
                    )

                    if (userId != null) {
                        db.collection("users")
                            .document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                loadingDialog.dismiss()
                                Log.d("Firestore", "DocumentSnapshot added with ID: $userId")
                                Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                loadingDialog.dismiss()
                                Log.w("Firestore", "Error adding document", e)
                                Toast.makeText(baseContext, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        loadingDialog.dismiss()
                        Toast.makeText(baseContext, "User ID is null", Toast.LENGTH_SHORT).show()
                        Log.e("Registration", "User ID is null")
                    }
                } else {
                    loadingDialog.dismiss()
                    Log.w("Registration", "createUserWithEmail:failure", task.exception)
                }
            }
    }

    private fun isValidPassword(password: String): Boolean {
        val hasMinLength = password.length >= 6
        val hasNumber = password.any { it.isDigit() }
        val hasCapitalLetter = password.any { it.isUpperCase() }
        return hasMinLength && hasNumber && hasCapitalLetter
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
}