package org.apps.flexmed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.apps.flexmed.databinding.ItemUserBinding
import org.apps.flexmed.model.User

class UserAdapter(
    private val listUser: ArrayList<User>,
    private val listener: OnItemClickListener
): RecyclerView.Adapter<UserAdapter.ViewHolder>(), Filterable {

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }

    inner class ViewHolder(private val binding: ItemUserBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(users: User){
            binding.apply {
                displayName.text = users.displayName
                Glide.with(itemView)
                    .load(users.image)
                    .circleCrop()
                    .into(imgProfil)

                itemView.setOnClickListener {
                    listener.onItemClick(users)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val users = listUser[position]
        holder.bind(users)
    }

    override fun getItemCount(): Int = listUser.size

    private var userListFiltered: ArrayList<User> = ArrayList(listUser)

    override fun getFilter(): Filter {
        return userFilter
    }

    private val userFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<User>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(userListFiltered)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()

                for (user in userListFiltered) {
                    if (user.displayName!!.toLowerCase().contains(filterPattern)) {
                        filteredList.add(user)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            listUser.clear()
            if (constraint.isNullOrEmpty()) {
                // Ketika etSearch Kosong maka List Kosong
            } else {
                if (results?.values is ArrayList<*>) {
                    listUser.addAll(results.values as ArrayList<User>)
                }
            }
            notifyDataSetChanged()
        }
    }

    fun updateUserList(newUsers: ArrayList<User>) {
        userListFiltered = newUsers
        notifyDataSetChanged()
    }
}