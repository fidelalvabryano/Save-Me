package com.harasio.savemeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.harasio.savemeapp.auth.SignInActivity

class StartActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

        Handler(mainLooper).postDelayed({
            if(user == null){
                val signInIntent = Intent(this, SignInActivity::class.java)
                startActivity(signInIntent)
                finish()
            }else{
                val dashboardIntent = Intent(this, BottomNavActivity::class.java)
                startActivity(dashboardIntent)
                finish()
            }
        }, 2000)
    }
}