package com.sstek.javca.main.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.sstek.javca.databinding.ItemUserBinding
import com.sstek.javca.user.domain.entity.User

import android.R
import android.util.Log

class PeopleAdapter(
    private var users: List<User>,
    var currentUser: User,
    private val onItemClick: (User) -> Unit,
    private val onCallClick: (User) -> Unit,
    private val onFavoriteToggle: (User) -> Unit
) : RecyclerView.Adapter<PeopleAdapter.UserViewHolder>() {

    private val filteredUsers = mutableListOf<User>()

    init {
        updateList()
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.usernameTextView.text = user.username
            binding.root.setOnClickListener { onItemClick(user) }
            binding.callButton.setOnClickListener { onCallClick(user) }

            val isFavorite = currentUser.favorites.containsKey(user.uid)
            binding.toggleFavoriteButton.setImageResource(
                if (isFavorite) {
                    com.sstek.javca.R.drawable.star_filled
                } else {
                    com.sstek.javca.R.drawable.star_empty
                }
            )

            binding.toggleFavoriteButton.setOnClickListener {
                onFavoriteToggle(user)
                // currentUser dışarıda güncellendiğinde adaptere yeni currentUser gelmeli
                // ve adapter dışardan updateCurrentUser fonksiyonuyla bilgilendirilmeli
            }

            /* TODO(belki sonra)
            val isOnline = user.status == "online"
            binding.statusIndicator.setImageResource(
                if (isOnline) {
                    com.sstek.javca.R.drawable.online_indicator
                } else {
                    com.sstek.javca.R.drawable.offline_indicator
                }
            )
             */
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
        users = newUsers.filter { it.uid != currentUser.uid }
        updateList()
    }

    fun updateCurrentUser(newCurrentUser: User) {
        currentUser = newCurrentUser
        updateList()
    }

    private fun updateList() {
        // Favoriler en üstte olacak şekilde sırala
        val favs = currentUser.favorites.keys
        val sorted = users.sortedWith(
            compareByDescending<User> { favs.contains(it.uid) }
                .thenBy { it.username }
        )
        filteredUsers.clear()
        filteredUsers.addAll(sorted)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val lowerQuery = query.lowercase()
        val filteredList = if (lowerQuery.isEmpty()) {
            users
        } else {
            users.filter { it.username.lowercase().contains(lowerQuery) }
        }

        val favs = currentUser.favorites.keys
        val sorted = filteredList.sortedWith(
            compareByDescending<User> { favs.contains(it.uid) }
                .thenBy { it.username }
        )

        filteredUsers.clear()
        filteredUsers.addAll(sorted)
        notifyDataSetChanged()
    }

}

