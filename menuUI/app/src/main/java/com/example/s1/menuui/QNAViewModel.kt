package com.example.s1.menuui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QNAViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to Q&A Board!"
    }
    val text: LiveData<String> = _text
}