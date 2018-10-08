package com.dew.edward.dewchat.util


/**
 *   Created by Edward on 9/3/2018.
 */

// RequestCode
const val RC_SIGN_IN = 1
const val RC_IMAGE_PICK = 2
const val RC_FACEBOOK_LOGIN = 64206

// Values
const val VALUE_NOT_FRIEND = "not_friend"
const val VALUE_FRIEND_REQUEST_SENT = "friend_request_sent"
const val VALUE_FRIEND_REQUEST_RECEIVED = "friend_request_received"
const val VALUE_BECOME_FRIEND = "become_friend"
const val VALUE_INVALID_PASSWORD = "The password is invalid or the user does not have a password."
const val VALUE_NO_ACCOUNT = "There is no user record corresponding to this identifier. The user may have been deleted."

// Field Keys
const val KEY_FRIENDSHIP = "friendship"
const val KEY_DATE = "date"

// Intent Extra Keys
const val KEY_EXTRA_DISPLAY_MY_POSTS = "display_my_posts"
const val KEY_EXTRA_LISTED_USER_ID = "USER_ID"
const val KEY_EXTRA_USER_NAME = "USER_NAME"


// Shared Preference
const val SHARED_PREF_NAME = "dewpreferences"
const val KEY_ACCESS_TOKEN = "registrationToken"