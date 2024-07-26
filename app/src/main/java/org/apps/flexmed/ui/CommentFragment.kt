package org.apps.flexmed.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.adapter.CommentAdapter
import org.apps.flexmed.databinding.FragmentCommentBinding
import org.apps.flexmed.model.Comment

class CommentFragment : DialogFragment(), CommentAdapter.OnItemClickListener {

    private lateinit var binding: FragmentCommentBinding
    private lateinit var databasePost: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var commentAdapter: CommentAdapter

    private var postId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getString("POST_ID")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        databasePost = FirebaseDatabase.getInstance().getReference("posts/${postId}/comments")

        setupRecyclerView()
        bindingDo()
    }

    private fun setupRecyclerView() {
        val query = databasePost.orderByKey()
        val options = FirebaseRecyclerOptions.Builder<Comment>()
            .setQuery(query, Comment::class.java)
            .build()

        commentAdapter = CommentAdapter(options, this)
        binding.rvComment.layoutManager = LinearLayoutManager(context)
        binding.rvComment.adapter = commentAdapter
    }

    private fun saveComment() {
        val content = binding.commentInput.text.toString()
        postId = arguments?.getString("POST_ID")

        if (content.isNotEmpty()) {
            val userId = firebaseAuth.currentUser?.uid
            val id = databasePost.push().key
            val comment = Comment(
                id = id!!,
                content = content,
                postId = postId,
                userId = userId
            )

            databasePost.child(id).setValue(comment).addOnCompleteListener {
                binding.commentInput.text.clear()
            }
        }
    }

    private fun bindingDo() {
        binding.apply {
            submit.setOnClickListener {
                saveComment()
            }
            commentInput.requestFocus()
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            silang.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        commentAdapter.startListening()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onStop() {
        super.onStop()
        commentAdapter.stopListening()
    }

    override fun onItemDelete(commentId: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Ingin menghapus Komentar?")
            .setPositiveButton("OK") { _, _ ->
                removeComment(commentId)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun removeComment(commentId: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference

        val childUpdates = hashMapOf<String, Any?>(
            "posts/$postId/comments/$commentId" to null
        )

        databaseRef.updateChildren(childUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Komentar Berhasil Dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Komentar Gagal Dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }
}