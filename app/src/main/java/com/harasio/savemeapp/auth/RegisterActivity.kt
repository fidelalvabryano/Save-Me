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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.harasio.savemeapp.R
import com.harasio.savemeapp.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userData: User
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance("https://b21-cap0083-default-rtdb.asia-southeast1.firebasedatabase.app/")
        myRef = database.getReference("users")


        btn_register.setOnClickListener{
            signUpUser()
        }
    }

    private fun signUpUser(){
        if (et_name.text.toString().isEmpty()){
            et_name.error = "Name must be filled!"
            et_name.requestFocus()
            return
        }
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
        if (et_password.text.toString().length < 6){
            et_password.error = "Password must be at least 6 characters!"
            et_password.requestFocus()
            return
        }
        if (et_confirmPassword.text.toString() != et_password.text.toString()) {
            et_confirmPassword.error = "Password confirmation doesn't match!"
            et_confirmPassword.requestFocus()
            return
        }


        auth.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        user!!.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        saveData()
                                    }
                                }

                    } else {
                        Toast.makeText(baseContext, "Register failed", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun saveData() {
        val name = et_name.text.toString()
        val email = et_email.text.toString()
        val password = et_password.text.toString()

        val user = User(name, email, password)
        val uid = auth.currentUser?.uid
        if (uid != null) {
            myRef.child(uid).setValue(user).addOnCompleteListener{
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignInActivity::class.java))
            }.addOnFailureListener{
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
            }
        }
        }
}