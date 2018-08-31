package com.dew.edward.dewchat.ui

import android.app.ProgressDialog
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.di.DewChatApp
import com.dew.edward.dewchat.viewmodel.DewViewModelFactory
import com.dew.edward.dewchat.viewmodel.LoginViewModel
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: DewViewModelFactory
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel::class.java)
    }

    private lateinit var loadingBar: ProgressDialog
    private lateinit var googleSignInClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        DewChatApp.appComponent.inject(this)

        loadingBar = ProgressDialog(this)

        registerAccountLink.setOnClickListener {
            loginViewModel.signOut()
            sendUserToRegisterActivity()
        }

        loginButton.setOnClickListener {

        }
    }

    private fun sendUserToRegisterActivity() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
