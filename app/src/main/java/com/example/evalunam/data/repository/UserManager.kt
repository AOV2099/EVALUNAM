package com.example.evalunam.data.repository

import android.content.Context
import com.example.evalunam.data.model.UserData

class UserManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)

    fun saveUserData(userData : UserData) {
        val editor = sharedPreferences.edit()
        editor.putString("usrLogi", userData.usrLogi)
        editor.putString("usrPass", userData.usrPass)
        editor.putString("career", userData.career)
        editor.putString("name", userData.name)
        editor.putString("curp", userData.curp)
        editor.putString("imageB64", userData.imageB64)
        editor.apply()
    }

    fun getUserData() : UserData {
        val usrLogi = sharedPreferences.getString("usrLogi", "")
        val usrPass = sharedPreferences.getString("usrPass", "")
        val career = sharedPreferences.getString("career", "")
        val name = sharedPreferences.getString("name", "")
        val curp = sharedPreferences.getString("curp", "")
        val imageB64 = sharedPreferences.getString("imageB64", "")
        return UserData(usrLogi!!, usrPass!!, career!!, name!!, curp!!, imageB64!!)
    }
}