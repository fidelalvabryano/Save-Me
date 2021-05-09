package com.harasio.savemeapp.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.harasio.savemeapp.R
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_register.*

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        auth = FirebaseAuth.getInstance()

        btn_resetpassword.setOnClickListener{
            resetPassword()
        }
    }

    private fun resetPassword(){
        if (et_email_forgotpassword.text.toString().isEmpty()){
            et_email_forgotpassword.error = "Email Address must be filled"
            et_email_forgotpassword.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(et_email_forgotpassword.text.toString()).matches()){
            et_email_forgotpassword.error = "Email Address is not valid"
            et_email_forgotpassword.requestFocus()
            return
        }
        auth.sendPasswordResetEmail(et_email_forgotpassword.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Password reset sent to your email address", Toast.LENGTH_SHORT).show()
                    }
                }
    }
}