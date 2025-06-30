package com.sstek.javca.presentation.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sstek.javca.databinding.ActivityRegisterBinding
import com.sstek.javca.presentation.login.LogInActivity
import com.sstek.javca.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            viewModel.register(username, email, password)
        }

        viewModel.state.observe(this) { state ->
            if (state.isSuccess) {
                Toast.makeText(this, "Kayıt başarılı.", Toast.LENGTH_SHORT).show()
                Log.d("RegisterActivity", "onCreate() register success.")

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else if (state.errorMessage != null) {
                Toast.makeText(this, "Kayıt başarısız.", Toast.LENGTH_SHORT).show()
                Log.e("RegisterActivity", "onCreate() register error ${state.errorMessage}.")
            }
        }

        binding.loginPageButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}