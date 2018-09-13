package com.dew.edward.dewchat.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.app.DewChatApp
import com.dew.edward.dewchat.util.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private val tag: String = this.javaClass.simpleName

    private lateinit var googleSignInClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        registerAccountLink.setOnClickListener {
            DbUtil.signOut()
            sendUserToRegisterActivity()
        }

        loginButton.setOnClickListener {
            AppUtil.hideKeyboard(this)

            val loginEmail = inputTextRegisterEmail.text.trim().toString()
            val loginPassword = inputTextLoginPassword.text.trim().toString()
            if (loginEmail.isNotEmpty() && loginPassword.isNotEmpty()) {
                loginProgressBar.visibility = View.VISIBLE

                DbUtil.mAuth
                        .signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            loginProgressBar.visibility = View.GONE
                            if (task.isSuccessful) {
                                val currentUserId = DbUtil.mAuth.currentUser?.uid
                                Log.d(tag, "currentUserId = $currentUserId")
                                currentUserId?.let { userId ->
                                    listeningUser(userId, loginProgressBar)
                                }
                            } else {
                                Log.d(tag, "Login failed: ${task.exception?.message}")
                                val message = task.exception?.message
                                when (message) {
                                    VALUE_INVALID_PASSWORD -> {
                                        message.toast(this)
                                    }
                                    VALUE_NO_ACCOUNT -> {
                                        "You don't have a account yet. Please create your account...".toast(this)
                                        sendUserToRegisterActivity()
                                    }
                                    else -> {
                                        message?.toast(this)
                                    }
                                }
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
            loginProgressBar.visibility = View.VISIBLE
            // start sign-in
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleSignInClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
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
                            loginProgressBar.visibility = View.GONE

                            if (task.isSuccessful) {
                                Log.d(tag, "signIn With Auth Credential: succeeded")
                                sendUserToMainActivity()
                            } else {
                                Log.d(tag, "signIn With Auth Credential: failed, Error: ${task.exception?.message}")
                                "signIn With Google failed, due to ${task.exception?.message}".toast(this)
                            }
                        }
            } else {
                loginProgressBar.visibility = View.GONE
                "sign-in failed: ${result?.toString()}".toast(this)
            }
        }
    }

    private fun listeningUser(userId: String, loadingBar: ProgressBar) {
        AppUtil.showProgressBar(loadingBar)
        DbUtil.usersRef.document("$userId")
//          FirebaseFirestore.getInstance().collection("Users").document("$userId")
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    AppUtil.dismissProgressBar(loadingBar)
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

    private fun verifyEmailAddress() {
        // make sure that it's real time.
        val isEmailVerified = FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
        if (isEmailVerified) {
            sendUserToMainActivity()
        } else {
            "Please verify your email first...".toast(this)
            DbUtil.signOut()
        }
    }

    private fun sendUserToSetupActivity() {
        val intent = Intent(this@LoginActivity, SetupActivity::class.java)
        startActivity(intent)
        if (!this@LoginActivity.isFinishing) {
            finish()
        }
    }

    private fun sendUserToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        if (!this@LoginActivity.isFinishing) {
            finish()
        }
    }

    private fun sendUserToResetPasswordActivity() {
        val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
        startActivity(intent)

    }

    private fun sendUserToRegisterActivity() {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)

    }
}
