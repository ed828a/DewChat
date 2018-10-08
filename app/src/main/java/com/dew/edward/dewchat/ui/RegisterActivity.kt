package com.dew.edward.dewchat.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.app.DewChatApp
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.toast
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private val tag: String = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        registerCreateAccount.setOnClickListener {
            // create a new account
            val email = inputTextRegisterEmail.text.trim().toString()
            if (email.isEmpty()) {
                "please input your email...".toast(this)
                return@setOnClickListener
            }

            val password = inputTextRegisterPassword.text.trim().toString()
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

            registerProgressBar.visibility = View.VISIBLE

            DbUtil.mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        registerProgressBar.visibility = View.GONE

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
        startActivity(setupIntent)
        finish()
    }
    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }
}
