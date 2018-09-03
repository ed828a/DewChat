package com.dew.edward.dewchat.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.di.DewChatApp
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.AppUtil.Companion.showProgressDialog
import com.dew.edward.dewchat.util.toast
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private val tag: String = this.javaClass.simpleName


    private lateinit var loadingBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        loadingBar = ProgressDialog(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        registerCreateAccount.setOnClickListener {
            // create a new account
            val email = registerLoginEmail.text.trim().toString()
            if (email.isEmpty()) {
                "please input your email...".toast(this)
                return@setOnClickListener
            }

            val password = registerLoginPassword.text.trim().toString()
            if (password.isEmpty()) {
                "please input your password".toast(this)
                return@setOnClickListener
            }
            val confirmPassword = registerConfirmPassword.text.trim().toString()
            if (confirmPassword.isEmpty()) {
                "please confirm your password...".toast(this)
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                "Please make sure that Confirm Password is same as Password...".toast(this)
                return@setOnClickListener
            }

            showProgressDialog(loadingBar, "Create new account",
                    "Creating your new account...")

            DbUtil.mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        loadingBar.dismiss()

                        if (task.isSuccessful) {
                            if (DewChatApp.isNeedEmailVerification) {
                                sendEmailVerificationMessage()  // with email verification
                            } else {
                                // without email verification
                                sendUserToSetupActivity()  // without email verification
                            }
                        } else {
                            "Error: ${task.exception?.message}".toast(this)
                        }
                    }
        }
    }

    private fun sendEmailVerificationMessage() {
        if (DbUtil.mAuth.currentUser != null) {
            DbUtil.mAuth.currentUser!!.sendEmailVerification()
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful){
                            "Confirmation Email was sent to you, please verify your account...".toast(this)
                            sendUserToLoginActivity()
                        } else {
                            "Error: ${task.exception?.message}".toast(this)
                        }
                        DbUtil.signOut()
                    }
        }

    }

    private fun sendUserToSetupActivity() {
        val setupIntent = Intent(this@RegisterActivity, SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()
    }
    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(loginIntent)
        finish()
    }
}
