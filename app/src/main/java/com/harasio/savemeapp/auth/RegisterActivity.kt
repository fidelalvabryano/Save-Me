package com.harasio.savemeapp.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.harasio.savemeapp.R
import com.harasio.savemeapp.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        btn_register.setOnClickListener{
            signUpUser()
        }
    }

    private fun signUpUser(){
        if (et_email.text.toString().isEmpty()){
            et_email.error = "Email Address must be filled"
            et_email.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString()).matches()){
            et_email.error = "Email Address is not valid"
            et_email.requestFocus()
            return
        }
        if (et_password.text.toString().isEmpty()){
            et_password.error = "Password must be filled"
            et_password.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        user!!.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        startActivity(Intent(this, SignInActivity::class.java))
                                        finish()
                                    }
                                }

                    } else {
                        Toast.makeText(baseContext, "Register failed", Toast.LENGTH_SHORT).show()
                    }
                }
    }





}