package com.sstek.javca.presentation.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.LinearInterpolator
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sstek.javca.presentation.service.CallListenerService
import com.sstek.javca.databinding.ActivityMainBinding
import com.sstek.javca.presentation.login.LogInActivity
import dagger.hilt.android.AndroidEntryPoint

import com.sstek.javca.R
import com.sstek.javca.domain.model.User
import com.sstek.javca.presentation.call.CallActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val REQUEST_CODE = 6666
        if (!Settings.canDrawOverlays(applicationContext)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_CODE)
        }

        // TODO(Authttan silinse bile hala daha telefonda devam edebiliyor. Bu problemi çöz.)
        //viewModel.reloadAuth()
        //viewModel.checkUser()

        //TODO(Register sonrası logout oluyor tekrar login yapmak gerekiyor)

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, CallListenerService::class.java))
        } else {
            startService(Intent(this, CallListenerService::class.java))
        }
         */
        startService(Intent(this, CallListenerService::class.java))

        binding.targetUsernameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    adapter.filter(s.toString())
                }
            }
        })

        binding.userListRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(
            ArrayList<User>(),
            onCallClick = { selectedUser ->
                Snackbar.make(binding.root, "${selectedUser.username} aranıyor.", Snackbar.LENGTH_SHORT).show()
                viewModel.startCall(viewModel.getCurrentUser()?.uid.toString(), selectedUser.uid.toString(), { callId ->
                    Log.d("MainActivity", "contact called onCallClick")
                    val intent = Intent(this, CallActivity::class.java).apply {
                        putExtra("callId", callId)
                        putExtra("isCaller", true)
                    }
                    startActivity(intent)
                })
            },
            onItemClick = { selectedUser ->
                Snackbar.make(binding.root, "Seviyorsan tıkla ve konuş bence.", Snackbar.LENGTH_SHORT).show()
            }
        )
        binding.userListRecyclerView.adapter = adapter
        viewModel.loadUsers()
        viewModel.userList.observe(this) { users ->
            val currentUserId = viewModel.getCurrentUser()?.uid
            val filteredUsers = users.filter { it.uid != currentUserId }
            adapter.updateUsers(filteredUsers)
        }

        viewModel.username.observe(this) { name ->
            Log.d("MainActivity", "name=$name")
            if (name == null || name == "null") {
                startActivity(Intent(this, LogInActivity::class.java))
                finish()
            } else {
                val welcomeMessage = getString(R.string.welcomeTextView)
                binding.welcomeTextView.text = "$welcomeMessage, $name!"
                binding.welcomeTextView.animate()
                    .rotationBy(360f)
                    .setDuration(1000)
                    .setInterpolator(LinearInterpolator())
                    .start()

                binding.logOutButton.setOnClickListener {
                    viewModel.logOut()
                    val intent = Intent(this, LogInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }

        viewModel.checkUser()

        viewModel.callState.observe(this) { state ->
            when (state) {
                is CallUiState.Loading -> Snackbar.make(binding.root, "Loading", Snackbar.LENGTH_SHORT).show()
                is CallUiState.Success -> Snackbar.make(binding.root, "Success", Snackbar.LENGTH_SHORT).show()
                is CallUiState.Error -> Snackbar.make(binding.root, "Error", Snackbar.LENGTH_SHORT).show()
                else -> Unit
            }
        }


    }



}

