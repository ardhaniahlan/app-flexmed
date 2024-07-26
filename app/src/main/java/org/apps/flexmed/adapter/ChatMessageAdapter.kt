package org.apps.flexmed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.apps.flexmed.Utils
import org.apps.flexmed.databinding.ItemChatReceiverBinding
import org.apps.flexmed.databinding.ItemChatSenderBinding
import org.apps.flexmed.model.ChatMessage

class ChatMessageAdapter(private val listMessage: ArrayList<ChatMessage>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (listMessage[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    inner class SenderViewHolder(private val binding: ItemChatSenderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage) {
            binding.tvMessage.text = chat.message
            binding.tvTimestamp.text = Utils.convertTimestampToRealTime(chat.timestamp)
        }
    }

    inner class ReceiverViewHolder(private val binding: ItemChatReceiverBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage) {
            binding.tvMessage.text = chat.message
            binding.tvTimestamp.text = Utils.convertTimestampToRealTime(chat.timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT){
            val binding = ItemChatSenderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SenderViewHolder(binding)
        } else {
            val binding = ItemChatReceiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceiverViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messages = listMessage[position]
        if (holder is SenderViewHolder){
            holder.bind(messages)
        } else if (holder is ReceiverViewHolder){
            holder.bind(messages)
        }

    }

    override fun getItemCount(): Int = listMessage.size
}
