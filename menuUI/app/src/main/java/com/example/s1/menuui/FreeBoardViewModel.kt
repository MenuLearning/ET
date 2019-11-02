package com.example.s1.menuui.ui.freeboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FreeBoardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to Free Board"
    }
    val text: LiveData<String> = _text
}