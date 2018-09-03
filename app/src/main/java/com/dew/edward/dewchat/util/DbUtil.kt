package com.dew.edward.dewchat.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


/**
 *   Created by Edward on 8/31/2018.
 */
class DbUtil {
    companion object {
        val mAuth: FirebaseAuth by lazy {  FirebaseAuth.getInstance() }

        private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
        val usersRef: CollectionReference = db.collection("Users")
        val messagesRef: CollectionReference = db.collection("Messages")
        val postsRef: CollectionReference = db.collection("Posts")
        val friendsRef: CollectionReference = db.collection("Friends")
        val LikesRef: CollectionReference = db.collection("Likes")


        val fireStorageInstance by lazy { FirebaseStorage.getInstance() }
        val profileImageStorageRef = fireStorageInstance.reference.child("profile_images")

        fun signOut() = mAuth.signOut()
    }

}