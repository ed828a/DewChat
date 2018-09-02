package com.dew.edward.dewchat.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.util.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        resetSubmitButton.setOnClickListener {
            val email = resetEmailAddress.text.toString().trim()
            if (email.isEmpty()){
                "please provide your account email...".toast(this)
            } else {
                FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                "please check your email...".toast(this)
                                sendUserToLoginActivity()
                            } else {
                                "Error: ${task.exception?.message}".toast(this)
                            }
                        }
            }
        }
    }

    private fun sendUserToLoginActivity() {
        val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
