package com.sstek.javca.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sstek.javca.databinding.ItemUserBinding
import com.sstek.javca.domain.model.User

class UserAdapter(
    private var users: List<User>,
    private val onItemClick: (User) -> Unit,
    private val onCallClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val filteredUsers = mutableListOf<User>()

    init {
        filteredUsers.addAll(users)
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.usernameTextView.text = user.username
            binding.root.setOnClickListener { onItemClick(user) }
            binding.callButton.setOnClickListener { onCallClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int = filteredUsers.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(filteredUsers[position])
    }

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        filteredUsers.clear()
        filteredUsers.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val lowerQuery = query.lowercase()
        filteredUsers.clear()
        if (lowerQuery.isEmpty()) {
            filteredUsers.addAll(users)
        } else {
            filteredUsers.addAll(users.filter {
                it.username.lowercase().contains(lowerQuery)
            })
        }
        notifyDataSetChanged()
    }
}