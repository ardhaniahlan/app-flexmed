package org.apps.flexmed.ui.post

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.R
import org.apps.flexmed.databinding.FragmentPostBinding
import org.apps.flexmed.model.Post
import org.apps.flexmed.ui.MainActivity
import org.apps.flexmed.ui.person.PersonFragment

@Suppress("DEPRECATION")
class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding

    private lateinit var databasePost: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    private var selectedImageUri: Uri? = null

    companion object {
        const val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPostBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        databasePost = FirebaseDatabase.getInstance().getReference("posts")

        setupBinding()

        return binding.root
    }

    private fun previewPost() {
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
            dialog.show(childFragmentManager, "PreviewPostFragment")
        } else {
            Toast.makeText(context, "Lengkapi Gambar atau Captionnya", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePost(){
        val captions = binding.captions.text.toString()

        if (selectedImageUri != null && captions.isNotEmpty()) {
            val id = databasePost.push().key
            val userId = firebaseAuth.currentUser?.uid
            val post = Post(
                id = id!!,
                image = selectedImageUri.toString(),
                caption = captions,
                userId = userId)
            databasePost.child(id).setValue(post)
                .addOnCompleteListener{
                    Toast.makeText(context, "Data DiUpload", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Data Gagal DiUpload", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Lengkapi Gambar atau Captionnya", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBinding() {
        binding.apply {
            addPicture.setOnClickListener {
                if (checkPermission()) openGallery() else requestPermission()
            }

            btnPreview.setOnClickListener {
                previewPost()
            }

            reset.setOnClickListener {
                reset()
            }

            btnShare.setOnClickListener {
                savePost()
            }
        }
    }

    private fun reset(){
        binding.captions.text.clear()
        binding.addPicture.setImageResource(R.drawable.ic_image)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                binding.addPicture.setImageURI(selectedImageUri)
                Toast.makeText(context, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal mendapatkan URI gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireContext() as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IMAGE_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
    }
}
