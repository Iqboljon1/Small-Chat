package com.iraimjanov.smallchat.data

import com.google.firebase.auth.PhoneAuthProvider
import com.iraimjanov.smallchat.cc.CountryDB
import com.iraimjanov.smallchat.models.Groups
import com.iraimjanov.smallchat.models.PersonalUsers
import com.iraimjanov.smallchat.models.Users

object PublicData {
    var countryDB = CountryDB("Uzbekistan" , "+998")
    var sentCode = ""
    var number = ""
    var profile = Users()
    lateinit var storedVerificationId :String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var chatProfile: PersonalUsers
    lateinit var groups: Groups
}