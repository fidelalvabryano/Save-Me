package com.harasio.savemeapp.ui.home

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.harasio.savemeapp.MyFirebaseMessagingService
import com.harasio.savemeapp.R
import com.harasio.savemeapp.auth.SignInActivity
import com.harasio.savemeapp.databinding.FragmentHomeBinding
import com.harasio.savemeapp.md5
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header

class HomeFragment : Fragment() {

    companion object {
        const val EXTRA_UID = "extra_uid"
        const val EXTRA_DEVICE_TOKEN = "extra_device_token"
    }

    private lateinit var mAuth: FirebaseAuth
    private var _binding: FragmentHomeBinding? = null
    private lateinit var myfms: MyFirebaseMessagingService
    private val LOCATION_PERMISSION_REQUEST = 1
    lateinit var googleMap: GoogleMap
    private lateinit var currlocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        myfms = MyFirebaseMessagingService()

        if(currentUser?.getIdToken(false)?.getResult()?.signInProvider == "google.com")
        {
            binding.tvFullnameHome.text = currentUser.displayName
        }
        else
        {
            val email = currentUser?.email
            var indx = email?.indexOf('@')
            val name = email?.substring(0, indx!!)
            binding.tvFullnameHome.text = name
        }

        binding.btnPanic.setMainMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_24, R.drawable.ic_outline_cancel_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_24)
            .setOnMenuSelectedListener(){
                /*ini kalo data uid & token yg kita kirim gak sama kaya yg ada di firestore,
                data lat longnya gak akan kekirim*/
                val uid = mAuth.currentUser?.uid
                var long : Double = 0.0
                var lat : Double = 0.0
                var token = getDeviceRegistrationToken()
                var kejahatan = ""

                when(it) {
                    0 -> {
                        kejahatan = "Kejahatan 1"
                    }
                    1 -> {
                        kejahatan = "Kejahatan 2"
                    }
                    2 -> {
                        kejahatan = "Kejahatan 3"
                    }
                    3 -> {
                        kejahatan = "Kejahatan 4"
                    }

                }
                val client = AsyncHttpClient()
                val url = "http://159.65.4.250:3000/api/ping/v1/ping"
                val params = RequestParams()
                params.put("_id", uid)
                params.put("kejahatan", kejahatan)
                params.put("long", long)
                params.put("lat", lat)
                params.put("deviceRegistrationToken", token)
                client.post(url, params ,object : AsyncHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        Toast.makeText(context, "MANTAP SUKSES PING!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                        Toast.makeText(context, "GAGAL PING!!!", Toast.LENGTH_SHORT).show()
                    }
                })
                //untuk cek isi data yg dikirim ke server apa aja
                Toast.makeText(context, uid, Toast.LENGTH_SHORT).show()
                Toast.makeText(context, kejahatan, Toast.LENGTH_SHORT).show()
                Toast.makeText(context, long.toString(), Toast.LENGTH_SHORT).show()
                Toast.makeText(context, lat.toString(), Toast.LENGTH_SHORT).show()
                Toast.makeText(context, token, Toast.LENGTH_SHORT).show()


            }

        binding.signOutBtn.setOnClickListener{
            mAuth.signOut()
            val intent = Intent(context,SignInActivity::class.java)
            startActivity(intent)
            (activity as AppCompatActivity).finish()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDeviceRegistrationToken() : String? {
        return myfms.getToken(context)
    }
}