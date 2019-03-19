package com.wildan.basajanmessenger.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.graphics.Typeface
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView

import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy

import com.wildan.basajanmessenger.R
import com.wildan.basajanmessenger.models.Conv
import com.wildan.basajanmessenger.ui.ChatFrame

class Frag2_Pesan : Fragment() {

    private var mConvList: RecyclerView? = null
    private var kontak_img: ImageView? = null
    private var K_notif1: TextView? = null
    private var K_notif2: TextView? = null

    private var mConvDatabase: DatabaseReference? = null
    private var mMessageDatabase: DatabaseReference? = null
    private var mUsersDatabase: DatabaseReference? = null
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Conv, ConvViewHolder>? = null

    private var mAuth: FirebaseAuth? = null

    private var mCurrent_user_id: String? = null

    private val mMainView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view_frag2 = inflater.inflate(R.layout.fragment_pesan, container, false)
        setHasOptionsMenu(true)

        mConvList = mMainView?.findViewById(R.id.conv_list)
        kontak_img = view_frag2.findViewById(R.id.kontak_img)
        K_notif1 = view_frag2.findViewById(R.id.K_notif1)
        K_notif2 = view_frag2.findViewById(R.id.K_notif2)
        mAuth = FirebaseAuth.getInstance()

        mCurrent_user_id = mAuth?.currentUser?.uid

        mConvDatabase = FirebaseDatabase.getInstance().reference.child("Chat").child(mCurrent_user_id!!)

        mConvDatabase!!.keepSynced(true)
        mUsersDatabase = FirebaseDatabase.getInstance().reference.child("Users")
        mMessageDatabase = FirebaseDatabase.getInstance().reference.child("Messages").child(mCurrent_user_id!!)
        mUsersDatabase!!.keepSynced(true)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        mConvList?.setHasFixedSize(true)
        mConvList?.layoutManager = linearLayoutManager

