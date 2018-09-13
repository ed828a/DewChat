package com.dew.edward.dewchat.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


/**
 *   Created by Edward on 9/3/2018.
 */
class DbUtil {
    companion object {
        // Authentication
        val mAuth: FirebaseAuth
            get() = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser?
            get() = FirebaseAuth.getInstance().currentUser
        val currentUserId: String?
            get() = FirebaseAuth.getInstance().currentUser?.uid

        // Cloud Firestore
        val db: FirebaseFirestore
            get() = FirebaseFirestore.getInstance()
        val usersRef: CollectionReference
            get() = FirebaseFirestore.getInstance().collection("Users")
        val messagesRef: CollectionReference
            get() = FirebaseFirestore.getInstance().collection("Messages")
        val postsRef: CollectionReference
            get() = FirebaseFirestore.getInstance().collection("Posts")
//        val friendsRef: CollectionReference
//            get() = FirebaseFirestore.getInstance().collection("Friends")
//        val LikesRef: CollectionReference
//            get() = FirebaseFirestore.getInstance().collection("Likes")

        // FirebaseStorage
        val fireStorageInstance: FirebaseStorage
            get() = FirebaseStorage.getInstance()
        val profileImageStorageRef: StorageReference
            get() = FirebaseStorage.getInstance().reference.child("profile_images")
        val postImageStorageRef: StorageReference
            get() = FirebaseStorage.getInstance().reference.child("post_images")
        val messageImageStorageRef: StorageReference
            get() = FirebaseStorage.getInstance().reference.child("message_images")

        fun signOut() = mAuth.signOut()
    }

}