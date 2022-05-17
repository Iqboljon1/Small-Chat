package com.iraimjanov.smallchat.models

data class GroupMessages(
    var uid: String = "",
    var messages: String = "",
    var time: String = "",
    var userUid: String = "",
    var userFirstname: String = "",
    var userLastname: String = "",
    var userImageId: String = "",
    var userImageUrl: String = "",
)