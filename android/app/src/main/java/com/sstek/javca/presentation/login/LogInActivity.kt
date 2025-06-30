package com.sstek.javca.presentation.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

import com.sstek.javca.databinding.ActivityLoginBinding
import androidx.lifecycle.Observer
import com.sstek.javca.presentation.main.MainActivity
import com.sstek.javca.presentation.register.RegisterActivity

@AndroidEntryPoint
class LogInActivity : AppCompatActivity() {
    private val viewModel: LogInViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(email, password)
        }

        viewModel.state.observe(this, Observer { state ->
            if (state.isSuccess) {
                binding.textViewTitle.setTextColor(Color.GREEN)
                binding.textViewTitle.setText("Giriş başarılı.")

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else if (state.errorMessage != null) {
                binding.textViewTitle.setTextColor(Color.RED)
                binding.textViewTitle.setText("Giriş hatalı ${state.errorMessage}.")
            }
        })

        binding.registerPageButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}