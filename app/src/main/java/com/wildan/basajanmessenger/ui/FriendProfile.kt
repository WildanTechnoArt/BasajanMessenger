package com.wildan.basajanmessenger.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.wildan.basajanmessenger.R
import kotlinx.android.synthetic.main.activity_friend_profile.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.HashMap

class FriendProfile : AppCompatActivity() {

    //Deklarasi Variable
    private var getNama: String? = null
    private var getNomor: String? = null
    private var getStatus: String? = null
    private var getImage: String? = null
    private var getSearchIndex: String? = null
    private var friendReference: DatabaseReference? = null
    private var profileReference: DatabaseReference? = null
    private var friendDatabase: DatabaseReference? = null
    private var notificationReference: DatabaseReference? = null
    private var roofRef: DatabaseReference? = null
    private var user: FirebaseUser? = null
    private var currentState: String? = null
    private var friendUID: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        user = FirebaseAuth.getInstance().currentUser

        //Mendapatkan Kode Unik (User ID) dari Kontak Teman
        friendUID = intent.extras.getString("user_id")

        //Mendapatkan Referensi Database untuk permintaan pertemenan
        friendReference = FirebaseDatabase.getInstance().reference.child("FriendRequest")

        //Mendapatkan Referensi Database dari kontak taman
        profileReference = FirebaseDatabase.getInstance().reference.child("Users").child(friendUID!!)

        //Mendapatkan Referensi Database untuk menyimpan pertemanan antar user
        friendDatabase = FirebaseDatabase.getInstance().reference.child("Friends")

        //Mendapatkan Referensi Database untuk notifikasi antar user
        notificationReference = FirebaseDatabase.getInstance().reference.child("Notification")

        roofRef = FirebaseDatabase.getInstance().reference

        //Kondisi Default sebelum mengirimkan permintaan pertemanan
        currentState = "not_friends"

        progressBar.visibility = View.VISIBLE

