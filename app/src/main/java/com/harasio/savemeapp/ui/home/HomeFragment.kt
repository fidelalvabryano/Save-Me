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
import com.harasio.savemeapp.R
import com.harasio.savemeapp.auth.SignInActivity
import com.harasio.savemeapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private var _binding: FragmentHomeBinding? = null
    private val LOCATION_PERMISSION_REQUEST = 1
    lateinit var googleMap: GoogleMap
    private lateinit var currlocation : Location
    //private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
                when(it) {
                    0 -> Toast.makeText(context, "0 selected", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(context, "1 selected", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(context, "2 selected", Toast.LENGTH_SHORT).show()
                    3 -> Toast.makeText(context, "3 selected", Toast.LENGTH_SHORT).show()

                }
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
}