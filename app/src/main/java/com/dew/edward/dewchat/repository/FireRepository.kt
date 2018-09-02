package com.dew.edward.dewchat.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


/**
 *   Created by Edward on 8/31/2018.
 */
object FireRepository {
    val mAuth: FirebaseAuth by lazy {  FirebaseAuth.getInstance() }

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val usersRef: CollectionReference = firestore.collection("Users")
    val messagesRef: CollectionReference = firestore.collection("Messages")
    val postsRef: CollectionReference = firestore.collection("Posts")
    val friends: CollectionReference = firestore.collection("Friends")
    val Likes: CollectionReference = firestore.collection("Likes")


    val firestorage by lazy { FirebaseStorage.getInstance() }
    val profileImageStorageRef = firestorage.reference.child("profile_images")

    fun signOut() = mAuth.signOut()

}