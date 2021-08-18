package com.harasio.savemeapp.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.harasio.savemeapp.*
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var myfms: MyFirebaseMessagingService
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        supportActionBar?.hide()
        bundle = Bundle()
        myfms = MyFirebaseMessagingService()


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        btn_sign_in.setOnClickListener(){
            doLogin()
        }
        btn_forgotpassword.setOnClickListener{
            forgotpassword()
        }
        btn_registerpage.setOnClickListener{
            register()
            finish()
        }
    }

    private fun doLogin() {
        if (et_email_login.text.toString().isEmpty()){
            et_email_login.error = "Email Address must be filled"
            et_email_login.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(et_email_login.text.toString()).matches()){
            et_email_login.error = "Email Address is not valid"
            et_email_login.requestFocus()
            return
        }
        if (et_password_login.text.toString().isEmpty()){
            et_password_login.error = "Password must be filled"
            et_password_login.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(et_email_login.text.toString(), et_password_login.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    updateUI(null)
                }
            }
    }

    private fun register() {
        val registerIntent = Intent(this@SignInActivity, RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    private fun forgotpassword() {
        val forgotpasswordIntent = Intent(this@SignInActivity, ForgotPasswordActivity::class.java)
        startActivity(forgotpasswordIntent)
    }

    public override fun onStart() {
        super.onStart()
    }

    private fun updateUI(currentUser: FirebaseUser?){
        if (currentUser != null){
            if (currentUser.isEmailVerified){
                saveData()
                startActivity(Intent(this, BottomNavActivity::class.java))
                finish()
            } else {
                Toast.makeText(baseContext, "Please verify your email address",
                    Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(baseContext, "Invalid email address or password",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData() {
        /*ini juga harusnya uidnya pake hasil hash dari email address, kalo gak datanya gak akan kekirim*/
        val uid = auth.currentUser?.uid
        val token = getDeviceRegistrationToken()

        val client = AsyncHttpClient()
        val url = "http://34.101.177.1:3000/api/account/v1/updateRegistrationToken"
        val params = RequestParams()
        params.put("_id", uid)
        params.put("deviceRegistrationToken", token)
        client.post(url, params ,object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                //Toast.makeText(this@SignInActivity, "MANTAP UPDATE SUKSES!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                //Toast.makeText(this@SignInActivity, "UPDATE GAGAL!!!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getDeviceRegistrationToken() : String? {
        //Toast.makeText(this@SignInActivity,myfms.getToken(applicationContext),Toast.LENGTH_LONG).show()
        return myfms.getToken(applicationContext)
    }
}