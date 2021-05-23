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
import com.google.firebase.messaging.FirebaseMessaging
import com.harasio.savemeapp.R
import com.harasio.savemeapp.User
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.lang.Exception
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }

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
        if(isDataValid()) {
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
    }

    private fun isDataValid() : Boolean {
        if (et_name.text.toString().isEmpty()){
            et_name.error = "Name must be filled!"
            et_name.requestFocus()
            return false
        }
        else if (et_email.text.toString().isEmpty()){
            et_email.error = "Email Address must be filled"
            et_email.requestFocus()
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString()).matches()){
            et_email.error = "Email Address is not valid"
            et_email.requestFocus()
            return false
        }
        else if (et_password.text.toString().isEmpty()){
            et_password.error = "Password must be filled"
            et_password.requestFocus()
            return false
        }
        else if (et_password.text.toString().length < 6){
            et_password.error = "Password must be at least 6 characters!"
            et_password.requestFocus()
            return false
        }
        else if (et_confirmPassword.text.toString() != et_password.text.toString()) {
            et_confirmPassword.error = "Password confirmation doesn't match!"
            et_confirmPassword.requestFocus()
            return false
        }
        else if (et_umur.text.toString().isEmpty()) {
            et_umur.error = "Umur must be filled"
            et_umur.requestFocus()
            return false
        }
        else if (et_gender.text.toString().isEmpty()) {
            et_gender.error = "Gender must be filled"
            et_gender.requestFocus()
            return false
        }
        else if (et_alamat.text.toString().isEmpty()) {
            et_alamat.error = "Alamat must be filled"
            et_alamat.requestFocus()
            return false
        }
        else if (et_kota.text.toString().isEmpty()) {
            et_kota.error = "Kota must be filled"
            et_kota.requestFocus()
            return false
        }
        else if (et_provinsi.text.toString().isEmpty()) {
            et_provinsi.error = "Provinsi must be filled"
            et_provinsi.requestFocus()
            return false
        }
        else if (et_zipcode.text.toString().isEmpty()) {
            et_zipcode.error = "Zip Code must be filled"
            et_zipcode.requestFocus()
            return false
        } else {
            return true
        }
    }

    private fun saveData() {
        val name = et_name.text.toString()
        val email = et_email.text.toString()
        val password = et_password.text.toString()
        val umur = et_umur.text.toString()
        val gender = et_gender.text.toString()
        val alamat = et_alamat.text.toString()
        val kota = et_kota.text.toString()
        val provinsi = et_provinsi.text.toString()
        val zipcode = et_zipcode.text.toString()
        val token = getDeviceRegistrationToken()

        val client = AsyncHttpClient()
        val url = "http://159.65.4.250:3000/api/account/v1/register"
        val params = RequestParams()
        params.put("fullname", name)
        params.put("email", email)
        params.put("password", password)
        params.put("umur", umur)
        params.put("gender", gender)
        params.put("alamat", alamat)
        params.put("kota", kota)
        params.put("provinsi", provinsi)
        params.put("zipcode", zipcode)
        params.put("deviceRegistrationToken", token)
        client.post(url, params ,object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                Toast.makeText(this@RegisterActivity, "MANTAP SUKSES!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                Toast.makeText(this@RegisterActivity, "GAGAL!!!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getDeviceRegistrationToken() : String {
        var token = "-TOKEN-TOKEN-TOKEN-TOKEN-TOKEN-TOKEN-TOKEN-TOKEN"
        if (token.isEmpty()) {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        token = task.result
                    } else if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@addOnCompleteListener
                    }
                }
        }
        return token
    }
}