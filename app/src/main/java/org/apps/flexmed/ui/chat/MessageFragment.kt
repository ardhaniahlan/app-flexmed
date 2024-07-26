package org.apps.flexmed.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.apps.flexmed.adapter.ChatMessageAdapter
import org.apps.flexmed.databinding.FragmentMessageBinding
import org.apps.flexmed.model.ChatMessage
import org.apps.flexmed.model.RecentChat
import org.apps.flexmed.model.User

class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding

    private lateinit var databaseChatMessage: DatabaseReference

    private lateinit var databaseRecentChat: DatabaseReference
    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private lateinit var listMessage: ArrayList<ChatMessage>

    private lateinit var chatRoomId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMessageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            chatRoomId = it.getString("chatRoomId").orEmpty()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseChatMessage = FirebaseDatabase.getInstance().getReference("chat_rooms/${chatRoomId}/messages")
        databaseRecentChat = FirebaseDatabase.getInstance().getReference("recent_chat")
        listMessage = arrayListOf()

        setupBinding()
        loadMessage()
    }

    private fun setupBinding() {
        binding.apply {
            btnSend.setOnClickListener {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val receivedId = arguments?.getParcelable<User>("user")

        if (messageText.isNotEmpty() && userId != null && receivedId != null){
            val id = databaseChatMessage.push().key
            val chatMessage = ChatMessage(
                id = id,
                senderId = userId,
                receiverId = receivedId.id,
                message = messageText
            )
            databaseChatMessage.child(id!!).setValue(chatMessage)
            updateRecentChat(userId, receivedId.id,messageText, chatRoomId)
            binding.etMessage.text.clear()
        }
    }

    private fun updateRecentChat(senderId: String, receiverId: String, message: String, chatRoomId: String){
        val timestamp = System.currentTimeMillis()
        val recentChatData = RecentChat(
            senderId = senderId,
            receiverId = receiverId,
            lastMessage = message,
            timestamp = timestamp
        )
        databaseRecentChat.child(senderId).child(chatRoomId).setValue(recentChatData)
        databaseRecentChat.child(receiverId).child(chatRoomId).setValue(recentChatData)
    }

    private fun loadMessage(){
        databaseChatMessage.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listMessage.clear()
                    for (messageSnapshot in snapshot.children) {
                        val chatMessage = messageSnapshot.getValue(ChatMessage::class.java)
                        chatMessage?.let { listMessage.add(it) }
                    }
                    showMessage(listMessage)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showMessage(listMessage: ArrayList<ChatMessage>) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        binding.rvMessage.layoutManager = LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
            stackFromEnd = true
        }
        chatMessageAdapter = ChatMessageAdapter(listMessage, currentUserId!!)
        binding.rvMessage.adapter = chatMessageAdapter

    }
}