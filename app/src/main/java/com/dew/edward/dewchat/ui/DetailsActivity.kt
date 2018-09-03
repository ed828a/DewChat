package com.dew.edward.dewchat.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.model.User
import com.dew.edward.dewchat.util.AppUtil
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.RC_IMAGE_PICK
import com.dew.edward.dewchat.util.toast
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {
    private val tag: String = this.javaClass.simpleName


    private lateinit var loadingBar: ProgressDialog
    private var registration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        loadingBar = ProgressDialog(this)

        updateAccountDetailsButton.setOnClickListener {
            updateAccountDetails()
        }

        detailsProfileImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, RC_IMAGE_PICK)
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUserId = DbUtil.mAuth.currentUser?.uid
        currentUserId?.let {
            registration = DbUtil.usersRef.document(it)
                    .addSnapshotListener { documentSnapshot,
                                           exception ->

                        if (exception != null) {
                            Log.d(tag, "Listen failed. ${exception.message}")
                            return@addSnapshotListener
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            val user = documentSnapshot.toObject(User::class.java)
                            user?.let {
                                Picasso.get().load(it.profileImageUrl).into(detailsProfileImage)
                                detailsStatus.setText(it.status)
                                detailsUserName.setText(it.username)
                                detailsFullName.setText(it.userFullName)
                                detailsCountry.setText(it.country)
                                detailsDOB.setText(it.DOB)
                                detailsGender.setText(it.gender)
                                detailsRelationship.setText(it.relationship)
                            }
                        } else {
                            Log.d(tag, "No such document: $currentUserId")
                        }
                    }
        }
    }

    private fun updateAccountDetails() {
        val username = detailsStatus.text.toString().trim()
        val fullname = detailsFullName.text.toString().trim()
        val country = detailsCountry.text.toString().trim()
        val gender = detailsGender.text.toString().trim()
        val userDOB = detailsDOB.text.toString().trim()
        val status = detailsStatus.text.toString().trim()
        val relationship = detailsRelationship.text.toString().trim()

        val usermap = mapOf<String, Any>(
                "username" to username,
                "userFullName" to fullname,
                "gender" to gender,
                "country" to country,
                "DOB" to userDOB,
                "status" to status,
                "relationship" to relationship
        )

        val currentUserId = DbUtil.mAuth.currentUser?.uid
        currentUserId?.let {
            DbUtil.usersRef.document(currentUserId)
                    .set(usermap, SetOptions.merge())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            "Your account details updated successfully. ".toast(this)
                            sendUserToMainActivity()
                        } else {
                            Log.d(tag, "Error: ${task.exception.toString()}")
                        }
                    }
        }
    }

    private fun sendUserToMainActivity() {
        val intent = Intent(this@DetailsActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUrl = data.data
            CropImage.activity(imageUrl)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {

            AppUtil.storeImage(data, resultCode, loadingBar, this@DetailsActivity){
                startActivity(Intent(this@DetailsActivity, DetailsActivity::class.java))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        registration?.remove()
    }
}
