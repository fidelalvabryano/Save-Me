package com.harasio.savemeapp.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.media.MediaRecorder
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.harasio.savemeapp.MyFirebaseMessagingService
import com.harasio.savemeapp.PingData
import com.harasio.savemeapp.R
import com.harasio.savemeapp.auth.SignInActivity
import com.harasio.savemeapp.databinding.FragmentHomeBinding
import com.harasio.savemeapp.ml.Model
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var latLng : LatLng
    private var _binding: FragmentHomeBinding? = null
    private lateinit var myfms: MyFirebaseMessagingService
    private val LOCATION_PERMISSION_REQUEST = 1
    private val SMS_PERMISSION_REQUEST = 1
    private val RECORD_PERMISSION_REQUEST = 1
    var PERMISSION_ALL = 1
    var PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECORD_AUDIO
    )
    private lateinit var currlocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val binding get() = _binding!!
    private var pingList: ArrayList<PingData> = ArrayList()
    private lateinit var bundle: Bundle
    private lateinit var smsManager: SmsManager
    private lateinit var phone: String
    private lateinit var mediaRecorder: MediaRecorder
    private var fileName = "recorded.3gp"
    private lateinit var sp: SoundPool
    private var soundId: Int = 0
    private var spLoaded = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        currlocation = Location("dummyprovider")
        latLng = LatLng(0.0, 0.0)
        fusedLocationProviderClient = activity?.let {
            LocationServices.getFusedLocationProviderClient(it)
        }!!
        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL);
        return root
    }



    private fun checkRecordPermission(permission: String) : Boolean {
        val check = context?.let { ContextCompat.checkSelfPermission(it, permission) }
        return (check == PackageManager.PERMISSION_GRANTED)
    }

    private fun checkSMSPermission(permission: String) : Boolean {
        val check = context?.let { ContextCompat.checkSelfPermission(it, permission) }
        return (check == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } != PackageManager.PERMISSION_GRANTED && context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) } != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            } else {
                Toast.makeText(context, "User has not granted location access permission", Toast.LENGTH_LONG).show()
                activity?.finish()
            }
        }

    }

    private fun sendSMS(phone: String, message: String) {
        smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phone, null, message, null, null)
        Toast.makeText(context, "SMS SENT", Toast.LENGTH_SHORT).show()
    }

    private fun record() {
        fileName = context?.getExternalFilesDir(null)?.absolutePath + "/recording.mp3"
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)

        mediaRecorder.setOutputFile(fileName)
        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecord() {
        mediaRecorder.stop()
        mediaRecorder.release()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        myfms = MyFirebaseMessagingService()
        bundle = Bundle()

        sp = SoundPool.Builder()
            .setMaxStreams(10)
            .build()

        sp.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (status == 0) {
                spLoaded = true
            } else {
                Toast.makeText(context, "Gagal load", Toast.LENGTH_SHORT).show()
            }
        }

        soundId = sp.load(context, R.raw.alarm, 1)



        if(currentUser?.getIdToken(false)?.result?.signInProvider == "google.com")
        {
            binding.tvFullnameHome.text = currentUser.displayName
        }
        else
        {
            val email = currentUser?.email
            val indx = email?.indexOf('@')
            val name = email?.substring(0, indx!!)
            binding.tvFullnameHome.text = name
        }
        binding.btnPanic.setMainMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_24, R.drawable.ic_outline_cancel_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_alarm_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_record_24)
        getPhoneNumber()

        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location :Location ->
                    if(location != null)
                    {
                        currlocation = location
                        latLng  = LatLng(currlocation.latitude,currlocation.longitude)

                        binding.btnPanic.setOnMenuSelectedListener() {
                            val name = binding.tvFullnameHome.text.toString()
                            val uid = mAuth.currentUser?.uid
                            val long =latLng.longitude
                            val lat  = latLng.latitude
                            val token = getDeviceRegistrationToken()
                            var kejahatan = ""
                            val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            val message = "$name sedang dalam bahaya di http://maps.google.com/?q=$lat,$long pada $currentDateTime ."


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
                            pingList.add(
                                PingData(kejahatan, lat.toString(), long.toString(), currentDateTime)
                            )

                            //untuk cek isi data yg dikirim ke server apa aja
                            Toast.makeText(context, uid, Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, kejahatan, Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, long.toString(), Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, lat.toString(), Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, token, Toast.LENGTH_SHORT).show()
                            if (checkSMSPermission(Manifest.permission.SEND_SMS)) {
                                sendSMS(phone, message)
                            }
                            when(it) {
                                0 -> {
                                    if (spLoaded) {
                                        sp.play(soundId, 1f, 1f, 0, 5, 1f)
                                    }
                                }
                                1 -> {
                                    if (checkRecordPermission(Manifest.permission.RECORD_AUDIO)) {
                                        record()
                                        Handler().postDelayed({
                                            stopRecord()
                                        }, 5000)
                                    }
                                }
                            }



                            var byteBuffer : ByteBuffer = ByteBuffer.allocateDirect(7*4)
                            byteBuffer.putInt(2)
                            byteBuffer.putInt(1)
                            byteBuffer.putInt(5)
                            byteBuffer.putInt(3)
                            byteBuffer.putInt(4)
                            byteBuffer.putFloat((7).toFloat())
                            byteBuffer.putFloat((6).toFloat())

                            val model = Model.newInstance(requireContext())

                            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 7), DataType.FLOAT32)
                            inputFeature0.loadBuffer(byteBuffer)


                            val outputs = model.process(inputFeature0)
                            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
                            for(i in 0..15)
                            {
                                Toast.makeText(context, outputFeature0[i].toString(), Toast.LENGTH_SHORT).show()
                            }

                            model.close()


                        }
                    }

                }
        }
        else
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST) }



        binding.signOutBtn.setOnClickListener{
            mAuth.signOut()
            val intent = Intent(context,SignInActivity::class.java)
            startActivity(intent)
            (activity as AppCompatActivity).finish()
        }
    }

    private fun getPhoneNumber() {
        val client = AsyncHttpClient()
        val url = "http://159.65.4.250:3000/api/account/v1/fetch"
        val params = RequestParams()
        params.put("_id", mAuth.currentUser?.uid)
        client.post(url, params ,object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray) {
                val result = String(responseBody)
                try {
                    val responseObject = JSONObject(result)
                    val dataObject = responseObject.getJSONObject("data")

                    val detZipcode = dataObject.getString("zipcode")

                    phone = detZipcode

                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                Toast.makeText(context, "GAGAL Fetch!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDeviceRegistrationToken() : String? {
        return context?.let { myfms.getToken(it) }
    }
}