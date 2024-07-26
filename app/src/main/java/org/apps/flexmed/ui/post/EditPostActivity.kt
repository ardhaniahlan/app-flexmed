package org.apps.flexmed.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.apps.flexmed.databinding.ActivityEditPostBinding
import org.apps.flexmed.model.Post
import org.apps.flexmed.ui.MainActivity

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databasePost: DatabaseReference

    private var selectedImageUri: Uri? = null

    companion object{
        const val POST = "post"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databasePost = FirebaseDatabase.getInstance().getReference("posts")

        displayPost()
        setupBinding()
    }

    private fun setupBinding() {
        binding.apply {

            btnUpdate.setOnClickListener {
                updatePost()
            }

            btnPreview.setOnClickListener {
                previewPost()
            }

            reset.setOnClickListener {
                reset()
            }

            back.setOnClickListener {
                finish()
            }
        }
    }

    private fun displayPost(){
        val post = intent?.getParcelableExtra<Post>("post")

        if (post != null) {
            binding.captions.setText(post.caption)
            Glide.with(this)
                .load(post.image)
                .into(binding.addPicture)
        }
    }

    private fun updatePost(){
        val post = intent?.getParcelableExtra<Post>("post")
        if (post != null){
            val captions = binding.captions.text.toString()

            if (selectedImageUri != null && captions.isNotEmpty()) {

                val updatePost = hashMapOf<String, Any>(
                    "image" to selectedImageUri.toString(),
                    "caption" to captions
                )
                databasePost.child(post.id!!).updateChildren(updatePost)
                    .addOnCompleteListener{
                        Toast.makeText(this, "Data TerUpdate", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Data Gagal DiUpdate", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Lengkapi Gambar atau Captionnya", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun previewPost(){
        val posts = intent?.getParcelableExtra<Post>("post")
        if (posts != null){
            selectedImageUri = posts.image?.toUri()
            val captions = binding.captions.text.toString()
            if (selectedImageUri != null && captions.isNotEmpty()) {
                val post = Post(
                    image = selectedImageUri.toString(),
                    caption = captions
                )

                val dialog = PreviewPostFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("review", post)
                    }
                }
                dialog.show(supportFragmentManager, "PreviewPostFragment")
            } else {
                Toast.makeText(this, "Lengkapi Gambar atau Captionnya", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reset(){
        binding.captions.text.clear()
    }
}