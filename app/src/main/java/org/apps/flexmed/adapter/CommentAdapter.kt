package org.apps.flexmed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apps.flexmed.databinding.ItemCommentBinding
import org.apps.flexmed.model.Comment

class CommentAdapter(
    options: FirebaseRecyclerOptions<Comment>,
    private val listener: OnItemClickListener
) : FirebaseRecyclerAdapter<Comment, CommentAdapter.CommentViewHolder>(options) {

    interface OnItemClickListener {
        fun onItemDelete(commentId: String)
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comments: Comment, listener: OnItemClickListener) {
            binding.apply {
                val idUser = comments.userId
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                if (idUser != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val document = getUserDocument(idUser)
                        if (document != null) {
                            val displayName = document.getString("displayName")
                            val imgUser = document.getString("image")

                            binding.displayName.text = displayName
                            Glide.with(itemView)
                                .load(imgUser)
                                .circleCrop()
                                .into(binding.imgProfil)
                        }
                        binding.delete.visibility = if (idUser == currentUserId) View.VISIBLE else View.GONE
                    }
                } else {
                    binding.delete.visibility = View.GONE
                }
                comment.text = comments.content

                binding.delete.setOnClickListener {
                    listener.onItemDelete(comments.id!!)
                }
            }
        }

        private suspend fun getUserDocument(userId: String): DocumentSnapshot? {
            return try {
                val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                userRef.get().await()
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: Comment) {
        holder.bind(model, listener)
    }
}