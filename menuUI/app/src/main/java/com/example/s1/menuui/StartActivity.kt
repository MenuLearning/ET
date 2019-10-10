package com.example.s1.menuui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        try{
            Thread.sleep(3000);
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }catch(e: Exception){
            return
        }

    }
}
