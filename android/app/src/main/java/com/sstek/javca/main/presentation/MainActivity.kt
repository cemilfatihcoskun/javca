package com.sstek.javca.main.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sstek.javca.call_history.presentation.CallHistoryFragment
import com.sstek.javca.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import com.sstek.javca.R
import com.sstek.javca.auth.presentation.login.LogInActivity
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragmentTag = "MainFragment"

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, PeopleFragment(), "PeopleFragment")
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_users -> {
                    switchToFragment(PeopleFragment(), "PeopleFragment")
                    true
                }
                R.id.nav_history -> {
                    switchToFragment(CallHistoryFragment(), "CallHistoryFragment")
                    true
                }
                else -> false
            }
        }

        viewModel.loadCurrentUser()
    }

    private fun switchToFragment(fragment: Fragment, tag: String) {
        if (tag == currentFragmentTag) return

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment, tag)
            .commit()

        currentFragmentTag = tag

        //invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        for (i in 0 until (menu?.size() ?: 0)) {
            menu?.getItem(i)?.icon?.mutate()?.setTint(ContextCompat.getColor(this, R.color.white))
        }

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                if (fragment is PeopleFragment) {
                    fragment.search(newText ?: "")
                }
                else if (fragment is CallHistoryFragment) {
                    fragment.search(newText ?: "")
                }
                return true
            }
        })

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
        val currentUser = viewModel.currentUser.value
        if (currentUser == null) {
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("JAVCA")
        builder.setMessage("Hoşgeldiniz ${currentUser.username}. Oturumunuzu kapatmak mı istiyorsunuz?")
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


}
