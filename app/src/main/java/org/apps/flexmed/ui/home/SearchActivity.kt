package org.apps.flexmed.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.flexmed.R
import org.apps.flexmed.adapter.UserAdapter
import org.apps.flexmed.databinding.ActivitySearchBinding
import org.apps.flexmed.model.User
import org.apps.flexmed.ui.MainActivity
import org.apps.flexmed.ui.PopImageFragment
import org.apps.flexmed.ui.person.PersonFragment
import org.apps.flexmed.ui.post.PostFragment

class SearchActivity : AppCompatActivity(), UserAdapter.OnItemClickListener {

    private lateinit var binding: ActivitySearchBinding

    private lateinit var firebaseStore: FirebaseFirestore

    private lateinit var userAdapter: UserAdapter
    private lateinit var listUser: ArrayList<User>

    override fun onItemClick(user: User) {
        val fragment = PersonFragment.newInstance(user)

        supportFragmentManager.beginTransaction()
            .replace(R.id.person_fragment, fragment)
            .commit()

        binding.etSearch.clearFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseStore = FirebaseFirestore.getInstance()
        listUser = arrayListOf()

        fetchData()
        showUser()
        setupsBinding()
    }

    private fun setupsBinding() {
        binding.apply {
            back.setOnClickListener {
                startActivity(Intent(this@SearchActivity,MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }

            etSearch.requestFocus()
            etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    userAdapter.filter.filter(newText)
                    return false
                }
            })
        }
    }

    private fun fetchData() {
        firebaseStore.collection("users").get()
            .addOnSuccessListener { result ->
                listUser.clear()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    listUser.add(user)
                }
                userAdapter.updateUserList(listUser)
            }
            .addOnFailureListener {}
    }

    private fun showUser(){
        binding.rvUser.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(arrayListOf(), this)
        binding.rvUser.adapter = userAdapter
    }
}