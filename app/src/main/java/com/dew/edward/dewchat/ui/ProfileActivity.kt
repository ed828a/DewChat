package com.dew.edward.dewchat.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.model.UserData
import com.dew.edward.dewchat.util.DbUtil


import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private val tag = this::class.java.simpleName

    private var countFriends = 0
    private var countPosts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)



        DbUtil.postsRef
                .whereEqualTo("userId", DbUtil.currentUserId)
                .addSnapshotListener(this) { querySnapshot, exception ->
                    if (exception != null) {
                        Log.d(tag, "Adding listener failed: ${exception.message}")
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty){
                        countPosts = querySnapshot.size()
                        numberOfPostsButton.text = "$countPosts Posts"
                    } else {
                        numberOfPostsButton.text = "No Posts yet"
                    }
                }

        DbUtil.usersRef.document(DbUtil.currentUserId!!).collection("Friends")
                .addSnapshotListener(this){querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                    if (exception != null) {
                        Log.d(tag, "Adding listener failed: ${exception.message}")
                        return@addSnapshotListener
                    }
                    if (querySnapshot != null && !querySnapshot.isEmpty){
                        countFriends = querySnapshot.size()
                        numberOfFriendsButton.text = "$countFriends Friends"
                    } else {
                        numberOfFriendsButton.text = "No Friends Yet"
                    }
                }

        DbUtil.usersRef.document(DbUtil.currentUserId!!)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(UserData::class.java)
                    user?.let{
                        if (it.profileImageUrl.isNotEmpty())
                        Picasso.get().load(it.profileImageUrl).into(profileImagePicture)

                        profileUserFullName.text = it.userFullName
                        profileCountry.text = it.country
                        profileDOB.text = it.DOB
                        profileGender.text = it.gender
                        profileRelationship.text = it.relationship
                        profileStatus.text = it.status
                    }
                }

        numberOfFriendsButton.setOnClickListener {
            sendUserToFriendsActivity()
        }

        numberOfPostsButton.setOnClickListener {
            sendUserToMyPostActivity()
        }
    }




    private fun sendUserToMyPostActivity() {
        startActivity(Intent(this@ProfileActivity, MyPostsActivity::class.java))
        finish()
    }

    private fun sendUserToFriendsActivity() {
        startActivity(Intent(this@ProfileActivity, FriendsActivity::class.java))
        finish()
    }
}
