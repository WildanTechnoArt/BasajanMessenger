package com.wildan.basajanmessenger.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wildan.basajanmessenger.GetTimeAgo

import com.wildan.basajanmessenger.R
import com.wildan.basajanmessenger.adapter.MessageAdapter
import com.wildan.basajanmessenger.models.MessageModel
import kotlinx.android.synthetic.main.activity_chat_frame.*

@Suppress("NAME_SHADOWING")
class ChatFrame : AppCompatActivity() {

    private var mRootRef: DatabaseReference? = null
    private var mUserRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var stateListener: FirebaseAuth.AuthStateListener? = null
    private var firebaseListAdapter: FirebaseListAdapter<MessageModel>? = null
    private var userChat: DatabaseReference? = null

    private var loggedID = ""
    private var getNama: String? = null
    private var getStatus: String? = null
    private var mChatUser: String? = null
    private var mCurrentUserId: String? = null
    private var getImage: String? = null
    private var getSearchIndex: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_frame)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mChatUser = intent.getStringExtra("user_id")

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserId = mAuth?.currentUser?.uid

        stateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                displayChatMessages()
            } else {
                onSignedOutCleanup()
            }
        }
        mUserRef = FirebaseDatabase.getInstance().reference.child("Users")
                .child(mCurrentUserId!!)
        mUserRef?.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                getImage = p0.child("thumbImage").value.toString()

                if(getImage != "default"){
                    Picasso.get().load(getImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.circle_profil)
                            .into(photo, object : Callback{
                                override fun onSuccess() {

                                }

                                override fun onError(e: Exception?) {
                                    Picasso.get().load(getImage).placeholder(R.drawable.circle_profil).into(photo)
                                }

                            })
                }

            }
        })

        mRootRef = FirebaseDatabase.getInstance().reference.child("Users").child(mChatUser!!)
        mRootRef?.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {

                getSearchIndex = p0.child("searchIndex").value.toString()
            }
        })

        //Jika user belum menginputkan Pesan, maka tombol Kirim tidak dapat digunakan
        send_chat!!.isEnabled = false
        chat_input!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty()){
                    send_chat.isEnabled = true
                    send_chat.setBackgroundResource(R.drawable.b_selector)
                }else{
                    send_chat.isEnabled = false
                    send_chat.setBackgroundResource(R.drawable.bg_chat_button)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        //Membatasi Jumlah Text pada Pesan
        chat_input?.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1000))

        send_chat?.setOnClickListener {
            sendMessage()
        }

        photo?.setOnClickListener {
            val intent = Intent(this@ChatFrame, FriendProfile::class.java)
            intent.putExtra("user_id", mChatUser)
            startActivity(intent)
            finish()
        }

        getFriendIdent()

        mRootRef?.child("Chat")?.child(mCurrentUserId!!)?.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {
                Log.e("ChatFrame.kt", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {

                val chatAddMap = mutableMapOf<String, Any>()
                chatAddMap["seen"] = false
                chatAddMap["timestamp"] = ServerValue.TIMESTAMP
                chatAddMap["searchIndex"] = getSearchIndex!!

                val chatAddMap2 = mutableMapOf<String, Any>()
                chatAddMap["seen"] = false
                chatAddMap["timestamp"] = ServerValue.TIMESTAMP

                val chatUserMap = mutableMapOf<String, Any>()
                chatUserMap["Chat/$mCurrentUserId/$mChatUser"] = chatAddMap
                chatUserMap["Chat/$mChatUser/$mCurrentUserId"] = chatAddMap2

                mRootRef?.updateChildren(chatUserMap) { p0, _ ->

                    if(p0 != null){
                        Log.d("CHAT_LOG", p0.message)
                    }
                }
            }
        })
    }

    //Digunakan untuk menampilkan chat pada layar dan mendapatkan referensi dari View
    private fun displayChatMessages() {
        loggedID = FirebaseAuth.getInstance().currentUser!!.uid

        //Referensi Untuk Melihat Chat yang dikirimpan Teman kepada User
        userChat = FirebaseDatabase.getInstance().reference.child("Messages")
                .child(mCurrentUserId!!)
                .child(mChatUser!!)

        val options = FirebaseListOptions.Builder<MessageModel>()
                .setLayout(R.layout.layout_msg_friend)
                .setQuery(this.userChat!!, MessageModel::class.java)
                .setLifecycleOwner(this)
                .build()

        firebaseListAdapter = MessageAdapter(this, options)

        list_chat.adapter = firebaseListAdapter
    }

    //Digunakan untuk mengambil userID, untuk menentukan Layout dari ChatList pada MessageAdapter
    fun getloggedID(): String {
        return loggedID
    }

    private fun onSignedOutCleanup() {
        firebaseListAdapter!!.stopListening()
    }

    override fun onPause() {
        super.onPause()
        if (stateListener != null) {
            mAuth!!.removeAuthStateListener(stateListener!!)
        }
        firebaseListAdapter!!.stopListening()
    }

    override fun onResume() {
        super.onResume()
        mAuth!!.addAuthStateListener(stateListener!!)
    }

    //Digunakan untuk menentukan aksi pada tombol home
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun sendMessage(){

        val message = chat_input.text.toString()
        if(!TextUtils.isEmpty(message)){

            val current_user_ref = "Messages/$mCurrentUserId/$mChatUser"
            val chat_user_ref = "Messages/$mChatUser/$mCurrentUserId"

            val user_message_push: DatabaseReference = mRootRef?.child("Messages")?.child(mCurrentUserId!!)
                    ?.child(mChatUser!!)?.push()!!

            val pust_id = user_message_push.key

            val messageMap = mutableMapOf<String, Any>()
            messageMap["message"] = message
            messageMap["seen"] = false
            messageMap["type"] = "text"
            messageMap["time"] = ServerValue.TIMESTAMP
            messageMap["thumbImage"] = getImage!!

            val messageUserMap = mutableMapOf<String, Any>()
            messageUserMap["$current_user_ref/$pust_id"] = messageMap
            messageUserMap["$chat_user_ref/$pust_id"] = messageMap

            mRootRef?.updateChildren(messageUserMap) { p0, _ ->

                if(p0 != null){
                    Log.d("CHAT_LOG", p0.message)
                }
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun getFriendIdent() {

        //Menerapkan Listener untuk Referensi Database pada Profi teman
        mRootRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {

                getStatus = p0.child("online").getValue(String::class.java)

            }
        })

        getNama = intent.extras!!.get("Nama")!!.toString()

        friendName.isSelected = true
        friendName.text = getNama
        if(getStatus?.equals("true")!!){

            condition.text = "Online"

        }else{

            val getTimeAgo = GetTimeAgo()
            val lastTime = getStatus?.toLong()
            val lastSeemTime = getTimeAgo.getTimeAgo(lastTime!!)

            condition.text = lastSeemTime

        }
    }
}