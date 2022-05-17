package com.iraimjanov.smallchat.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iraimjanov.smallchat.databinding.ItemUsersBinding
import com.iraimjanov.smallchat.models.PersonalUsers

class RVUsersAdapter(
    private val listUsers: ArrayList<PersonalUsers>,
    private val rvClickUsers: RVClickUsers,
) :
    RecyclerView.Adapter<RVUsersAdapter.VH>() {

    inner class VH(var itemRV: ItemUsersBinding) : RecyclerView.ViewHolder(itemRV.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(personalUsers: PersonalUsers) {
            itemRV.tvName.text = "${personalUsers.firstName} ${personalUsers.lastName}"
            if (personalUsers.imageId.isNotEmpty()) {
                Glide.with(itemRV.root).load(personalUsers.imageUrl).into(itemRV.imageProfile)
            }
            itemRV.root.setOnClickListener {
                rvClickUsers.click(personalUsers)
            }
            itemRV.root.setOnLongClickListener {
                rvClickUsers.delete(personalUsers)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(listUsers[position])

    }

    override fun getItemCount(): Int = listUsers.size

    interface RVClickUsers {
        fun click(personalUsers: PersonalUsers)
        fun delete(personalUsers: PersonalUsers)
    }
}