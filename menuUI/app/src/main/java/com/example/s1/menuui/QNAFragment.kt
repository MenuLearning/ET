package com.example.s1.menuui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class QNAFragment : Fragment() {

    private lateinit var qnaViewModel: QNAViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        qnaViewModel =
            ViewModelProviders.of(this).get(QNAViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_qna, container, false)
        val textView: TextView = root.findViewById(R.id.text_search)
        qnaViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}