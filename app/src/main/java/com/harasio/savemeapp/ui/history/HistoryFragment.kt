package com.harasio.savemeapp.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.harasio.savemeapp.R


class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters

    companion object {
        const val EXTRA_PING_LIST = "extra_ping_list"
    }

    private lateinit var bundle: Bundle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)



    }

}