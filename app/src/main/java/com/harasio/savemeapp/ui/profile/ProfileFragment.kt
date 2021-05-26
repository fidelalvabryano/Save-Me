package com.harasio.savemeapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.harasio.savemeapp.databinding.FragmentProfileBinding
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject

class ProfileFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        getDetailUser()
    }

    private fun getDetailUser() {
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

                    val detFullname = dataObject.getString("fullname")
                    val detEmail = dataObject.getString("email")
                    val detUmur = dataObject.getString("umur")
                    val detGender = dataObject.getString("gender")
                    val detAlamat = dataObject.getString("alamat")
                    val detKota = dataObject.getString("kota")
                    val detProvinsi = dataObject.getString("provinsi")
                    val detZipcode = dataObject.getString("zipcode")

                    _binding?.txtDetFullname?.text = detFullname
                    _binding?.txtDetEmail?.text = detEmail
                    _binding?.txtDetUmur?.hint = detUmur
                    _binding?.txtDetGender?.text = detGender
                    _binding?.txtDetAlamat?.hint = detAlamat
                    _binding?.txtDetKota?.hint = detKota
                    _binding?.txtDetProvinsi?.hint = detProvinsi
                    _binding?.txtDetZipCode?.hint = detZipcode

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
}