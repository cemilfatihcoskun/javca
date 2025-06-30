package com.sstek.javca.presentation.call

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

import com.sstek.javca.databinding.ActivityLoginBinding
import androidx.lifecycle.Observer
import com.sstek.javca.databinding.ActivityCallBinding
import com.sstek.javca.presentation.call.CallViewModel
import com.sstek.javca.presentation.main.MainActivity
import com.sstek.javca.presentation.register.RegisterActivity

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {
    private val viewModel: CallViewModel by viewModels()
    private lateinit var binding: ActivityCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.setText("araba")
    }
}