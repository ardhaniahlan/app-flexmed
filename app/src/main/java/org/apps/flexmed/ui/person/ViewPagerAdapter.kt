package org.apps.flexmed.ui.person

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.apps.flexmed.ui.post.PostFragment

class ViewPagerAdapter(
    fragmentActivity: FragmentManager,
    lifecycle: Lifecycle,
    private val userId: String
) : FragmentStateAdapter(fragmentActivity, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val myPostFragment = MyPostFragment()
                val bundle = Bundle().apply {
                    putString("userId", userId)
                }
                myPostFragment.arguments = bundle
                myPostFragment
            }
            1 -> {
                val likeFragment = LikeFragment()
                val bundle = Bundle().apply {
                    putString("userId", userId)
                }
                likeFragment.arguments = bundle
                likeFragment
            }
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}