        //Menerapkan Listener untuk Referensi Database pada Profi teman
        profileReference?.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {

                //Mendapatkan data-data dari kontak yang di akses oleh user
                getNama = p0.child("namaUser").value.toString()
                getNomor = p0.child("nomorUser").value.toString()
                getStatus = p0.child("statusUser").value.toString()
                getImage = p0.child("thumbImage").value.toString()
                getSearchIndex = p0.child("searchIndex").value.toString()

                //Menampilkan data tersebut
                friend_name.text = getNama
                friend_nomor.text = getNomor
                status.text = "\"$getStatus\""

                //Menampilkan foto profil dari user (Teman)
                Picasso.get().load(getImage).placeholder(R.drawable.circle_profil).into(friend_photo)

                //Mengakses Referensi data user dan menambahkan Listener
                friendReference?.child(user?.uid!!)?.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {

                        //Mengecek Konsisi permintaan pertemanan antar user
                        if(p0.hasChild(friendUID!!)){
                            //Kondisi User sebelum menjadi teman
                            val getRequest = p0.child(friendUID!!).child("request_type").value.toString()
                            if(getRequest == "received"){
                                currentState = "req_received"
                                add.text = "Terima"
                                decline.visibility = View.VISIBLE
                            }
                            else if(getRequest == "sent"){
                                currentState = "req_sent"
                                add.text = "Batalkan Permintaan"
                                decline.visibility = View.GONE
                            }

                            progressBar.visibility = View.GONE

                        }else{

                            //Kondisi User setelah menjadi teman
                            friendDatabase?.child(user?.uid!!)?.addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {

                                    if(p0.hasChild(friendUID!!)){

                                        currentState = "friend"
                                        add.text = "Hapus Pertemanan"
                                        decline.visibility = View.GONE
                                    }

                                    progressBar.visibility = View.GONE
                                }

                            })
                        }
                    }
                })

                friend_photo.visibility = View.VISIBLE
                friend_name.visibility = View.VISIBLE
                friend_nomor.visibility = View.VISIBLE
                status.visibility = View.VISIBLE
                add.visibility = View.VISIBLE
            }
        })

        add.setOnClickListener {

            add.isEnabled = false

            //============= MEMINTA PERTEMANAN (NOT FRIEND STATE) =============//
            if(currentState.equals("not_friends")){

                progressBar.visibility = View.VISIBLE

                val newNotificationRes = roofRef?.child("Notification")?.child(friendUID!!)?.push()
                val newNotifiactionId = newNotificationRes?.key

                //Mengirim Notifikasi pada user yang akan ditambahkan
                val sendNotification = HashMap<String, String>()
                sendNotification["From"] = user?.uid!!
                sendNotification["Type"] = "Request"

                val requestMap = mutableMapOf<String, Any>()
                requestMap["FriendRequest/" + user?.uid!! + "/" + friendUID + "/request_type"] = "sent"
                requestMap["FriendRequest/" + friendUID + "/" + user?.uid + "/request_type"] = "received"
                requestMap["Notification/$friendUID/$newNotifiactionId"] = sendNotification

                friendReference?.updateChildren(requestMap) { p0, _ ->

                    if(p0 != null){
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@FriendProfile, "Gagal Mengirim Permintaan", Toast.LENGTH_SHORT).show()
                    }

                    add.isEnabled = true
                    currentState = "req_sent"
                    add.text = "Batalkan Permintaan"
                }

            }

            //============= BATALKAN PERTEMANAN =============//
            if(currentState.equals("req_sent")){

                add.isEnabled = false

                progressBar.visibility = View.VISIBLE

                friendReference?.child(user?.uid!!)?.child(friendUID!!)?.removeValue()
                        ?.addOnCompleteListener {
                            if(it.isSuccessful){
                                friendReference?.child(friendUID!!)?.child(user?.uid!!)?.removeValue()
                                        ?.addOnSuccessListener {
                                            add.isEnabled = true
                                            currentState = "not_friends"
                                            add.text = "Tambahkan Teman"
                                            decline.visibility = View.GONE
                                            progressBar.visibility = View.GONE
                                            Toast.makeText(this@FriendProfile, "Permintaan Dibatalkan", Toast.LENGTH_SHORT).show()
                                        }
                            }else{
                                progressBar.visibility = View.GONE
                                Toast.makeText(this@FriendProfile, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                            }
                        }
            }

            //============= MENERIMA PERTEMANAN =============//
            if(currentState.equals("req_received")){

                val currentDate = DateFormat.getDateTimeInstance().format(Date())

                val friendRequest = mutableMapOf<String, Any>()

                friendRequest["Friends/" + user?.uid!! + "/" + friendUID + "/date"] = currentDate
                friendRequest["Friends/" + user?.uid!! + "/" + friendUID + "/searchIndex"] = getSearchIndex!!
                friendRequest["Friends/" + friendUID + "/" + user?.uid + "/date"] = currentDate

                friendRequest["FriendRequest/" + user?.uid!! + "/" + friendUID]
                friendRequest["FriendRequest/" + friendUID + "/" + user?.uid]

                friendReference?.updateChildren(friendRequest) { p0, _ ->

                    if(p0 == null){
                        progressBar.visibility = View.GONE
                        currentState = "friend"
                        add.isEnabled = true
                        add.text = "Hapus Pertemanan"
                        decline.visibility = View.GONE
                    }else{
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@FriendProfile, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //============= HAPUS PERTEMANAN =============//
            if(currentState.equals("friend")){

                val unfriend = mutableMapOf<String, Any>()

                unfriend["Friends/" + user?.uid!! + "/" + friendUID]
                unfriend["Friends/" + friendUID + "/" + user?.uid]

                friendReference?.updateChildren(unfriend) { p0, _ ->

                    if(p0 == null){
                        progressBar.visibility = View.GONE
                        currentState = "not_friends"
                        add.isEnabled = true
                        add.text = "Tambah Teman"
                        decline.visibility = View.GONE
                    }else{
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@FriendProfile, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                    }

                    add.isEnabled = true
                }
            }
        }
    }
}