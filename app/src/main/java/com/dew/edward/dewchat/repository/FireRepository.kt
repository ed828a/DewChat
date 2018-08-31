package com.dew.edward.dewchat.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


/**
 *   Created by Edward on 8/31/2018.
 */
class FireRepository {
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUserId = mAuth.currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    val firestorage = FirebaseStorage.getInstance()
    val firestoragRef = firestorage.reference


}