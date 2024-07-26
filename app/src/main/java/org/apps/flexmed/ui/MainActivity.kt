package org.apps.flexmed.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.apps.flexmed.R
import org.apps.flexmed.databinding.ActivityMainBinding
import org.apps.flexmed.ui.home.HomeFragment
import org.apps.flexmed.ui.person.PersonFragment
import org.apps.flexmed.ui.post.PostFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())

        binding.navbar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(HomeFragment())
                R.id.post -> replaceFragment(PostFragment())
                R.id.person -> replaceFragment(PersonFragment())
            }
            true
        }

        val navMenu = binding.navbar.menu
        for (i in 0 until navMenu.size()) {
            val menuItem = navMenu.getItem(i)
            val stateListDrawable = when (menuItem.itemId) {
                R.id.home -> ContextCompat.getDrawable(this, R.drawable.nav_home_selector)
                R.id.post -> ContextCompat.getDrawable(this, R.drawable.nav_post_selector)
                R.id.person -> ContextCompat.getDrawable(this, R.drawable.nav_person_selector)
                else -> null
            }
            stateListDrawable?.let {
                menuItem.icon = it
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
    }
}