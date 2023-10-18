package com.example.c001apk.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.c001apk.databinding.ActivityCopyBinding

class CopyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCopyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCopyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = intent.getStringExtra("text")
        binding.textView.text = text

    }


}