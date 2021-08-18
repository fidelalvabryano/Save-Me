package com.harasio.savemeapp.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.google.firebase.auth.FirebaseAuth
import com.harasio.savemeapp.MyFirebaseMessagingService
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
    private var _binding: FragmentHomeBinding? = null
    private lateinit var myfms: MyFirebaseMessagingService
    private var LOCATION_PERMISSION_REQUEST = 1
    private var PERMISSION_ALL = 1
    var PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECORD_AUDIO
    )
    private val binding get() = _binding!!
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
        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL)
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

            val email = currentUser?.email
            val indx = email?.indexOf('@')
            val name = email?.substring(0, indx!!)
            binding.tvFullnameHome.text = name

        binding.btnPanic.setMainMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_24, R.drawable.ic_outline_cancel_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_alarm_24)
            .addSubMenu(Color.parseColor("#FF0000"), R.drawable.ic_baseline_panic_record_24)
        getPhoneNumber()

        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED) {


            binding.btnPanic.setOnMenuSelectedListener() {
                val name = binding.tvFullnameHome.text.toString()
                val uid = mAuth.currentUser?.uid
                val lat = 34.0897
                val long  = -118.2561
                val token = getDeviceRegistrationToken()
                var kejahatan = ""
                val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val message = "$name sedang dalam bahaya di http://maps.google.com/?q=$lat,$long pada $currentDateTime ."


                val year = normalize(2021,2010,2017)
                val month = normalize(6,1,12)
                val day = normalize(9,1,31)
                val dayOfWeek = normalize(2,0,6)
                val quarter = normalize(2,1,4)
                val time = normalize(1500,1,2359)
                val age = normalize(20,2,75)
                val gender = normalize(1,0,1)
                val descode = normalize(6,0,19)
                val PremiseCode = normalize(194,37,238)
                val addresCode = normalize(9624,0,23756)
                val LatN = normalizeD(lat,33.801,34.3272)
                val LongN = normalizeD(long,-118.6673,-118.1624)


                val byteBuffer : ByteBuffer = ByteBuffer.allocateDirect(13*4)
                byteBuffer.putFloat(year)
                byteBuffer.putFloat(month)
                byteBuffer.putFloat(day)
                byteBuffer.putFloat(dayOfWeek)
                byteBuffer.putFloat(quarter)
                byteBuffer.putFloat(time)
                byteBuffer.putFloat(age)
                byteBuffer.putFloat(gender)
                byteBuffer.putFloat(descode)
                byteBuffer.putFloat(PremiseCode)
                byteBuffer.putFloat(addresCode)
                byteBuffer.putFloat(LatN.toFloat())
                byteBuffer.putFloat(LongN.toFloat())



                val model = Model.newInstance(requireContext())


                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 13), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)


                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray


                for(i in 0..15)
                {
                    if(outputFeature0[i].toString() == "1.0" && i == 0)
                    {
                        val crime = "Battery with sexual contact"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }

                    if(outputFeature0[i].toString() == "1.0" && i == 1)
                    {
                        val crime = "BEASTIALITY, CRIME AGAINST NATURE SEXUAL ASSLT WITH ANIM"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 2)
                    {
                        val crime = "CHILD ABANDONMENT"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 3)
                    {
                        val crime = "CHILD ABUSE (PHYSICAL) - AGGRAVATED ASSAULT"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 4)
                    {
                        val crime = "CHILD ABUSE (PHYSICAL) - SIMPLE ASSAULT"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 5)
                    {
                        val crime = "HUMAN TRAFFICKING - COMMERCIAL SEX ACTS"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 6)
                    {
                        val crime = "INCEST (SEXUAL ACTS BETWEEN BLOOD RELATIVES)"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 7)
                    {
                        val crime = "Letters,Lewd-Telephone calls"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 8)
                    {
                        val crime = "Lewd Conduct"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 9)
                    {
                        val crime = "LEWD/LASCIVIOUS ACTS WITH CHILD"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 10)
                    {
                        val crime = "ORAL COPULATION"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 11)
                    {
                        val crime = "Rape, Attempted"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 12)
                    {
                        val crime = "Rape,Forcible"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 13)
                    {
                        val crime = "SEX,UNLAWFUL(INC MUTUAL CONSENT, PENETRATION W/ FRGN OBJ"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 14)
                    {
                        val crime = "SEXUAL PENETRATION W/FOREIGN OBJECT"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }
                    if(outputFeature0[i].toString() == "1.0" && i == 15)
                    {
                        val crime = "SODOMY/SEXUAL CONTACT B/W PENIS OF ONE PERS TO ANUS OTH"
                        kejahatan = crime
                        Toast.makeText(context, crime, Toast.LENGTH_SHORT).show()
                    }

                }

                model.close()

                val client = AsyncHttpClient()
                val url = "http://34.101.177.1:3000/api/ping/v1/ping"
                val params = RequestParams()
                params.put("_id", uid)
                params.put("kejahatan", kejahatan)
                params.put("long", long)
                params.put("lat", lat)
                params.put("deviceRegistrationToken", token)
                client.post(url, params ,object : AsyncHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                        //Toast.makeText(context, "MANTAP SUKSES PING!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                        //Toast.makeText(context, "GAGAL PING!!!", Toast.LENGTH_SHORT).show()
                    }
                })

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
                            }, 50000)
                        }
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
        val url = "http://34.101.177.1:3000/api/account/v1/fetch"
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

    fun normalize(value: Int, min: Int, max: Int): Float {
        var result = (value - min) / (max - min)

        if(result >= 1.0)
        {
            result = 0
            return result.toFloat()
        }
        else return result.toFloat()
    }

    fun normalizeD(value: Double, min: Double, max: Double): Double {
        var result = (value - min) / (max - min)
        return result
    }
}