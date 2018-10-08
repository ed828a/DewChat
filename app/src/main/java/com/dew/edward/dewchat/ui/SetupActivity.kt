package com.dew.edward.dewchat.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.service.FCMService
import com.dew.edward.dewchat.util.*
import com.google.firebase.firestore.SetOptions
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_setup.*

class SetupActivity : AppCompatActivity() {
    private val tag: String = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        setupUserProfileImage.setOnClickListener {
            saveInputFields()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, RC_IMAGE_PICK)
        }

        setupSaveButton.setOnClickListener {
            saveUserAccountInformation()
        }

        val currentUserId = DbUtil.mAuth.currentUser?.uid
        currentUserId?.let {
            DbUtil.usersRef.document(it)
                    .addSnapshotListener { documentSnapshot,
                                           exception ->
                        if (exception != null) {
                            Log.d(tag, "Exception: ${exception.message}")
                            "You don't have a profile yet!".toast(this)
                            return@addSnapshotListener
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            if (documentSnapshot.contains("profileImageUrl")) {
                                val imageUrl = documentSnapshot.get("profileImageUrl").toString()
                                Picasso.get().load(imageUrl).into(setupUserProfileImage)
                            }
                            if (documentSnapshot.contains("username")) {
                                val username = documentSnapshot.get("username").toString()
                                setupUserName.setText(username)
                            }
                            if (documentSnapshot.contains("userFullName")) {
                                val fullname = documentSnapshot.get("userFullName").toString()
                                setupUserFullName.setText(fullname)
                            }
                            if (documentSnapshot.contains("country")) {
                                val country = documentSnapshot.get("country").toString()
                                setupUserCountry.setText(country)
                            }

                        }
                    }
        }


    }

    private fun saveInputFields() {
        val inputFieldsMap = mutableMapOf<String, Any>()
        val username = setupUserName.text.toString().trim()
        if (username.isNotEmpty()) {
            inputFieldsMap["username"] = username
        }

        val userFullName = setupUserFullName.text.toString().trim()
        if (userFullName.isNotEmpty()) {
            inputFieldsMap["userFullName"] = userFullName
        }

        val country = setupUserCountry.text.toString().trim()
        if (country.isNotEmpty()) {
            inputFieldsMap["country"] = country
        }
        DbUtil.usersRef.document(DbUtil.mAuth.currentUser?.uid!!)
                .set(inputFieldsMap, SetOptions.merge())

        val sharePrefManager = SharePrefManager(applicationContext)
        val token = sharePrefManager.getToken()
        if (token != null)
            FCMService.addTokenToFirestore(token)

    }

    private fun saveUserAccountInformation() {
        val username = setupUserName.text.toString().trim()
        if (username.isEmpty()) {
            "please input your name...".toast(this@SetupActivity)
            return
        }

        val userFullName = setupUserFullName.text.toString().trim()
        if (userFullName.isEmpty()) {
            "please input your full name...".toast(this@SetupActivity)
            return
        }

        val country = setupUserCountry.text.toString().trim()
        if (country.isEmpty()) {
            "please input your country...".toast(this@SetupActivity)
            return
        }

        setupProgressBar.visibility = View.VISIBLE

        val userInfoMap = mutableMapOf<String, Any>(
                "username" to username,
                "userFullName" to userFullName,
                "country" to country,
                "gender" to "none",
                "DOB" to "none",
                "relationship" to "none",
                "status" to "none"
        )
        DbUtil.usersRef.document(DbUtil.mAuth.currentUser?.uid!!)
                .set(userInfoMap, SetOptions.merge())
                .addOnCompleteListener { task ->
                    setupProgressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        sendUserToMainActivity()
                        "Your account has been created successfully.".toast(this)
                    } else {
                        "Error: ${task.exception?.message}".toast(this)
                    }
                }

    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@SetupActivity, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            AppUtil.storeImage(data, resultCode, setupProgressBar,
                    this@SetupActivity) {
                startActivity(Intent(this, SetupActivity::class.java))
            }
        }
    }
}
