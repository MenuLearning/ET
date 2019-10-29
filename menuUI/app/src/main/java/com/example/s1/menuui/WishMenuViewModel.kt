package com.example.s1.menuui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WishMenuViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Your Wish Menu List"
    }
    val text: LiveData<String> = _text
}