package org.apps.flexmed.ui.person

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import org.apps.flexmed.R
import org.apps.flexmed.PreferenceManager
import org.apps.flexmed.databinding.FragmentPersonBinding
import org.apps.flexmed.ui.home.SearchActivity
import org.apps.flexmed.ui.loginregis.LoginActivity
import java.util.HashMap

class PersonFragment : Fragment() {

    private lateinit var binding: FragmentPersonBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPersonBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        preferenceManager = PreferenceManager(requireContext())

        tabLayout()
        setupBinding()
        displaySearchUser()
    }

    private fun displaySearchUser(){
        val users = arguments?.getParcelable<org.apps.flexmed.model.User>("user")
        if (users != null){
            binding.displayName.text = users.displayName
            binding.username.text = "@${users.username}"
            context?.let { context ->
                Glide.with(context)
                    .load(users.image)
                    .circleCrop()
                    .into(binding.imgProfile)
            }
            binding.bio.text = users.bio

            viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle, users.id)
            binding.viewPage2.adapter = viewPagerAdapter

            binding.back.visibility = View.VISIBLE
            binding.back.setOnClickListener {
                startActivity(Intent(context, SearchActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }

            binding.editProfile.visibility = View.GONE
        } else {
            displayUser()
        }
    }

    private fun tabLayout(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null){
            viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle, userId)
            binding.viewPage2.adapter = viewPagerAdapter
        }

        TabLayoutMediator(binding.tablayout, binding.viewPage2) { tab, position ->
            tab.setCustomView(R.layout.tab_item)
            val tabView = tab.customView
            val tabIconView = tabView?.findViewById<ImageView>(R.id.tab_icon)
            val tabTextView = tabView?.findViewById<TextView>(R.id.tab_text)
            when (position) {
                0 -> {
                    tabTextView?.text = "My Post"
                    tabIconView?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_mypost, null))
                }
                1 -> {
                    tabTextView?.text = "Likes"
                    tabIconView?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_likes, null))
                }
            }
        }.attach()
    }

    private fun displayUser(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null){
            val displayName = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
            displayName.get().addOnSuccessListener {
                if (it.exists()){
                    val name = it.getString("displayName")
                    val username = it.getString("username")
                    val image = it.getString("image")
                    val bio = it.getString("bio")
                    binding.displayName.text = name
                    binding.username.text = "@${username}"
                    context?.let { context ->
                        Glide.with(context)
                            .load(image)
                            .circleCrop()
                            .into(binding.imgProfile)
                    }
                    binding.bio.text = bio
                }
            }
        }
    }

    private fun setupBinding(){
        binding.apply {
            logout.setOnClickListener {
                logoutUser()
            }

            editProfile.setOnClickListener {
                startActivityForResult(Intent(context, EditProfileActivity::class.java), REQUEST_CODE)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            displayUser()
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        preferenceManager.setLoggedIn(false)

        showProgressbar(true)
        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(
                requireContext(),
                "Anda Keluar",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            showProgressbar(false)
        }, 2000)
    }

    private fun showProgressbar(isLoading: Boolean){
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(user: org.apps.flexmed.model.User) =
            PersonFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("user", user)
                }
            }

        const val REQUEST_CODE = 1
    }
}