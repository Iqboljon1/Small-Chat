package com.iraimjanov.smallchat.models

data class Users(
    var uid: String = "",
    var number: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var imageId: String = "",
    var imageUrl: String = "",
    var personalUsers: ArrayList<PersonalUsers> = ArrayList(),
    var groups: ArrayList<Groups> = ArrayList(),
)