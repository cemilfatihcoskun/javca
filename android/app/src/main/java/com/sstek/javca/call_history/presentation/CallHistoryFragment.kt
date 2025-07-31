package com.sstek.javca.call_history.presentation

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sstek.javca.call.presentation.CallActivity
import com.sstek.javca.databinding.FragmentCallHistoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallHistoryFragment : Fragment() {

    private var _binding: FragmentCallHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CallHistoryViewModel by viewModels()
    private lateinit var adapter: CallHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CallHistoryAdapter(
            "",
            emptyList(),
            emptyMap(),
            onCallButtonClick = { calleeId ->
                val callerId = viewModel.currentUser.value?.uid ?: return@CallHistoryAdapter
                viewModel.startCall(callerId, calleeId) { callId ->
                    if (!isInternetAvailable(requireContext())) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "İnternet bağlantısı yok, arama yapılamaz", Toast.LENGTH_SHORT).show()
                        }
                        return@startCall
                    }

                    if (callId.isNullOrBlank()) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Arama başlatılamadı.", Toast.LENGTH_SHORT).show()
                        }
                        return@startCall
                    }

                    viewModel.checkServerConnectionOnce { isConnected ->
                        if (!isConnected) {
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "VCA sunucusuna bağlanılamadı, arama yapılamaz", Toast.LENGTH_SHORT).show()
                            }
                            return@checkServerConnectionOnce
                        }

                        val intent = Intent(requireContext(), CallActivity::class.java).apply {
                            putExtra("callId", callId)
                            putExtra("isCaller", true)
                        }

                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
        )
        binding.callHistoryRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.callHistoryRecycler.adapter = adapter

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            adapter.setCurrentUserId(user.uid)
        }

        viewModel.callHistory.observe(viewLifecycleOwner) {
            adapter.updateCalls(it)
        }

        viewModel.usersMap.observe(viewLifecycleOwner) {
            adapter.setUserMap(it)
        }

        viewModel.loadCurrentUser()
        viewModel.loadUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.currentUser.value?.let { user ->
            viewModel.startObserving(user.uid)
        }
    }

    // TODO(lowercase yapılmalı mı gerçekten, düşünmeli)
    fun search(query: String) {
        adapter.filter(query.lowercase().trim())
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

}
