package org.apps.flexmed.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.R
import org.apps.flexmed.databinding.FragmentPreviewPostBinding
import org.apps.flexmed.model.Post
import org.apps.flexmed.ui.MainActivity

@Suppress("DEPRECATION")
class PreviewPostFragment : DialogFragment() {

    private lateinit var binding: FragmentPreviewPostBinding

    private lateinit var databasePost: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPreviewPostBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        databasePost = FirebaseDatabase.getInstance().getReference("posts")

        val posts = arguments?.getParcelable<Post>("review")
        if (posts != null){
            binding.apply {
                Glide.with(requireContext())
                    .load(posts.image)
                    .into(binding.imgPost)
                caption.text = posts.caption
            }
        }

        setupBinding()

        return binding.root
    }


    private fun setupBinding(){
        binding.apply {
            silang.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

}
