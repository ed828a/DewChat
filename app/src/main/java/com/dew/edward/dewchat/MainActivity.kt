package com.dew.edward.dewchat

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.dew.edward.dewchat.ui.DetailsActivity
import com.dew.edward.dewchat.ui.LoginActivity
import com.dew.edward.dewchat.ui.SetupActivity
import com.dew.edward.dewchat.util.DbUtil
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val tag = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = DbUtil.mAuth.currentUser
        if (currentUser == null) {
            sendUserToLoginActivity()
        } else {
            checkUserExistence()
        }
    }

    private fun checkUserExistence() {
        val currentUserId = DbUtil.mAuth.currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("Users").document(currentUserId)
//              DbUtil.usersRef.document(currentUserId)
                    .addSnapshotListener { documentSnapshot,
                                           exception ->
                        if (exception != null){
                            Log.d(tag, "Exception: ${exception.message}")
                            return@addSnapshotListener
                        }

                        if (documentSnapshot == null || !documentSnapshot.exists()){
                            sendUserToSetupActivity()
                        }
                    }
        }
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                return true
            }

            R.id.user_profile -> {
                startActivity(Intent(this, SetupActivity::class.java))
                return true
            }

            R.id.account_details -> {
                startActivity(Intent(this@MainActivity, DetailsActivity::class.java))
                return true
            }

            R.id.settings_sign_out -> {
                DbUtil.signOut()
                sendUserToLoginActivity()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun sendUserToLoginActivity() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        // this line cause the leakage
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        if (!this@MainActivity.isFinishing) {
            finish()
        }
    }

    private fun sendUserToSetupActivity() {
        val intent = Intent(this@MainActivity, SetupActivity::class.java)
        startActivity(intent)
//        if (!this@MainActivity.isFinishing) {
//            finish()
//        }
    }

}
