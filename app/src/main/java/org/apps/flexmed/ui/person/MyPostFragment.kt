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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.apps.flexmed.ui.post.EditPostActivity
import org.apps.flexmed.adapter.PostAdapter
import org.apps.flexmed.databinding.FragmentMyPostBinding
import org.apps.flexmed.model.Post
import org.apps.flexmed.ui.CommentFragment
import org.apps.flexmed.ui.PopImageFragment

class MyPostFragment : Fragment(), PostAdapter.OnItemClickListener {

    private lateinit var binding: FragmentMyPostBinding

    private lateinit var databasePost: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

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
        // Inflate the layout for this fragment
        binding = FragmentMyPostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        databasePost = FirebaseDatabase.getInstance().getReference("posts")

        showPostSearchUser()
        postAdapter.startListening()
    }

    private fun showPostSearchUser(){
        val userId = arguments?.getString("userId")

        if (userId != null){
            val query = databasePost.orderByChild("userId").equalTo(userId).limitToLast(50)
            val options = FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()

            postAdapter = PostAdapter(options, this) { position ->
                postAdapter.notifyItemChanged(position)
            }

            binding.rvPost.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = postAdapter
            }

        } else {
            showPost()
        }
    }

    private fun showPost() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val query = databasePost.orderByChild("userId").equalTo(userId).limitToLast(50)
            val options = FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(query, Post::class.java)
                .build()

            postAdapter = PostAdapter(options, this) { position ->
                postAdapter.notifyItemChanged(position)
            }

            binding.rvPost.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = postAdapter
            }
        }
    }

    override fun onStop() {
        super.onStop()
        postAdapter.stopListening()
    }
}