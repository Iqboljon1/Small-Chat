package com.iraimjanov.smallchat.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iraimjanov.smallchat.models.Messages
import com.iraimjanov.smallchat.databinding.ItemFromBinding
import com.iraimjanov.smallchat.databinding.ItemToBinding
import java.text.SimpleDateFormat
import java.util.*


class RVChatAdapter(val list: List<Messages>, var uid: String, val rvClickChats: RVClickChats) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class FromVh(var itemRv: ItemFromBinding) : RecyclerView.ViewHolder(itemRv.root) {

        fun onBind(messages: Messages, position: Int) {
            itemRv.tvMessages.text = messages.messages
            itemRv.tvTime.text = getTime(messages.time.toLong())
            itemRv.root.setOnClickListener {
                rvClickChats.click(messages, itemRv.tvTime, position)
            }
        }
    }

    inner class ToVh(var itemRv: ItemToBinding) : RecyclerView.ViewHolder(itemRv.root) {
        fun onBind(messages: Messages, position: Int) {
            itemRv.tvMessages.text = messages.messages
            itemRv.tvTime.text = getTime(messages.time.toLong())
            itemRv.root.setOnClickListener {
                rvClickChats.click(messages, itemRv.tvTime, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == 1) {
            FromVh(ItemFromBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false))
        } else {
            ToVh(ItemToBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        return if (list[position].from == uid) {
            1
        } else {
            2
        }
    }

    override fun getItemCount(): Int = list.size

    interface RVClickChats {
        fun click(messages: Messages, view: View, position: Int)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(time: Long): String {
        val tz: TimeZone = TimeZone.getDefault()
        val destFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        destFormat.timeZone = tz
        return destFormat.format(time)
    }
}