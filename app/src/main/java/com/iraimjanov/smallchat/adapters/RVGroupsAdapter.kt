package com.iraimjanov.smallchat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.databinding.ItemGroupsBinding
import com.iraimjanov.smallchat.models.Groups

class RVGroupsAdapter(
    private val listGroups: List<Groups>,
    private val rvClickGroups: RVClickGroups,
) :
    RecyclerView.Adapter<RVGroupsAdapter.VH>() {

    inner class VH(var itemRV: ItemGroupsBinding) : RecyclerView.ViewHolder(itemRV.root) {
        fun onBind(groups: Groups) {
            if (groups.admin == profile.uid) {
                itemRV.imageSetting.visibility = View.VISIBLE
            }
            itemRV.tvName.text = groups.name
            if (groups.imageID.isNotEmpty()) {
                Glide.with(itemRV.root).load(groups.imageUrl).centerCrop().into(itemRV.imageProfile)
            }
            itemRV.root.setOnClickListener {
                rvClickGroups.click(groups)
            }
            itemRV.root.setOnLongClickListener {
                if (groups.admin == profile.uid) {
                    rvClickGroups.delete(groups)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemGroupsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(listGroups[position])

    }

    override fun getItemCount(): Int = listGroups.size

    interface RVClickGroups {
        fun click(groups: Groups)
        fun delete(groups: Groups)
    }

}