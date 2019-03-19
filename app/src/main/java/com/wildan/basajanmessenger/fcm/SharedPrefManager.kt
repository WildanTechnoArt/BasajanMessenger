package com.wildan.basajanmessenger.fcm

import android.annotation.SuppressLint
import android.content.Context

//Digunakan untuk mengatur penyimpanan data pada SharedPreference
class SharedPrefManager private constructor(context: Context) {

    init {
        mContext = context
    }

    companion object {

        //Nama File untuk SharedPreferenxe
        private const val SHARED_PREF_NAME = "FCMTokenMessenger"
        private const val SHARED_DATA_PROFIL = "Profil User"

        //Key untuk mengambil Value pada SharedPreference
        private const val TOKEN_KEY_ACCESS = "token"
        private const val NAME_KEY_ACCESS = "nama_user"
        private const val NOMOR_KEY_ACCESS = "nomor_telepon"
        private const val STATUS_KEY_ACCESS = "status_user"

        @SuppressLint("StaticFieldLeak")
        private lateinit var mContext: Context
        @SuppressLint("StaticFieldLeak")
        private var mInstance: SharedPrefManager? = null

        @Synchronized
        fun getInstance(context: Context): SharedPrefManager {
            if (mInstance == null)
                mInstance = SharedPrefManager(context)
            return mInstance!!
        }
    }

    //Mendapatkan Token yang tersimpan didalam SharedPreference
    val token: String?
        get() {
            val preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return preferences.getString(TOKEN_KEY_ACCESS, null)
        }

    //Mendapatkan Data User yang tersimpan didalam SharedPreference
    val userName: String?
        get() {
            val preferences = mContext.getSharedPreferences(SHARED_DATA_PROFIL, Context.MODE_PRIVATE)
            return preferences.getString(NAME_KEY_ACCESS, null)
        }
    val userNomor: String?
        get() {
            val preferences = mContext.getSharedPreferences(SHARED_DATA_PROFIL, Context.MODE_PRIVATE)
            return preferences.getString(NOMOR_KEY_ACCESS, null)
        }

    val userStatus: String?
        get() {
            val preferences = mContext.getSharedPreferences(SHARED_DATA_PROFIL, Context.MODE_PRIVATE)
            return preferences.getString(STATUS_KEY_ACCESS, null)
        }

    //Method untuk meyimpan Token pada SharedPreference
    fun storeToken(token: String): Boolean {
        val preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(TOKEN_KEY_ACCESS, token)
        editor.apply()
        return true
    }

    //Method untuk meyimpan Nama pada SharedPreference
    fun storeDataNama(nama: String): Boolean {
        val preferences = mContext.getSharedPreferences(SHARED_DATA_PROFIL, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(NAME_KEY_ACCESS, nama)
        editor.apply()
        return true
    }

    //Method untuk meyimpan Status pada SharedPreference
    fun storeDataStatus(status: String): Boolean {
        val preferences = mContext.getSharedPreferences(SHARED_DATA_PROFIL, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(STATUS_KEY_ACCESS, status)
        editor.apply()
        return true
    }

    //Method untuk meyimpan Nama dan Nomor Telepon pada SharedPreference
    fun storeDataProfil(nama: String, nomor: String, status: String): Boolean {
        val preferences = mContext.getSharedPreferences(SHARED_DATA_PROFIL, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(NAME_KEY_ACCESS, nama)
        editor.putString(NOMOR_KEY_ACCESS, nomor)
        editor.putString(STATUS_KEY_ACCESS, status)
        editor.apply()
        return true
    }
}
