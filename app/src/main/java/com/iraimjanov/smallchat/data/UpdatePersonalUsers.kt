package com.iraimjanov.smallchat.data

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.smallchat.models.*


class UpdatePersonalUsers {
    private val realReference = Firebase.database.getReference("users")
    private val realReferenceChats = Firebase.database.getReference("chats")
    private val realReferenceGroups = Firebase.database.getReference("groups")
    private val realReferenceGroupsChats = Firebase.database.getReference("groupsChats")
    fun updatePersonalUsers(user: Users) {
        realReference.get().addOnSuccessListener { it ->
            for (child in it.children) {
                val users = child.getValue(Users::class.java)
                if (users != null && users.uid != user.uid) {
                    val testListPersonalUsers = ArrayList<PersonalUsers>()
                    for (personalUsers in users.personalUsers) {
                        if (personalUsers.uid == user.uid) {
                            val testPersonalUsers = PersonalUsers(user.uid,
                                user.number,
                                user.firstName,
                                user.lastName,
                                user.imageId,
                                user.imageUrl)
                            testListPersonalUsers.add(testPersonalUsers)
                        } else {
                            testListPersonalUsers.add(personalUsers)
                        }
                    }
                    val updateUser = Users(users.uid,
                        users.number,
                        users.firstName,
                        users.lastName,
                        users.imageId,
                        users.imageUrl,
                        testListPersonalUsers,
                        users.groups)
                    realReference.child(updateUser.uid).setValue(updateUser).addOnSuccessListener {
                        Log.d(TAG, "addOnSuccessListener Update Personal Users")
                    }.addOnFailureListener {
                        Log.d(TAG, "addOnFailureListener \n${it}")
                    }
                }
            }
        }
    }

    fun deletePersonaUsers(fromUid: String, toUid: String) {
        realReference.get().addOnSuccessListener {
            for (child in it.children) {
                val users = child.getValue(Users::class.java)
                if (users != null) {
                    if (users.uid == fromUid) {
                        val testListPersonalUsers = ArrayList<PersonalUsers>()
                        for (user in users.personalUsers) {
                            if (user.uid != toUid) {
                                testListPersonalUsers.add(user)
                            }
                        }
                        val user = Users(users.uid,
                            users.number,
                            users.firstName,
                            users.lastName,
                            users.imageId,
                            users.imageUrl,
                            testListPersonalUsers,
                            users.groups)
                        realReference.child(user.uid).setValue(user)
                    }
                    if (users.uid == toUid) {
                        val testListPersonalUsers = ArrayList<PersonalUsers>()
                        for (user in users.personalUsers) {
                            if (user.uid != fromUid) {
                                testListPersonalUsers.add(user)
                            }
                        }
                        val user = Users(users.uid,
                            users.number,
                            users.firstName,
                            users.lastName,
                            users.imageId,
                            users.imageUrl,
                            testListPersonalUsers,
                            users.groups)
                        realReference.child(user.uid).setValue(user)
                    }
                }
            }
        }
    }

    fun addPersonalUser(user: Users, toUser: Users) {
        val personalUsers = PersonalUsers(user.uid,
            user.number,
            user.firstName,
            user.lastName,
            user.imageId,
            user.imageUrl)
        toUser.personalUsers.add(personalUsers)
        realReference.child(toUser.uid).setValue(toUser)
    }

    fun closeTheConversationBetweenUsers(fromUid: String, toUid: String) {
        realReferenceChats.get().addOnSuccessListener {
            for (child in it.children) {
                val messages = child.getValue(Messages::class.java)
                if (messages != null && messages.from == fromUid && messages.to == toUid || messages!!.from == toUid && messages.to == fromUid) {
                    realReferenceChats.child(messages.uid).removeValue()
                }
            }
        }
    }

    fun updatePersonalUsersInGroup(users: Users) {
        realReferenceGroups.get().addOnSuccessListener {
            for (child in it.children) {
                val groups = child.getValue(Groups::class.java)
                if (groups != null) {
                    var boolean = false
                    val testListUsers = ArrayList<PersonalUsers>()
                    for (listPersonalUser in groups.listPersonalUsers) {
                        if (listPersonalUser.uid == users.uid) {
                            val personalUsers = PersonalUsers(users.uid,
                                users.number,
                                users.firstName,
                                users.lastName,
                                users.imageId,
                                users.imageUrl)
                            testListUsers.add(personalUsers)
                            boolean = true
                        } else {
                            testListUsers.add(listPersonalUser)
                        }
                        if (boolean) {
                            val group = Groups(groups.uid,
                                groups.name,
                                groups.imageID,
                                groups.imageUrl,
                                groups.admin,
                                testListUsers)
                            realReferenceGroups.child(group.uid).setValue(group)
                        }
                    }
                }
            }
        }
    }

