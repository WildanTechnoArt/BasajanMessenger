package com.wildan.basajanmessenger.ui

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sunting_data.*
import com.wildan.basajanmessenger.R
import com.wildan.basajanmessenger.fcm.SharedPrefManager

class SuntingData : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sunting_data)
        mAuth = FirebaseAuth.getInstance()

        //Menampilkan tombol back pada Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sunting.setOnClickListener {
            val namaBaru = newName.text.toString()
            val statusBaru = newStatus.text.toString()
            if (TextUtils.isEmpty(namaBaru) || TextUtils.isEmpty(statusBaru)) {
                Toast.makeText(applicationContext, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                SharedPrefManager.getInstance(this@SuntingData).storeDataNama(namaBaru)
                SharedPrefManager.getInstance(this@SuntingData).storeDataStatus(statusBaru)
                updateNama()
                Toast.makeText(this@SuntingData, "Data telah diubah", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateNama(){
        val reference1 = FirebaseDatabase.getInstance().reference
        val reference2 = FirebaseDatabase.getInstance().reference
        val getUser = mAuth!!.currentUser?.uid
        reference1.child("Users").child(getUser.toString()).child("namaUser").setValue(newName.text.toString())
        reference2.child("Users").child(getUser.toString()).child("statusUser").setValue(newStatus.text.toString())
    }

    @SuppressLint("Recycle")
    override fun onStart() {
        super.onStart()
        val nama = SharedPrefManager.getInstance(this).userName
        val status = SharedPrefManager.getInstance(this).userStatus
        newName!!.setText(nama)
        newStatus!!.setText(status)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
