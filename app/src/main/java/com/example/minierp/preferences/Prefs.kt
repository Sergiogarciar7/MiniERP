package com.example.minierp.preferences

import android.content.Context

class Prefs(c: Context){
    val store= c.getSharedPreferences("LOGIN",0)
    public fun guardarEmail(email:String){
        store.edit().putString("Email",email).apply()
    }
    public fun leerEmail(): String? {
        return store.getString("Email", null)
    }
    public fun borrarTodo(){
        store.edit().clear().apply()
    }
}