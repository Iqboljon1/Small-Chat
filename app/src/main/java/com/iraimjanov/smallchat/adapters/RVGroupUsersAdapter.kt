package com.iraimjanov.smallchat.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iraimjanov.smallchat.databinding.ItemGroupUsersBinding
import com.iraimjanov.smallchat.models.PersonalUsers

class RVGroupUsersAdapter(
    private val listPersonalUsers: ArrayList<PersonalUsers>,
    private val admin: String,
) :
    RecyclerView.Adapter<RVGroupUsersAdapter.VH>() {

    inner class VH(var itemRV: ItemGroupUsersBinding) : RecyclerView.ViewHolder(itemRV.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(personalUsers: PersonalUsers) {
            itemRV.tvName.text = "${personalUsers.firstName} ${personalUsers.lastName}"
            if (personalUsers.imageId.isNotEmpty()) {
                Glide.with(itemRV.root).load(personalUsers.imageUrl).into(itemRV.imageProfile)
            }
            if (personalUsers.uid == admin) {
                itemRV.tvOwner.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemGroupUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(listPersonalUsers[position])

    }

    override fun getItemCount(): Int = listPersonalUsers.size

}