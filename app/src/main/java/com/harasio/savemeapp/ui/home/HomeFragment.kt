package com.harasio.savemeapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.harasio.savemeapp.MapActivity
import com.harasio.savemeapp.R
import com.harasio.savemeapp.auth.SignInActivity
import com.harasio.savemeapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        if(currentUser.getIdToken(false).getResult()?.signInProvider == "google.com")
        {
            binding.tvFullnameHome.text = currentUser.displayName
        }
        else
        {
            val email = currentUser.email
            var indx= email.indexOf('@')
            val name =email.substring(0,indx)
            binding.tvFullnameHome.text = name
        }





        binding.btnMap.setOnClickListener{
            val intent = Intent(context, MapActivity::class.java)
            startActivity(intent)
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