package org.apps.flexmed.ui.home

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
import org.apps.flexmed.databinding.FragmentHomeBinding
import org.apps.flexmed.model.Post
import org.apps.flexmed.ui.CommentFragment
import org.apps.flexmed.ui.PopImageFragment
import org.apps.flexmed.ui.chat.ChatActivity

class HomeFragment : Fragment(), PostAdapter.OnItemClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databasePost: DatabaseReference
    private lateinit var postAdapter: PostAdapter

    override fun onItemClick(postId: String) {
        val dialog = CommentFragment().apply {
            arguments = Bundle().apply {
                putString("POST_ID", postId)
            }
        }
        dialog.show(childFragmentManager, "DialogFragment")
    }

    override fun onItemDelete(postId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Ingin menghapus Postingan?")
            .setPositiveButton("OK") { dialog, id ->
                removePostAndLikes(postId)
            }
            .setNegativeButton("Batal") { dialog, id ->
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
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        databasePost = FirebaseDatabase.getInstance().getReference("posts")

        showPost()
        setupBinding()

        postAdapter.startListening()
    }

    private fun setupBinding() {
        binding.apply {
            search.setOnClickListener {
                startActivity(Intent(context, SearchActivity::class.java))
            }
            chat.setOnClickListener {
                startActivity(Intent(requireContext(), ChatActivity::class.java))
            }
        }
    }

    private fun showPost() {
        val options = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(databasePost, Post::class.java)
            .build()

        postAdapter = PostAdapter(options, this) { position ->
            postAdapter.notifyItemChanged(position)
        }

        binding.rvPost.apply {
            layoutManager = LinearLayoutManager(context).apply{
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = postAdapter
        }
    }

    override fun onStop() {
        super.onStop()
        postAdapter.stopListening()
    }
}