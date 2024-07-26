package org.apps.flexmed.ui.person

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.ui.post.EditPostActivity
import org.apps.flexmed.adapter.PostAdapter
import org.apps.flexmed.databinding.FragmentLikeBinding
import org.apps.flexmed.model.Post
import org.apps.flexmed.ui.CommentFragment
import org.apps.flexmed.ui.PopImageFragment

class LikeFragment : Fragment(), PostAdapter.OnItemClickListener {

    private lateinit var binding: FragmentLikeBinding

    private lateinit var databasePost: DatabaseReference

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    private lateinit var postAdapter: PostAdapter

    override fun onItemClick(postId: String) {
        val dialog = CommentFragment().apply {
            arguments = Bundle().apply {
                putString("POST_ID", postId)
            }
        }
        dialog.show(childFragmentManager, "CommentFragment")
    }

    override fun onItemDelete(postId: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Ingin menghapus Postingan?")
            .setPositiveButton("OK") { _, _ ->
                removePostAndLikes(postId)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onItemEdit(post: Post) {
        startActivity(Intent(context, EditPostActivity::class.java)
            .putExtra(EditPostActivity.POST, post))
    }

    override fun onImageClick(post: Post) {
        val dialog = PopImageFragment().apply {
            arguments = Bundle().apply {
                putString("imagePost", post.image)
            }
        }
        dialog.show(childFragmentManager, "PopImageFragment")
    }

    private fun removePostAndLikes(postId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        val databaseRef = FirebaseDatabase.getInstance().reference

        val childUpdates = hashMapOf<String, Any?>(
            "/posts/$postId" to null,
            "/user_likes/$userId/$postId" to null
        )

        databaseRef.updateChildren(childUpdates).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Post Berhasil Dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Post Gagal Dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentLikeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        databasePost = FirebaseDatabase.getInstance().getReference("posts")

        showLikeSearchUser()
    }

    private fun showLikeSearchUser(){
        val userId = arguments?.getString("userId")
        if (userId != null){
            val userLikesRef = FirebaseDatabase.getInstance().getReference("user_likes/$userId")

            userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val likedPostIds = arrayListOf<String>()
                        for (postSnap in snapshot.children) {
                            val isLiked = postSnap.getValue(Boolean::class.java) ?: false
                            if (isLiked) {
                                postSnap.key?.let { likedPostIds.add(it) }
                            }
                        }
                        setupLikesPost(likedPostIds)
                    } else {
                        binding.rvPost.adapter = null
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            showPost()
        }
    }

    private fun showPost() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userLikesRef = FirebaseDatabase.getInstance().getReference("user_likes/$userId")

            userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val likedPostIds = arrayListOf<String>()
                        for (postSnap in snapshot.children) {
                            val isLiked = postSnap.getValue(Boolean::class.java) ?: false
                            if (isLiked) {
                                postSnap.key?.let { likedPostIds.add(it) }
                            }
                        }
                        setupLikesPost(likedPostIds)
                    } else {
                        binding.rvPost.adapter = null
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setupLikesPost(likedPostIds: ArrayList<String>) {
        if (likedPostIds.isEmpty()) {
            binding.rvPost.adapter = null
            return
        }

        val postsRef = FirebaseDatabase.getInstance().getReference("posts")
        val query = postsRef.orderByKey().startAt(likedPostIds.first()).endAt(likedPostIds.last())

        val options = FirebaseRecyclerOptions.Builder<Post>()
            .setIndexedQuery(query, postsRef) { snapshot ->
                snapshot.getValue(Post::class.java)!!
            }
            .build()

        postAdapter = PostAdapter(options, this) { position ->
            postAdapter.notifyItemChanged(position)
        }

        binding.rvPost.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }

        postAdapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        postAdapter.stopListening()
    }
}