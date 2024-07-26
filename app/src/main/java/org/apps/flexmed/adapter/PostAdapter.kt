package org.apps.flexmed.adapter

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apps.flexmed.R
import org.apps.flexmed.databinding.ItemPostBinding
import org.apps.flexmed.model.Post
import org.apps.flexmed.ui.MainActivity
import org.apps.flexmed.ui.loginregis.LoginActivity

class PostAdapter(
    options: FirebaseRecyclerOptions<Post>,
    private val listener: OnItemClickListener,
    private val onLikeClick: (position: Int) -> Unit
) : FirebaseRecyclerAdapter<Post, PostAdapter.ViewHolder>(options) {

    interface OnItemClickListener {
        fun onItemClick(postId: String)
        fun onItemDelete(postId: String)
        fun onItemEdit(post: Post)
        fun onImageClick(post: Post)
    }

    inner class ViewHolder(
        private val binding: ItemPostBinding,
        private val onLikeClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(posts: Post) {
            binding.apply {
                showProgressbar(true)
                Handler(Looper.getMainLooper()).postDelayed({
                    displayName.text = posts.displayName
                    Glide.with(itemView)
                        .load(posts.imgUser)
                        .into(imgProfil)

                    val idUser = posts.userId
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

                            binding.optionMenu.visibility = if (idUser == currentUserId) View.VISIBLE else View.GONE
                        }
                    } else {
                        binding.optionMenu.visibility = View.GONE
                    }

                    caption.text = posts.caption
                    Glide.with(itemView)
                        .load(posts.image)
                        .into(imgPost)

                    val postRef = FirebaseDatabase.getInstance().getReference("posts/${posts.id}")
                    postRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val likesCount = snapshot.child("likes_count").getValue(Int::class.java) ?: 0
                                posts.likesCount = likesCount
                                countLikes.text = likesCount.toString()

                                val commentsCount = snapshot.child("comments").childrenCount.toInt()
                                posts.commentsCount = commentsCount
                                countComments.text = commentsCount.toString()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                    if (currentUserId != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val isLiked = getUserLikeStatus(currentUserId, posts.id!!)
                            posts.isLiked = isLiked
                            updateLikeButton(isLiked)
                        }
                    }

                    updateLikeButton(posts.isLiked)

                    like.setOnClickListener {
                        posts.isLiked = !posts.isLiked
                        updateLikeButton(posts.isLiked)
                        updateLikeStatusInDatabase(posts, currentUserId)
                        onLikeClick(adapterPosition)
                    }

                    comment.setOnClickListener{
                        listener.onItemClick(posts.id!!)
                    }

                    imgPost.setOnClickListener {
                        listener.onImageClick(posts)
                    }

                    optionMenu.setOnClickListener {
                        val popUpMenu = PopupMenu(itemView.context, optionMenu)
                        popUpMenu.inflate(R.menu.option_post_item)

                        try {
                            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                            fieldMPopup.isAccessible = true
                            val mPopup = fieldMPopup.get(popUpMenu)
                            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                                .invoke(mPopup, true)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        popUpMenu.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.delete -> {
                                    listener.onItemDelete(posts.id!!)
                                    true
                                }

                                R.id.edit -> {
                                    listener.onItemEdit(posts)
                                    true
                                }
                                else -> false
                            }
                        }
                        popUpMenu.show()
                    }
                    showProgressbar(false)
                }, 1000)
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

        private suspend fun getUserLikeStatus(userId: String, postId: String): Boolean {
            return try {
                val postRef = FirebaseDatabase.getInstance().getReference("user_likes/$userId/$postId")
                val dataSnapshot = postRef.get().await()
                dataSnapshot.getValue(Boolean::class.java) ?: false
            } catch (e: Exception) {
                false
            }
        }

        private fun updateLikeStatusInDatabase(posts: Post, userId: String?) {
            if (userId != null) {
                val postLikeRef = FirebaseDatabase.getInstance().getReference("posts/${posts.id}/likes/$userId")
                val userLikesRef = FirebaseDatabase.getInstance().getReference("user_likes/$userId/${posts.id}")
                val postLikesCountRef = FirebaseDatabase.getInstance().getReference("posts/${posts.id}/likes_count")

                postLikeRef.setValue(posts.isLiked).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val newLikesCount = if (posts.isLiked) posts.likesCount + 1 else posts.likesCount - 1
                        postLikesCountRef.setValue(newLikesCount).addOnCompleteListener { countTask ->
                            if (countTask.isSuccessful) {
                                posts.likesCount = newLikesCount
                                binding.countLikes.text = newLikesCount.toString()
                            }
                        }
                    }
                }
                userLikesRef.setValue(posts.isLiked)
            }
        }

        private fun updateLikeButton(isLiked: Boolean) {
            if (isLiked) {
                binding.like.setImageResource(R.drawable.ic_heart_active)
            } else {
                binding.like.setImageResource(R.drawable.ic_heart_inactive)
            }
        }

        private fun showProgressbar(isLoading: Boolean){
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onLikeClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Post) {
        holder.bind(model)
    }

}


