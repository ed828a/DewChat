package com.dew.edward.dewchat.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.di.DewChatApp
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.AppUtil
import com.dew.edward.dewchat.util.AppUtil.Companion.showProgressDialog
import com.dew.edward.dewchat.util.RC_SIGN_IN
import com.dew.edward.dewchat.util.toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var loadingBar: ProgressDialog
    private lateinit var googleSignInClient: GoogleApiClient

    private val tag: String = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loadingBar = ProgressDialog(this)

        registerAccountLink.setOnClickListener {
            DbUtil.signOut()
            sendUserToRegisterActivity()
        }

        loginButton.setOnClickListener {
            val loginEmail = inputTextLoginEmail.text.trim().toString()
            val loginPassword = inputTextLoginPassword.text.trim().toString()
            if (loginEmail.isNotEmpty() && loginPassword.isNotEmpty()) {
                showProgressDialog(loadingBar, "Google Sign in", "You are logging in your account...")

                DbUtil.mAuth
                        .signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            loadingBar.dismiss()
                            if (task.isSuccessful) {
                                val currentUserId = DbUtil.mAuth.currentUser?.uid
                                Log.d(tag, "currentUserId = $currentUserId")
                                currentUserId?.let {userId ->
                                    listeningUser(userId, loadingBar)
                                }
                            } else {
                                "You don't have a account yet. Please create your account...".toast(this)
                                Log.d(tag, "Login failed: ${task.exception?.message}")
                                sendUserToRegisterActivity()
                            }
                        }
            } else {
                "Please make sure that email and password both are input...".toast(this)
            }
        }

        forgetPasswordLink.setOnClickListener {
            sendUserToResetPasswordActivity()
        }

        // Configure Google Sign in
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleApiClient.Builder(this@LoginActivity)
                .enableAutoManage(this@LoginActivity, GoogleApiClient.OnConnectionFailedListener {
                    "Connection to Google SignIn failed".toast(this)
                    Log.d(tag, "Connection to Google SignIn failed: ${it.errorMessage}")
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        googleSignInButton.setOnClickListener {
            showProgressDialog(loadingBar, "Google Sign in",
                    "Please wait for logging in with your google account...")
            // start sign-in
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleSignInClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

    }

    private fun verifyEmailAddress() {

        val isEmailVerified = FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
        if (isEmailVerified) {
            sendUserToMainActivity()
        } else {
            "Please verify your email first...".toast(this)
            DbUtil.signOut()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                // Firebase Authentication with Google Account
                val credential: AuthCredential = GoogleAuthProvider.getCredential(account?.idToken, null)
                DbUtil.mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            loadingBar.dismiss()

                            if (task.isSuccessful) {
                                Log.d(tag, "signIn With Auth Credential: succeeded")
                                sendUserToMainActivity()
                            } else {
                                Log.d(tag, "signIn With Auth Credential: failed, Error: ${task.exception?.message}")
                                "signIn With Google failed, due to ${task.exception?.message}".toast(this)
                            }
                        }
            } else {
                loadingBar.dismiss()
                "sign-in failed: ${result?.toString()}".toast(this)
            }
        }
    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        if (!this@LoginActivity.isFinishing) {
            finish()  // won't come back
        }
    }

    private fun sendUserToResetPasswordActivity() {
        val resetIntent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
        startActivity(resetIntent)
    }

    private fun sendUserToRegisterActivity() {
        val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    private fun sendUserToSetupActivity() {
        val setupIntent = Intent(this@LoginActivity, SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        if (!this@LoginActivity.isFinishing) {
            finish()  // won't come back
        }
    }

    override fun onStart() {
        super.onStart()
        if (DbUtil.mAuth.currentUser != null) {

            val currentUserId = DbUtil.mAuth.currentUser?.uid
            Log.d(tag, "currentUserId = $currentUserId")
            currentUserId?.let {userId ->
                listeningUser(userId, loadingBar)
            }
        }
    }

    private fun listeningUser(userId: String, loadingBar: ProgressDialog){
        AppUtil.showProgressDialog(loadingBar, "Check Profile", "It's loading your account...")
        DbUtil.usersRef.document("$userId")
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    AppUtil.dismissProgressDialog(loadingBar)
                    if (firebaseFirestoreException != null) {
                        Log.d(tag, "Users listener error: ${firebaseFirestoreException.message}")
                        return@addSnapshotListener
                    }
                    Log.d(tag, "currentUserId = ${DbUtil.mAuth.currentUser?.uid}")
                    if (documentSnapshot == null || !documentSnapshot.contains("status")) {
                        Log.d(tag, "user profile doesn't existing. ")
                        sendUserToSetupActivity()
                    } else {
                        "Login is successful...".toast(this) // this toast is redundant
                        if (DewChatApp.isNeedEmailVerification) {
                            verifyEmailAddress()  // for email verification
                        } else {
                            sendUserToMainActivity()  // without email verification
                        }
                    }
                }
    }
}
