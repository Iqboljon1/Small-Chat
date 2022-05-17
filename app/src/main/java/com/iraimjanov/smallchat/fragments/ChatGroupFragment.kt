package com.iraimjanov.smallchat.fragments

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
import com.iraimjanov.smallchat.adapters.RVGroupsChatAdapter
import com.iraimjanov.smallchat.data.*
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.databinding.FragmentChatGroupBinding
import com.iraimjanov.smallchat.models.GroupMessages
import com.iraimjanov.smallchat.models.Groups
import com.iraimjanov.smallchat.R
import java.util.*


class ChatGroupFragment : Fragment() {
    private lateinit var binding: FragmentChatGroupBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var realReference: DatabaseReference
    private lateinit var realReferenceChats: DatabaseReference
    private var groups = PublicData.groups
    private lateinit var editingMessages: GroupMessages
    private var editingPosition = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChatGroupBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())
        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_chatGroupFragment_to_noInternetFragment)
        }
        return binding.root
    }

    private fun connection() {
        realReference = Firebase.database.getReference("groups").child(groups.uid)
        realReferenceChats = Firebase.database.getReference("groupsChats").child(groups.uid)
        getGroup()
        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.imageLogout.setOnClickListener {
            UpdatePersonalUsers().deleteUserFromGroup(groups.uid, profile.uid)
            UpdatePersonalUsers().logoutTheGroup(profile, groups.uid)
            findNavController().popBackStack()
        }
        binding.imageEdit.setOnClickListener {
            findNavController().navigate(R.id.action_chatGroupFragment_to_editGroupFragment)
        }
        binding.tvName.setOnClickListener {
            findNavController().navigate(R.id.action_chatGroupFragment_to_showGroupInfoFragment)
        }

        binding.imageSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                if (binding.lyTextEditing.visibility == View.VISIBLE) {
                    editMessage(editingMessages)
                    BuildPopupMenuForGroups(requireActivity(),
                        binding,
                        groups.uid).showSentMode()
                    binding.edtMessage.requestFocus()
                } else {
                    sentMessage(message)
                    BuildPopupMenuForGroups(requireActivity(),
                        binding,
                        groups.uid).showSentMode()
                }
            } else {
                if (binding.lyTextEditing.visibility == View.VISIBLE) {
                    Toast.makeText(requireActivity(),
                        "In this case, you cannot change it",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireActivity(), "No message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun editMessage(editingMessages: GroupMessages) {
        val groupMessages = GroupMessages(
            editingMessages.uid,
            binding.edtMessage.text.toString().trim(),
            editingMessages.time,
            editingMessages.userUid,
            editingMessages.userFirstname,
            editingMessages.userLastname,
            editingMessages.userImageId,
            editingMessages.userImageUrl
        )
        realReferenceChats.child(groupMessages.uid).setValue(groupMessages)
    }

    private fun sentMessage(message: String) {
        val currentTime = Calendar.getInstance().time
        val groupMessages = GroupMessages(realReference.push().key!!,
            message,
            currentTime.time.toString(),
            profile.uid,
            profile.firstName,
            profile.lastName,
            profile.imageId,
            profile.imageUrl)
        realReferenceChats.child(groupMessages.uid).setValue(groupMessages)
        binding.edtMessage.text.clear()
    }

    private fun getChats() {
        realReferenceChats.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listGroupMessages = ArrayList<GroupMessages>()
                for (child in snapshot.children) {
                    val groupMessages = child.getValue(GroupMessages::class.java)
                    if (groupMessages != null) {
                        listGroupMessages.add(groupMessages)
                    }
                }
                binding.rvChats.adapter = RVGroupsChatAdapter(listGroupMessages,
                    object : RVGroupsChatAdapter.RVClickGroupsChats {
                        override fun click(
                            groupMessages: GroupMessages,
                            view: View,
                            position: Int,
                        ) {
                            editingPosition = position
                            editingMessages = groupMessages
                            BuildPopupMenuForGroups(requireActivity(),
                                binding,
                                groups.uid).forChats(groupMessages, view)
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

    private fun getGroup() {
        realReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isVisible) {
                    val group = snapshot.getValue(Groups::class.java)
                    if (group != null) {
                        showActivity(group)
                        getChats()
                    } else {
                        if (isVisible) {
                            findNavController().popBackStack()
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showActivity(group: Groups) {
        binding.edtMessage.requestFocus()
        binding.tvName.text = group.name
        if (group.imageID.isNotEmpty()) {
            Glide.with(binding.root).load(group.imageUrl).into(binding.imageGroup)
        }
        if (group.admin != profile.uid) {
            binding.imageEdit.visibility = View.GONE
        }
    }
}