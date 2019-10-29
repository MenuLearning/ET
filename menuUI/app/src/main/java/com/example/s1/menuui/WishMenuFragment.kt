package com.example.s1.menuui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class WishMenuFragment : Fragment() {

    private lateinit var wishMenuViewModel: WishMenuViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        wishMenuViewModel =
            ViewModelProviders.of(this).get(WishMenuViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_wish_menu, container, false)
        val textView: TextView = root.findViewById(R.id.text_wish_menu)
        wishMenuViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}