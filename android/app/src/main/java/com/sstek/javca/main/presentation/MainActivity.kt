package com.sstek.javca.main.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private var adapter: UserAdapter? = null

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

        callListenerService = Intent(this, CallListenerService::class.java)
        startService(callListenerService)

        binding.userListRecyclerView.layoutManager = LinearLayoutManager(this)

        // currentUser gözlemle
        viewModel.currentUser.observe(this) { currentUser ->
            if (currentUser == null) {
                // Oturum kapalıysa giriş ekranına dön
                startActivity(Intent(this, LogInActivity::class.java))
                finish()
            } else {
                currentUserName = currentUser.username

                if (adapter == null) {
                    // Adapter yoksa oluştur
                    adapter = UserAdapter(
                        users = emptyList(),
                        currentUser = currentUser,
                        onItemClick = { /* İstersen bir şey yap */ },
                        onCallClick = { selectedUser ->
                            Snackbar.make(
                                binding.root,
                                "${selectedUser.username} aranıyor.",
                                Snackbar.LENGTH_SHORT
                            ).show()

                            viewModel.startCall(
                                currentUser.uid,
                                selectedUser.uid,
                                onCallStarted = { callId ->
                                    Log.d("MainActivity", "call started with id: $callId")
                                    val intent = Intent(this, CallActivity::class.java).apply {
                                        putExtra("callId", callId)
                                        putExtra("isCaller", true)
                                    }
                                    startActivity(intent)
                                }
                            )
                        },
                        onFavoriteToggle = { user ->
                            viewModel.toggleFavorite(user.uid)
                        }
                    )
                    binding.userListRecyclerView.adapter = adapter
                } else {
                    // Adapter varsa currentUser'ı güncelle ve listeyi yenile
                    adapter?.currentUser = currentUser
                    adapter?.notifyDataSetChanged()
                }
            }
        }

        // userList gözlemle ve güncelle
        viewModel.userList.observe(this) { users ->
            val filteredUsers = users.filter { it.uid != viewModel.currentUser.value?.uid }
            if (adapter == null) {
                // Eğer adapter henüz oluşturulmamışsa currentUser yüklendiğinde oluşturulacak, şimdilik sadece listeyi hazırla
                // (Genelde burada sorun olmaz, ama önlem olarak yazdım)
            } else {
                adapter?.updateUsers(filteredUsers)
            }
        }

        viewModel.currentUser.observe(this) { currentUser ->
            if (currentUser != null) {
                adapter?.updateCurrentUser(currentUser)
            }
        }
        

        viewModel.loadUsers()
    }

    // Menü ve diğer fonksiyonlar değişmeden kalabilir

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Kullanıcı ara..."

        searchView?.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { adapter?.filter(it) }
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter?.filter(newText ?: "")
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
        val builder = AlertDialog.Builder(this)
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
