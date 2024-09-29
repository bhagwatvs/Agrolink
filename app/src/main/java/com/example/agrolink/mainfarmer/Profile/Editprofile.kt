package com.example.agrolink.mainfarmer.Profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.agrolink.databinding.ActivityEditprofileBinding
import com.example.agrolink.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : Activity() {

    private lateinit var binding: ActivityEditprofileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val PICK_IMAGE_REQUEST = 1
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user_id" // Fallback ID for testing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore and Authentication
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Ensure user is authenticated
        if (auth.currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Load existing data
        loadProfileData()

        // Set up image selection
        binding.editProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Set up save button
        binding.saveProfileButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun loadProfileData() {
        firestore.collection("farmers_profile").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name") ?: ""
                    val age = documentSnapshot.getString("age") ?: ""
                    val location = documentSnapshot.getString("location") ?: ""
                    val experience = documentSnapshot.getString("experience") ?: ""
                    val imageUrl = documentSnapshot.getString("imageUrl") ?: ""

                    binding.editFarmerName.setText(name)
                    binding.editFarmerAge.setText(age)
                    binding.editFarmerLocation.setText(location)
                    binding.editFarmerExperience.setText(experience)

                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(binding.editProfileImage)
                }
            }
            .addOnFailureListener {
                // Handle the error
                Log.e("EditProfileActivity", "Error loading profile data", it)
            }
    }

    private fun saveProfileData() {
        val name = binding.editFarmerName.text.toString()
        val age = binding.editFarmerAge.text.toString()
        val location = binding.editFarmerLocation.text.toString()
        val experience = binding.editFarmerExperience.text.toString()

        val profileData = mapOf(
            "name" to name,
            "age" to age,
            "location" to location,
            "experience" to experience
        )

        firestore.collection("farmers_profile").document(userId).set(profileData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finish()  // Close the activity
                } else {
                    // Handle the error
                    Log.e("EditProfileActivity", "Error saving profile data", task.exception)
                    Toast.makeText(this, "Error saving data: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            binding.editProfileImage.setImageURI(imageUri)
        }
    }
}
