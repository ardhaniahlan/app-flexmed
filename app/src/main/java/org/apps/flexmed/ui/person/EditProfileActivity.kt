package org.apps.flexmed.ui.person

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.R
import org.apps.flexmed.databinding.ActivityEditProfileBinding
import org.apps.flexmed.model.User
import org.apps.flexmed.ui.post.PostFragment
import org.apps.flexmed.ui.post.PostFragment.Companion

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private var selectedImageUri: Uri? = null

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        displayUser()
        bindingDo()
    }

    private fun bindingDo() {
        binding.apply {
            back.setOnClickListener {
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

            editImg.setOnClickListener {
                showCeklis(true)
                if (checkPermission()) openGallery() else requestPermission()
            }

            ceklis.setOnClickListener {
                updateProfile()
            }

            setupEditText(etDisplayName)
            setupEditText(etUsername)
            setupEditText(etBio)
            showCeklis(false)
        }
    }

    private fun setupEditText(editText: EditText) {
        editText.setOnClickListener {
            showCeklis(true)
        }
    }

    private fun updateProfile(){
        val userId = firebaseAuth.currentUser?.uid

        val displayName = binding.etDisplayName.text.toString()
        val username = binding.etUsername.text.toString()
        val bio = binding.etBio.text.toString()

        val user = mutableMapOf(
            "displayName" to displayName,
            "username" to username,
            "bio" to bio
        )

        if (selectedImageUri != null) {
            user["image"] = selectedImageUri.toString()
        }

        val updateData = firebaseStore.collection("users").document(userId!!)
        updateData.update(user as Map<String, Any>)
            .addOnCompleteListener{
                Toast.makeText(this, "Data Diperbarui", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Data Gagal Diperbarui", Toast.LENGTH_SHORT).show()
            }

        showCeklis(false)
    }

    private fun showCeklis(isClick: Boolean){
        binding.ceklis.visibility = if (isClick) View.VISIBLE else View.GONE
    }

    private fun displayUser(){
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null){
            val displayName = firebaseStore.collection("users").document(userId)
            displayName.get().addOnSuccessListener {
                if (it.exists()){
                    val name = it.getString("displayName")
                    val username = it.getString("username")
                    val image = it.getString("image")
                    val bio = it.getString("bio")
                    binding.etDisplayName.setText(name)
                    binding.etUsername.setText(username)
                    Glide.with(this)
                        .load(image)
                        .circleCrop()
                        .into(binding.imgProfile)
                    binding.etBio.setText(bio)
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                binding.imgProfile.setImageURI(selectedImageUri)
                Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal mendapatkan URI gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PostFragment.IMAGE_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PostFragment.IMAGE_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
    }
}