    fun deleteGroupInUsers(groupsUid: String) {
        realReference.get().addOnSuccessListener {
            for (child in it.children) {
                val user = child.getValue(Users::class.java)
                if (user != null) {
                    val listGroups = user.groups
                    val testListGroups = ArrayList<Groups>()
                    for (group in listGroups) {
                        if (group.uid != groupsUid) {
                            testListGroups.add(group)
                        }
                    }
                    user.groups = testListGroups
                    realReference.child(user.uid).setValue(user)
                }
            }
        }
    }

    fun logoutTheGroup(users: Users, groupUid: String?) {
        realReference.child(users.uid).get().addOnSuccessListener {
            val user = it.getValue(Users::class.java)
            if (user != null) {
                val testListGroups = ArrayList<Groups>()
                for (group in user.groups) {
                    if (group.uid != groupUid) {
                        testListGroups.add(group)
                    }
                }
                user.groups = testListGroups
                realReference.child(user.uid).setValue(user)
            }
        }
    }

    fun addGroupToUser(groups: Groups, users: Users) {
        realReference.get().addOnSuccessListener {
            for (child in it.children) {
                val user = child.getValue(Users::class.java)
                if (user != null && user.uid == users.uid) {
                    val listGroups = user.groups
                    listGroups.add(groups)
                    val editedUser = Users(user.uid,
                        user.number,
                        user.firstName,
                        user.lastName,
                        user.imageId,
                        user.imageUrl,
                        user.personalUsers,
                        listGroups)
                    realReference.child(editedUser.uid).setValue(editedUser)
                }
            }
        }
    }

    fun deleteUserFromGroup(groupUid: String, userUid: String) {
        realReferenceGroups.child(groupUid).get().addOnSuccessListener {
            val group = it.getValue(Groups::class.java)
            val testListPersonalUsers = ArrayList<PersonalUsers>()
            if (group != null) {
                for (users in group.listPersonalUsers) {
                    if (users.uid != userUid) {
                        testListPersonalUsers.add(users)
                    }
                }
                group.listPersonalUsers = testListPersonalUsers
                realReferenceGroups.child(group.uid).setValue(group)
            }
        }
    }

    fun addPersonalUserToGroup(groups: Groups, user: Users) {
        val hashset = HashSet<String>()
        for (users in groups.listPersonalUsers) {
            hashset.add(users.uid)
        }
        if (hashset.add(user.uid)) {
            val personalUser = PersonalUsers(user.uid,
                user.number,
                user.firstName,
                user.lastName,
                user.imageId,
                user.imageUrl)
            groups.listPersonalUsers.add(personalUser)
            realReferenceGroups.child(groups.uid).setValue(groups)
        }
    }

    fun updateGroupsInUsers(newGroup: Groups) {
        realReference.get().addOnSuccessListener {
            for (child in it.children) {
                val user = child.getValue(Users::class.java)
                if (user != null) {
                    val listGroups = ArrayList<Groups>()
                    var boolean = false
                    for (group in user.groups) {
                        if (group.uid == newGroup.uid) {
                            listGroups.add(newGroup)
                            boolean = true
                        } else {
                            listGroups.add(group)
                        }
                    }
                    if (boolean) {
                        user.groups = listGroups
                        realReference.child(user.uid).setValue(user)
                    }
                }
            }
        }
    }

    fun updateChatsInGroup(user: Users) {
        realReferenceGroups.get().addOnSuccessListener { it ->
            for (child in it.children) {
                val group = child.getValue(Groups::class.java)
                if (group != null) {
                    realReferenceGroupsChats.child(group.uid).get().addOnSuccessListener {
                        for (message in it.children) {
                            val groupMessages = message.getValue(GroupMessages::class.java)
                            if (groupMessages != null) {
                                if (groupMessages.userUid == user.uid) {
                                    groupMessages.userFirstname = user.firstName
                                    groupMessages.userLastname = user.lastName
                                    groupMessages.userImageId = user.imageId
                                    groupMessages.userImageUrl = user.imageUrl
                                    realReferenceGroupsChats.child(group.uid)
                                        .child(groupMessages.uid).setValue(groupMessages)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}