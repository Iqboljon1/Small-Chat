package com.iraimjanov.smallchat.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.models.Users

class UpdateUser {
    private var auth = Firebase.auth
    private var reference = Firebase.database.getReference("users").child(auth.currentUser!!.uid)
    fun updateUser() {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                profile = snapshot.getValue(Users::class.java)!!
                Log.d(TAG , "updateUser")
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }
}