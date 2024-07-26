package org.apps.flexmed.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.R
import org.apps.flexmed.adapter.RecentChatAdapter
import org.apps.flexmed.adapter.UserChatAdapter
import org.apps.flexmed.databinding.FragmentHomeChatBinding
import org.apps.flexmed.model.RecentChat
import org.apps.flexmed.model.User
import org.apps.flexmed.ui.MainActivity

class HomeChatFragment : Fragment(), UserChatAdapter.OnItemClickListener, RecentChatAdapter.OnItemClickListener {

    private lateinit var binding: FragmentHomeChatBinding

    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var databaseRecentChat: DatabaseReference

    private lateinit var userAdapter: UserChatAdapter
    private lateinit var listUser: ArrayList<User>
    private lateinit var recentChatAdapter: RecentChatAdapter
    private lateinit var listRecentChat: ArrayList<RecentChat>

    private val userCache = mutableMapOf<String, User>()

    override fun onItemClick(user: User) {
        createOrGetChatRoom(user)
    }

    private fun createOrGetChatRoom(user: User) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val otherUserId = user.id
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)

        val chatRoomRef = FirebaseDatabase.getInstance().getReference("chat_rooms/$chatRoomId")

        chatRoomRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val chatRoomData = mapOf("created_at" to System.currentTimeMillis())
                chatRoomRef.setValue(chatRoomData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navigateToMessageFragment(chatRoomId, user)
                    }
                }
            } else {
                navigateToMessageFragment(chatRoomId, user)
            }
        }
    }

    private fun navigateToMessageFragment(chatRoomId: String, user: User) {
        val bundle = Bundle().apply {
            putString("chatRoomId", chatRoomId)
            putParcelable("user", user)
        }
        findNavController().navigate(R.id.action_homeChatFragment_to_messageFragment, bundle)
    }

    private fun getChatRoomId(user1Id: String, user2Id: String): String {
        return if (user1Id < user2Id) "$user1Id-$user2Id" else "$user2Id-$user1Id"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseRecentChat = FirebaseDatabase.getInstance().getReference("recent_chat")
        firebaseStore = FirebaseFirestore.getInstance()
        listUser = arrayListOf()
        listRecentChat = arrayListOf()


        fetchAllUsers()
        loadRecentChat()
        fetchData()
        setupBinding()
    }

    private fun setupBinding() {
        binding.apply {
            back.setOnClickListener {
                startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        }
    }

    private fun fetchData() {
        firebaseStore.collection("users").get()
            .addOnSuccessListener { result ->
                listUser.clear()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    listUser.add(user)
                }
                showUser(listUser)
            }
            .addOnFailureListener {}
    }

    private fun fetchAllUsers() {
        firebaseStore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    userCache[user.id] = user
                }
                recentChatAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {}
    }

    private fun loadRecentChat() {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUser != null) {
            databaseRecentChat.child(currentUser).orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listRecentChat.clear()
                        for (recentChatSnap in snapshot.children) {
                            val recentChat = recentChatSnap.getValue(RecentChat::class.java)
                            if (recentChat != null) {
                                recentChat.otherUserId = if (recentChat.senderId == currentUser) recentChat.receiverId else recentChat.senderId
                                listRecentChat.add(recentChat)
                            }
                        }
                        listRecentChat.sortByDescending { it.timestamp }
                        showRecentChat(listRecentChat)
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun showRecentChat(listRecentChat: ArrayList<RecentChat>) {
        binding.rvRecentChat.layoutManager = LinearLayoutManager(context)
        recentChatAdapter = RecentChatAdapter(listRecentChat, userCache, this)
        binding.rvRecentChat.adapter = recentChatAdapter
    }

    private fun showUser(listUser: ArrayList<User>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val filteredUserList = listUser.filter { it.id != userId }

        binding.rvUser.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        userAdapter = UserChatAdapter(java.util.ArrayList(filteredUserList), this)
        binding.rvUser.adapter = userAdapter
    }
}