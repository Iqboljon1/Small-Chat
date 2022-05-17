package com.iraimjanov.smallchat.models

data class Groups(
    var uid: String = "",
    var name: String = "",
    var imageID: String = "",
    var imageUrl: String = "",
    var admin: String = "",
    var listPersonalUsers: ArrayList<PersonalUsers> = ArrayList(),
)