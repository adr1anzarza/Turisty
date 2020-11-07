package com.adrian.zarza.turisty

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.adrian.zarza.turisty.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    val RC_SIGN_IN = 1
    var mFirebaseAuth: FirebaseAuth? = null
    var mFirebaseAuthListener: AuthStateListener? = null
    var mUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.myNavHostFragment)

        NavigationUI.setupActionBarWithNavController(this,navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        mFirebaseAuth = FirebaseAuth.getInstance()

        mFirebaseAuthListener = AuthStateListener { firebaseAuth: FirebaseAuth? ->
            val user = mFirebaseAuth!!.currentUser
            if (user != null) {
                //user is signed in
                onSignedInInitialize(user.displayName!!)
                Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
            } else {
                //user is signed out
                createSignInIntent()
            }
        }
    }

    fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = Arrays.asList(
                EmailBuilder().build(),
                GoogleBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN)
        // [END auth_fui_create_intent]
    }

    private fun onSignedInInitialize(username: String) {
        mUsername = username
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth!!.addAuthStateListener(mFirebaseAuthListener!!)
    }

   override fun onPause() {
        super.onPause()
        if (mFirebaseAuthListener != null) {
            mFirebaseAuth!!.removeAuthStateListener(mFirebaseAuthListener!!)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }
}