package com.sstek.javca.main.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sstek.javca.framework.CallListenerService
import com.sstek.javca.databinding.ActivityMainBinding
import com.sstek.javca.auth.presentation.login.LogInActivity
import dagger.hilt.android.AndroidEntryPoint

import com.sstek.javca.R
import com.sstek.javca.user.domain.entity.User
import com.sstek.javca.call.presentation.CallActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    private lateinit var callListenerService: Intent

    private var currentUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.requestFocus()

        val REQUEST_CODE = 6666
        if (!Settings.canDrawOverlays(applicationContext)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_CODE)
        }

        // TODO(Authttan silinse bile hala daha telefonda devam edebiliyor. Bu problemi çöz.)
        //viewModel.reloadAuth()
        //viewModel.checkUser()

        //DONE TODO(Register sonrası logout oluyor tekrar login yapmak gerekiyor)
        callListenerService = Intent(this, CallListenerService::class.java)

        // TODO(bu arızayı hallet bir nedenden dolayı startForegroundService çalışmıyor)
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(callListenerService)
        } else {
            startService(callListenerService)
        }
        */

        startService(callListenerService)

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
                //Snackbar.make(binding.root, "Seviyorsan tıkla ve konuş bence.", Snackbar.LENGTH_SHORT).show()
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
                currentUserName = name
            }
        }

        viewModel.checkUser()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView

        searchView?.queryHint = "Kullanıcı ara..."

        searchView?.apply {
            queryHint = "Kullanıcı ara..."

            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { adapter.filter(it) }
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter(newText ?: "")
                    return true
                }
            })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfileDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProfileDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("JAVCA")
        builder.setMessage("Hoşgeldiniz $currentUserName. Oturumunuzu kapatmak mı istiyorsunuz?")
        builder.setPositiveButton("Evet") { _, _ ->
            viewModel.logOut()
            val intent = Intent(this, LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("Hayır", null)
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(callListenerService)
    }
}

