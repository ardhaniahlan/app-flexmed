package org.apps.flexmed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.apps.flexmed.adapter.UserChatAdapter.OnItemClickListener
import org.apps.flexmed.databinding.ItemRecentChatBinding
import org.apps.flexmed.model.RecentChat
import org.apps.flexmed.model.User

class RecentChatAdapter(
    private val listRecentChat: ArrayList<RecentChat>,
    private val userCache: Map<String, User>,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<RecentChatAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }

    inner class ViewHolder(private val binding: ItemRecentChatBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(recentChat: RecentChat, user: User?) {
            binding.apply {
                Glide.with(itemView)
                    .load(user?.image)
                    .circleCrop()
                    .into(imgProfil)
                username.text = user?.displayName
                lastMessage.text = recentChat.lastMessage

                itemView.setOnClickListener {
                    listener.onItemClick(user!!)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recentChat = listRecentChat[position]
        val user = userCache[recentChat.otherUserId]
        holder.bind(recentChat, user)
    }

    override fun getItemCount(): Int = listRecentChat.size
}