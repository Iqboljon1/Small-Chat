package com.iraimjanov.smallchat.data

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.iraimjanov.smallchat.R
import com.iraimjanov.smallchat.databinding.FragmentHomeBinding

class BuildFAB(
    val requireActivity: FragmentActivity,
    val binding: FragmentHomeBinding,
    val findNavController: NavController,
) {
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity,
            R.anim.rotate_open_anim)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity,
            R.anim.rotate_close_anim)
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity,
            R.anim.from_bottom_anim)
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity,
            R.anim.to_bottom_anim)
    }

    private var clicked = false

    fun build() {
        binding.floatingActionButtonAdd.setOnClickListener {
            addOnButtonClicked()
        }
        binding.floatingActionButtonAddContact.setOnClickListener {
            findNavController.navigate(R.id.action_homeFragment_to_addContactFragment)
        }
        binding.floatingActionButtonAddGroup.setOnClickListener {
            findNavController.navigate(R.id.action_homeFragment_to_addGroupFragment)
        }
    }

    private fun addOnButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.floatingActionButtonAddContact.visibility = View.VISIBLE
            binding.floatingActionButtonAddGroup.visibility = View.VISIBLE
        } else {
            binding.floatingActionButtonAddContact.visibility = View.INVISIBLE
            binding.floatingActionButtonAddGroup.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.floatingActionButtonAddContact.startAnimation(fromBottom)
            binding.floatingActionButtonAddGroup.startAnimation(fromBottom)
            binding.floatingActionButtonAdd.startAnimation(rotateOpen)
        } else {
            binding.floatingActionButtonAddContact.startAnimation(toBottom)
            binding.floatingActionButtonAddGroup.startAnimation(toBottom)
            binding.floatingActionButtonAdd.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.floatingActionButtonAddContact.isClickable = true
            binding.floatingActionButtonAddGroup.isClickable = true
        } else {
            binding.floatingActionButtonAddContact.isClickable = false
            binding.floatingActionButtonAddGroup.isClickable = false
        }
    }

}