        mMessageDatabase?.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val itemCount = p0.childrenCount
                if (itemCount > 0) {
                    mConvList?.visibility = View.VISIBLE
                    kontak_img?.visibility = View.GONE
                    K_notif1?.visibility = View.GONE
                    K_notif2?.visibility = View.GONE
                } else {
                    mConvList?.visibility = View.GONE
                    kontak_img?.visibility = View.VISIBLE
                    K_notif1?.visibility = View.VISIBLE
                    K_notif2?.visibility = View.VISIBLE
                }
            }
        })

        return view_frag2
    }

    override fun onStart() {
        super.onStart()
        firebaseRecyclerAdapter()

    }

    private fun firebaseRecyclerAdapter(){

        val conversationQuery = mConvDatabase?.orderByChild("timestamp")

        val options = FirebaseRecyclerOptions.Builder<Conv>()
                .setQuery(conversationQuery!!, Conv::class.java)
                .setLifecycleOwner(this)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvViewHolder {
                return ConvViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.user_friends_layout, parent, false))
            }

            override fun onBindViewHolder(holder: ConvViewHolder, position: Int, model: Conv) {

                val list_user_id = getRef(position).key

                val lastMessageQuery = mMessageDatabase?.child(list_user_id!!)?.limitToLast(1)

                lastMessageQuery?.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                        val data = dataSnapshot.child("message").value!!.toString()
                        holder.setMessage(data, model.isSeen)

                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Frag2_Pesan.kt", databaseError.message)
                    }
                })

                mUsersDatabase?.child(list_user_id!!)?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val userName = dataSnapshot.child("name").value!!.toString()
                        val userThumb = dataSnapshot.child("thumb_image").value!!.toString()

                        if (dataSnapshot.hasChild("online")) {

                            val userOnline = dataSnapshot.child("online").value!!.toString()
                            holder.setUserOnline(userOnline)

                        }

                        holder.setName(userName)
                        holder.setUserImage(userThumb)

                        holder.mView.setOnClickListener {
                            val chatIntent = Intent(context, ChatFrame::class.java)
                            chatIntent.putExtra("user_id", list_user_id)
                            chatIntent.putExtra("Nama", userName)
                            startActivity(chatIntent)
                        }


                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Frag2_Pesan.kt", databaseError.message)
                    }
                })

            }
        }

        mConvList?.adapter = firebaseRecyclerAdapter
    }

    private fun firebaseRecyclerSearch(newText: String){

        val lowerText = newText.toLowerCase()

        val conversationQuery: Query = mConvDatabase!!.orderByChild("searchIndex")
                .startAt(lowerText).endAt(lowerText+"\uf8ff")

        val options = FirebaseRecyclerOptions.Builder<Conv>()
                .setQuery(conversationQuery, Conv::class.java)
                .setLifecycleOwner(this)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvViewHolder {
                return ConvViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.user_friends_layout, parent, false))
            }

            override fun onBindViewHolder(holder: ConvViewHolder, position: Int, model: Conv) {

                val list_user_id = getRef(position).key

                val lastMessageQuery = mMessageDatabase?.child(list_user_id!!)?.limitToLast(1)

                lastMessageQuery?.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                        val data = dataSnapshot.child("message").value!!.toString()
                        holder.setMessage(data, model.isSeen)

                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Frag2_Pesan.kt", databaseError.message)
                    }
                })

                mUsersDatabase?.child(list_user_id!!)?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val userName = dataSnapshot.child("name").value!!.toString()
                        val userThumb = dataSnapshot.child("thumb_image").value!!.toString()

                        if (dataSnapshot.hasChild("online")) {

                            val userOnline = dataSnapshot.child("online").value!!.toString()
                            holder.setUserOnline(userOnline)

                        }

                        holder.setName(userName)
                        holder.setUserImage(userThumb)

                        holder.mView.setOnClickListener {
                            val chatIntent = Intent(context, ChatFrame::class.java)
                            chatIntent.putExtra("user_id", list_user_id)
                            chatIntent.putExtra("Nama", userName)
                            startActivity(chatIntent)
                        }


                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Frag2_Pesan.kt", databaseError.message)
                    }
                })

            }
        }

        mConvList?.adapter = firebaseRecyclerAdapter
    }

    class ConvViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setMessage(message: String, isSeen: Boolean) {

            val userStatusView = mView.findViewById(R.id.status) as TextView
            userStatusView.text = message

            if (!isSeen) {
                userStatusView.setTypeface(userStatusView.typeface, Typeface.BOLD)
            } else {
                userStatusView.setTypeface(userStatusView.typeface, Typeface.NORMAL)
            }

        }

        fun setName(name: String) {

            val userNameView = mView.findViewById(R.id.username) as TextView
            userNameView.text = name

        }

        fun setUserImage(thumb_image: String) {

            val userImageView = mView.findViewById(R.id.photo) as CircleImageView
            Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.circle_profil)
                    .into(userImageView, object : Callback {
                        override fun onSuccess() {

                        }

                        override fun onError(e: Exception?) {
                            Picasso.get().load(thumb_image).placeholder(R.drawable.circle_profil).into(userImageView)
                        }

                    })

        }

        fun setUserOnline(online_status: String) {

            val userOnlineView = mView.findViewById(R.id.statusOnline) as ImageView

            if (online_status == "true") {

                userOnlineView.visibility = View.VISIBLE

            } else {

                userOnlineView.visibility = View.INVISIBLE

            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_bar2, menu)
        val searchItem = menu?.findItem(R.id.searchInput)
        val searchView = activity!!.findViewById<MaterialSearchView>(R.id.search_view)
        searchView.setHint("Cari...")
        searchView.setMenuItem(searchItem)
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            @SuppressLint("SetTextI18n")
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(nextText: String): Boolean {
                firebaseRecyclerSearch(nextText)
                return true
            }
        })
    }
}