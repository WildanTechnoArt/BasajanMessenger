package com.wildan.basajanmessenger.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_edit_kontak.*

import com.wildan.basajanmessenger.R

class EditKontak : AppCompatActivity() {

    //Deklarasi Variable
    private var mAuth: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_kontak)
        setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance().currentUser

        //Menampilkan tombol back pada Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Menampilkan data kontak yang ingin di Edit
        getKontak()

        //Menyimpan kontak yang baru di ubah pada database lokal (SQLite Database)
        save.setOnClickListener {
            //Mendapatkan Instance dari Firebase RTD
            val refUser = FirebaseDatabase.getInstance().reference.child("Users")
            val setName = name!!.text.toString()
            val setNomor = no_id!!.text.toString() + phone_number!!.text.toString()

            //Mengecek apakah nama(huruf) yang dimasukan tidak lebih dari 12
            if(setName.length > 12){
                Toast.makeText(this@EditKontak, "Nama Terlalu Panjang", Toast.LENGTH_SHORT).show()
            }else{
                //Mengecek apakah data kosong atau tidak
                if (TextUtils.isEmpty(setName) || TextUtils.isEmpty(setNomor)) {
                    Toast.makeText(applicationContext, "Masukan Nama dan Nomor Telepon", Toast.LENGTH_SHORT).show()
                }else{
                    progressBar2.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, "Memeriksa Nomor Kontak", Toast.LENGTH_SHORT).show()
                    //Mendapatkan referensi dari database, menuju lokasi(Database) profil teman/kontak yang akan ditambahkan
                    refUser.child(setNomor).child("Profil").child("userID").addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            Log.e("AddFriend", "Error: ", p0.toException())
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            //Mengecek apakah user(nomor) sudah terdaftar pada aplikasi dan mempunyai UID atau tidak
                            if(p0.exists()){
                                val dataKontak = HashMap<String, String>()
                                dataKontak["searchIndex"] = setName.toLowerCase()
                                dataKontak["namaUser"] = setName
                                dataKontak["nomorUser"] = setNomor
                                updateData(dataKontak)
                            }else{
                                progressBar2.visibility = View.GONE
                                Toast.makeText(this@EditKontak, "Nomor Kontak Belum Terdaftar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            }
        }
    }

    private fun updateData(data: HashMap<String, String>){
        val getKey = intent.extras?.getString("primaryKey")
        val refKontak = FirebaseDatabase.getInstance().reference.child("Users")
        refKontak.child(mAuth?.phoneNumber.toString())
                 .child("Kontak")
                 .child(getKey.toString())
                 .setValue(data)
                 .addOnSuccessListener({
                     progressBar2.visibility = View.GONE
                     name.setText("")
                     phone_number.setText("")
                     Toast.makeText(this@EditKontak, "Kontak Berhasil Diubah", Toast.LENGTH_SHORT).show()
                     finish()
                })
                .addOnFailureListener {
                    progressBar2.visibility = View.GONE
                    Toast.makeText(this@EditKontak, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    //Mendapatkan data kontak yang akan di Edit oleh user
    private fun getKontak() {
        name!!.setText(intent.extras!!.getString("Nama"))
        phone_number!!.setText(intent.extras!!.getString("Nomor"))
    }
}
