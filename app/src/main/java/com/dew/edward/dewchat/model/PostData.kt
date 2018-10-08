package com.dew.edward.dewchat.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.*


/**
 *   Created by Edward on 9/5/2018.
 */
data class PostData(
        val userId: String = "",
        val userFullName: String = "",
        val userProfileImage: String = "",
        val postText: String = "",
        val postImage: String = "",
        val countPosts: Int = 0,
        val date: String = "",
        val time: String = "",
        val postIndex: Long = 0
)