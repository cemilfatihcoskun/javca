package com.sstek.javca.main.presentation

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sstek.javca.call.presentation.CallActivity
import com.sstek.javca.databinding.FragmentPeopleBinding
import com.sstek.javca.framework.CallListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class PeopleFragment : Fragment() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PeopleViewModel by viewModels()

    private var adapter: PeopleAdapter? = null

    private var currentUserName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().startService(Intent(requireContext(), CallListenerService::class.java))

        binding.userListRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            currentUser?.let {
                currentUserName = it.username
                if (adapter == null) {
                    adapter = PeopleAdapter(
                        users = emptyList(),
                        currentUser = it,
                        onItemClick = {},
                        onCallClick = { selectedUser
                            // TODO(Mesajlar diğer fragment a geçince geliyor)
                            if (!isInternetAvailable(requireContext())) {
                                activity?.runOnUiThread {
                                    Toast.makeText(requireContext(), "İnternet bağlantısı yok, arama yapılamaz", Toast.LENGTH_SHORT).show()
                                }
                                return@PeopleAdapter
                            }

                            /* TODO(Belki sonra)
                            if (selectedUser.status == "offline") {
                                Toast.makeText(context, "${selectedUser.username} çevrimdışı", Toast.LENGTH_SHORT).show()
                                return@UserAdapter
                            }
                             */

                            viewModel.checkServerConnectionOnce { isConnected ->
                                if (!isConnected) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(requireContext(), "VCA sunucusuna bağlanılamadı, arama yapılamaz", Toast.LENGTH_SHORT).show()
                                    }
                                    return@checkServerConnectionOnce
                                }

                                viewModel.startCall(
                                    currentUser.uid,
                                    selectedUser.uid,
                                    onCallStarted = { callId ->
                                        if (callId.isNullOrBlank()) {
                                            activity?.runOnUiThread {
                                                Toast.makeText(requireContext(), "Arama başlatılamadı.", Toast.LENGTH_SHORT).show()
                                            }
                                            return@startCall
                                        }

                                        val intent = Intent(requireContext(), CallActivity::class.java).apply {
                                            putExtra("callId", callId)
                                            putExtra("isCaller", true)
                                        }
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                )
                            }
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(it.uid) }
                    )
                    binding.userListRecyclerView.adapter = adapter
                } else {
                    adapter?.updateCurrentUser(it)
                }
            }
        }

        viewModel.userList.observe(viewLifecycleOwner) { users ->
            val currentUserId = viewModel.currentUser.value?.uid
            if (currentUserId != null) {
                val filteredUsers = users.filter { it.uid != currentUserId }
                adapter?.updateUsers(filteredUsers)
            }
        }


        viewModel.loadUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    // TODO(lowercase yapılmalı mı gerçekten, düşünmeli)
    fun search(query: String) {
        adapter?.filter(query.lowercase().trim())
    }
}
