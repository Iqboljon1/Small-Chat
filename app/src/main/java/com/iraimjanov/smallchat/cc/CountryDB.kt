package com.iraimjanov.smallchat.cc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class CountryDB {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var names: String? = null
    var codes: String? = null

    constructor(id: Int?, names: String?, codes: String?) {
        this.id = id
        this.names = names
        this.codes = codes
    }

    constructor(names: String?, codes: String?) {
        this.names = names
        this.codes = codes
    }

    constructor()

}