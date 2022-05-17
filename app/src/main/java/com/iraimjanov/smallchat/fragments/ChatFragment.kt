package com.iraimjanov.smallchat.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.smallchat.adapters.RVChatAdapter
import com.iraimjanov.smallchat.data.BuildPopupMenuForPersonalChats
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.databinding.FragmentChatBinding
import com.iraimjanov.smallchat.models.Messages
import com.iraimjanov.smallchat.models.PersonalUsers
import com.iraimjanov.smallchat.R
import java.util.*

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var chatProfile: PersonalUsers
    private lateinit var realReference: DatabaseReference
    private lateinit var editingMessages: Messages
    private var editingPosition = -1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())
        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_chatFragment_to_noInternetFragment)
        }
        return binding.root
    }

    private fun connection() {
        loadData()
        showActivity()
        getMessages()
        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.imageSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                if (binding.lyTextEditing.visibility == View.VISIBLE) {
                    editMessage(editingMessages)
                    BuildPopupMenuForPersonalChats(requireActivity(), binding).showSentMode()
                    binding.edtMessage.requestFocus()
                } else {
                    sentMessage(message)
                    BuildPopupMenuForPersonalChats(requireActivity(), binding).showSentMode()
                }
            } else {
                if (binding.lyTextEditing.visibility == View.VISIBLE) {
                    Toast.makeText(requireActivity(),
                        "In this case, you cannot change it",
                        Toast.LENGTH_SHORT).show()
                    BuildPopupMenuForPersonalChats(requireActivity(), binding).showSentMode()
                } else {
                    Toast.makeText(requireActivity(), "No message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getMessages() {
        realReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listMessages = ArrayList<Messages>()
                for (child in snapshot.children) {
                    val messages = child.getValue(Messages::class.java)
                    if (messages != null) {
                        if (messages.from == profile.uid && messages.to == chatProfile.uid || messages.from == chatProfile.uid && messages.to == profile.uid) {
                            listMessages.add(messages)
                        }
                    }
                }
                binding.rvChats.adapter =
                    RVChatAdapter(listMessages, profile.uid, object : RVChatAdapter.RVClickChats {
                        override fun click(messages: Messages, view: View, position: Int) {
                            editingPosition = position
                            editingMessages = messages
                            BuildPopupMenuForPersonalChats(requireActivity(), binding).forChats(
                                messages,
                                view)
                        }
                    })
                if (editingPosition != -1) {
                    binding.rvChats.layoutManager!!.scrollToPosition(editingPosition)
                    editingPosition = -1
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sentMessage(message: String) {
        val currentTime = Calendar.getInstance().time
        val messages = Messages(realReference.push().key!!,
            message,
            currentTime.time.toString(),
            profile.uid,
            chatProfile.uid)
        realReference.child(messages.uid).setValue(messages)
        binding.edtMessage.text.clear()
    }

    private fun editMessage(messages: Messages) {
        val newMessages = Messages(messages.uid,
            binding.edtMessage.text.toString().trim(),
            messages.time,
            messages.from,
            messages.to)
        realReference.child(newMessages.uid).setValue(newMessages)
    }

    private fun loadData() {
        realReference = Firebase.database.getReference("chats")
        chatProfile = PublicData.chatProfile
    }

    @SuppressLint("SetTextI18n")
    private fun showActivity() {
        binding.edtMessage.requestFocus()
        binding.tvName.text = "${chatProfile.firstName} ${chatProfile.lastName}"
        binding.tvName.isSelected = true
        if (chatProfile.imageId.isNotEmpty()) {
            Glide.with(binding.root).load(chatProfile.imageUrl).into(binding.imageProfile)
        }
    }



}