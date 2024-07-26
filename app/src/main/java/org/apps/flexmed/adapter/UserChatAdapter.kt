package org.apps.flexmed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import org.apps.flexmed.databinding.ItemUserChatBinding
import org.apps.flexmed.model.User

class UserChatAdapter(
    private val listUser: ArrayList<User>,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<UserChatAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }

    inner class ViewHolder(private val binding: ItemUserChatBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(users: User){
            binding.apply {
                displayName.text = users.displayName
                Glide.with(itemView)
                    .load(users.image)
                    .circleCrop()
                    .into(imgProfil)

                itemView.setOnClickListener {
                    listener.onItemClick(users)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val users = listUser[position]
        holder.bind(users)
    }

    override fun getItemCount(): Int = listUser.size
}