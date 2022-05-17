package com.iraimjanov.smallchat.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.databinding.ItemFromBinding
import com.iraimjanov.smallchat.databinding.ItemGroupMessagesBinding
import com.iraimjanov.smallchat.models.GroupMessages
import java.text.SimpleDateFormat
import java.util.*

class RVGroupsChatAdapter(
    val list: List<GroupMessages>,
    val rvClickGroupsChats: RVClickGroupsChats,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FromVh(var itemRv: ItemFromBinding) :
        RecyclerView.ViewHolder(itemRv.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(groupMessages: GroupMessages, position: Int) {
            itemRv.tvMessages.text = groupMessages.messages
            itemRv.tvTime.text = getTime(groupMessages.time.toLong())
            itemRv.root.setOnClickListener {
                rvClickGroupsChats.click(groupMessages, itemRv.tvTime, position)
            }
        }
    }

    inner class ToVh(var itemRv: ItemGroupMessagesBinding) : RecyclerView.ViewHolder(itemRv.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(groupMessages: GroupMessages, position: Int) {
            itemRv.tvMessages.text = groupMessages.messages
            itemRv.tvTime.text = getTime(groupMessages.time.toLong())
            itemRv.tvUserName.text = "${groupMessages.userFirstname} ${groupMessages.userLastname}"
            if (groupMessages.userImageId.isNotEmpty()) {
                Glide.with(itemRv.root).load(groupMessages.userImageUrl).centerCrop()
                    .into(itemRv.chatProfileImage)
            }
            itemRv.root.setOnClickListener {
                if (groupMessages.userUid == profile.uid) {
                    rvClickGroupsChats.click(groupMessages, itemRv.tvTime, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == 1) {
            FromVh(ItemFromBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false))
        } else {
            ToVh(ItemGroupMessagesBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (getItemViewType(position) == 1) {
            val fromVh = holder as FromVh
            fromVh.onBind(list[position], position)
        } else {
            val toVh = holder as ToVh
            toVh.onBind(list[position], position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].userUid == profile.uid) {
            1
        } else {
            2
        }
    }

    override fun getItemCount(): Int = list.size

    interface RVClickGroupsChats {
        fun click(groupMessages: GroupMessages, view: View, position: Int)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(time: Long): String {
        val tz: TimeZone = TimeZone.getDefault()
        val destFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        destFormat.timeZone = tz
        return destFormat.format(time)
    }

}