package com.iraimjanov.smallchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iraimjanov.smallchat.databinding.ItemSearchGroupsBinding
import com.iraimjanov.smallchat.models.Groups

class RVSearchGroupsAdapter(
    private val listGroups: ArrayList<Groups>,
    private val rvClickSearchGroups: RVClickSearchGroups,
) :
    RecyclerView.Adapter<RVSearchGroupsAdapter.VH>() {

    inner class VH(var itemRV: ItemSearchGroupsBinding) : RecyclerView.ViewHolder(itemRV.root) {
        fun onBind(groups: Groups) {
            itemRV.tvName.text = groups.name
            itemRV.tvMembersCount.text = groups.listPersonalUsers.size.toString()
            if (groups.imageID.isNotEmpty()) {
                Glide.with(itemRV.root).load(groups.imageUrl).into(itemRV.imageProfile)
            }
            itemRV.root.setOnClickListener {
                rvClickSearchGroups.click(groups)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemSearchGroupsBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(listGroups[position])

    }

    override fun getItemCount(): Int = listGroups.size

    interface RVClickSearchGroups {
        fun click(groups: Groups)
    }

}