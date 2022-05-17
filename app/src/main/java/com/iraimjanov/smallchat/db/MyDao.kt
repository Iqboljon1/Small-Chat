package com.iraimjanov.smallchat.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.iraimjanov.smallchat.cc.CountryDB

@Dao
interface MyDao {

    @Insert
    fun addCountryCode(countryDB: CountryDB)

    @Query("select * from CountryDB")
    fun getAllCountryCode(): List<CountryDB>

    @Query("select * from CountryDB where codes like :codes ")
    fun searchOneCountryCode(codes: String): List<CountryDB>

    @Query("select * from CountryDB where names like :names || '%'")
    fun searchCountryCode(names: String): List<CountryDB>

}