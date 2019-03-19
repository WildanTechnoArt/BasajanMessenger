package com.wildan.basajanmessenger

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

class BasajanMessenger : MultiDexApplication(){

    private var auth: FirebaseAuth? = null
    private var mUsersDatabase: DatabaseReference? = null

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        /* Picasso*/

        val builder: Picasso.Builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this))
        val build: Picasso = builder.build()
        build.setIndicatorsEnabled(true)
        build.isLoggingEnabled = true
        Picasso.setSingletonInstance(build)

        /* Friends */

        auth = FirebaseAuth.getInstance()
        try{
            mUsersDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(auth?.currentUser?.uid!!)

            mUsersDatabase?.addValueEventListener(object : ValueEventListener{

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    mUsersDatabase?.child("online")?.onDisconnect()?.setValue(ServerValue.TIMESTAMP)

                }

            })
        }catch (ex: NullPointerException){
            ex.printStackTrace()
        }
    }
}