package com.example.echoesapp

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddPostActivity : AppCompatActivity() {

    private lateinit var loadingDialog: Dialog
    private lateinit var backArrowImageView: ImageView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var descriptionInputLayout: TextInputLayout
    private lateinit var fileNameTextView: TextView
    private lateinit var imageNameTextView: TextView
    private lateinit var genreSpinner: Spinner
    private lateinit var chooseFileButton: ImageButton
    private lateinit var chooseImageButton: ImageButton
    private lateinit var uploadButton: Button

    private var selectedFileUri: Uri? = null
    private var selectedImageUri: Uri? = null

    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val FILE_REQUEST_CODE = 1001
    private val IMAGE_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        loadingDialog = createLoadingDialog(this)
        backArrowImageView = findViewById(R.id.backArrow_imageView)
        titleEditText = findViewById(R.id.title_editText)
        descriptionEditText = findViewById(R.id.description_editText)
        titleInputLayout = findViewById(R.id.title_inputLayout)
        descriptionInputLayout = findViewById(R.id.description_inputLayout)
        fileNameTextView = findViewById(R.id.file_name_textView)
        imageNameTextView = findViewById(R.id.image_name_textView)
        genreSpinner = findViewById(R.id.genre_spinner)
        chooseFileButton = findViewById(R.id.choose_file_button)
        chooseImageButton = findViewById(R.id.choose_image_button)
        uploadButton = findViewById(R.id.upload_button)

        backArrowImageView.setOnClickListener {
            finish()
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.genres,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        genreSpinner.adapter = adapter

        chooseFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/mpeg"
            startActivityForResult(intent, FILE_REQUEST_CODE)
        }

        chooseImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_REQUEST_CODE)
        }

        uploadButton.setOnClickListener {
            validateAndUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                FILE_REQUEST_CODE -> {
                    selectedFileUri = data.data
                    val mime = getMimeType(selectedFileUri)
                    if (mime != "audio/mpeg") {
                        selectedFileUri = null
                        fileNameTextView.text = ".MP3 File..."
                        Toast.makeText(this, "Please select a valid .mp3 file", Toast.LENGTH_SHORT).show()
                    } else {
                        fileNameTextView.text = selectedFileUri?.lastPathSegment.toString() + ".mp3"
                    }
                }

                IMAGE_REQUEST_CODE -> {
                    selectedImageUri = data.data
                    val extension = getMimeType(selectedImageUri)
                    if (extension == null || !extension.startsWith("image")) {
                        selectedImageUri = null
                        imageNameTextView.text = "Upload an image..."
                        Toast.makeText(this, "Please select a valid image file", Toast.LENGTH_SHORT).show()
                    } else {
                        imageNameTextView.text = selectedImageUri?.lastPathSegment.toString()
                    }
                }
            }
        }
    }

    private fun validateAndUpload() {
        loadingDialog.show()
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val genre = genreSpinner.selectedItem?.toString() ?: ""

        var isValid = true

        titleInputLayout.error = null
        descriptionInputLayout.error = null

        if (title.isEmpty()) {
            loadingDialog.dismiss()
            titleInputLayout.error = "Title cannot be empty"
            isValid = false
        }

        if (description.isEmpty()) {
            loadingDialog.dismiss()
            descriptionInputLayout.error = "Description cannot be empty"
            isValid = false
        }

        if (selectedFileUri == null) {
            loadingDialog.dismiss()
            Toast.makeText(this, "Please choose an MP3 file", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (selectedImageUri == null) {
            loadingDialog.dismiss()
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid){
            loadingDialog.dismiss()
            return
        }

        uploadData(title, description, genre)
    }

    private fun uploadData(title: String, description: String, genre: String) {
        val songId = UUID.randomUUID().toString()
        val userId = currentUser?.uid ?: return

        val fileRef = storage.reference.child("songs/$songId/audio.mp3")
        val imageRef = storage.reference.child("songs/$songId/cover.jpg")

        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    fileRef.putFile(selectedFileUri!!)
                        .addOnSuccessListener {
                            fileRef.downloadUrl.addOnSuccessListener { fileUrl ->
                                val song = hashMapOf(
                                    "song_id" to songId,
                                    "artist_id" to userId,
                                    "title" to title,
                                    "description" to description,
                                    "genre" to genre,
                                    "cover_url" to imageUrl.toString(),
                                    "file_url" to fileUrl.toString(),
                                    "upload_date" to System.currentTimeMillis(),
                                    "favorites_count" to 0
                                )

                                db.collection("songs")
                                    .document(songId)
                                    .set(song)
                                    .addOnSuccessListener {
                                        loadingDialog.dismiss()
                                        Toast.makeText(this, "Song uploaded successfully!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        loadingDialog.dismiss()
                                        Toast.makeText(this, "Failed to upload song data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            loadingDialog.dismiss()
                            Toast.makeText(this, "Failed to upload MP3 file", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getMimeType(uri: Uri?): String? {
        return uri?.let {
            contentResolver.getType(it)
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
}
