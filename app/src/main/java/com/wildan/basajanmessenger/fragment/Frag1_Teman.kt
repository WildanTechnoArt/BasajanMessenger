package com.wildan.basajanmessenger.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.wildan.basajanmessenger.R
import com.wildan.basajanmessenger.models.FriendModel
import com.wildan.basajanmessenger.ui.ChatFrame
import com.wildan.basajanmessenger.ui.FriendProfile

@Suppress("NAME_SHADOWING")
open class Frag1_Teman : Fragment(){

    //Deklarasi Variable
    private var recycler: RecyclerView? = null
    private var kontak_img: ImageView? = null
    private var K_notif1: TextView? = null
    private var K_notif2: TextView? = null
    private var auth: FirebaseAuth? = null
    private var searchItem: MenuItem? = null
    private var searchView: MaterialSearchView? = null
    private var mCurrent_user_id: String? = null
    private var mFriendDatabase: DatabaseReference? = null
    private var mUsersDatabase: DatabaseReference? = null
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<FriendModel, UsersViewHolder>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view_frag1 = inflater.inflate(R.layout.fragment_kontak, container, false)
        setHasOptionsMenu(true)
        recycler = view_frag1.findViewById(R.id.view_teman)
        kontak_img = view_frag1.findViewById(R.id.kontak_img)
        K_notif1 = view_frag1.findViewById(R.id.K_notif1)
        K_notif2 = view_frag1.findViewById(R.id.K_notif2)

        val layout = LinearLayoutManager(context)
        recycler?.layoutManager = layout
        recycler?.setHasFixedSize(true)

        auth = FirebaseAuth.getInstance()
        mCurrent_user_id = auth?.uid

        mFriendDatabase = FirebaseDatabase.getInstance().reference.child("Friends").child(mCurrent_user_id!!)
        mFriendDatabase?.keepSynced(true)
        mUsersDatabase = FirebaseDatabase.getInstance().reference.child("Users")
        mUsersDatabase?.keepSynced(true)

        mFriendDatabase?.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                Log.e("Frag1_Teman.kt", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val itemCount = p0.childrenCount
                if (itemCount > 0) {
                    recycler?.visibility = View.VISIBLE
                    kontak_img?.visibility = View.GONE
                    K_notif1?.visibility = View.GONE
                    K_notif2?.visibility = View.GONE
                } else {
                    recycler?.visibility = View.GONE
                    kontak_img?.visibility = View.VISIBLE
                    K_notif1?.visibility = View.VISIBLE
                    K_notif2?.visibility = View.VISIBLE
                }
            }
        })

        return view_frag1
    }

    override fun onStart() {
        super.onStart()
        firebaseRecyclerAdapter()
    }

    override fun onResume() {
        super.onResume()
        searchView?.closeSearch()
    }

    private fun firebaseRecyclerAdapter(){

        val options = FirebaseRecyclerOptions.Builder<FriendModel>()
                .setQuery(this.mFriendDatabase!!, FriendModel::class.java)
                .setLifecycleOwner(this)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FriendModel, UsersViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
                return UsersViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_teman, parent, false))
            }

            override fun onBindViewHolder(holder: UsersViewHolder, position: Int, model: FriendModel) {

                holder.setDate(model.date!!)
                val list_user_id = getRef(holder.adapterPosition).key

                mUsersDatabase?.child(list_user_id!!)?.addValueEventListener(object : ValueEventListener{

                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("Frag1_Teman.kt", p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val getNama = p0.child("namaUser").getValue(String::class.java)
                        val getImage = p0.child("thumbImage").getValue(String::class.java)

                        if(p0.hasChild("online")){
                            val getOnlineStatus: Boolean = p0.child("online").value as Boolean
                            holder.setStatusOnline(getOnlineStatus)
                        }

                        holder.setDisplayName(getNama!!)
                        holder.setDisplayImage(getImage!!)

                        holder.mView?.setOnClickListener { v: View? ->
                            val intent = Intent(v?.context, ChatFrame::class.java)
                            intent.putExtra("Nama", getNama)
                            intent.putExtra("Image", getImage)
                            startActivity(intent)
                        }

                        holder.mView?.setOnLongClickListener { v: View? ->
                            val action = arrayOf("Lihat profil", "Kirim pesan")
                            val alert = AlertDialog.Builder(v?.context!!)
                            alert.setTitle("Pilih Opsi")
                            alert.setItems(action) { _, i ->
                                when (i) {
                                    0 -> {
                                        val dataForm = Intent(v.context, FriendProfile::class.java)
                                        dataForm.putExtra("user_id", list_user_id)
                                        context?.startActivity(dataForm)
                                    }
                                    1 -> {
                                        val dataForm = Intent(v.context, ChatFrame::class.java)
                                        dataForm.putExtra("user_id", list_user_id)
                                        dataForm.putExtra("Nama", getNama)
                                        dataForm.putExtra("Image", getImage)
                                        context?.startActivity(dataForm)
                                    }
                                }
                            }
                            alert.create()
                            alert.show()
                            true
                        }
                    }
                })
            }
            override fun onDataChanged() {
                super.onDataChanged()
                firebaseRecyclerAdapter?.notifyDataSetChanged()
            }
        }

        recycler?.adapter = firebaseRecyclerAdapter
    }

    private fun firebaseRecyclerSearch(newText: String){

        val lowerText = newText.toLowerCase()

        val query: Query = mFriendDatabase!!.orderByChild("searchIndex").startAt(lowerText).endAt(lowerText+"\uf8ff")

        val options = FirebaseRecyclerOptions.Builder<FriendModel>()
                .setQuery(query, FriendModel::class.java)
                .setLifecycleOwner(this)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<FriendModel, UsersViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
                return UsersViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_teman, parent, false))
            }

            override fun onBindViewHolder(holder: UsersViewHolder, position: Int, model: FriendModel) {

                holder.setDate(model.date!!)
                val list_user_id = getRef(holder.adapterPosition).key

                mUsersDatabase?.child(list_user_id!!)?.addValueEventListener(object : ValueEventListener{

                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("Frag1_Teman.kt", p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        val getNama = p0.child("namaUser").getValue(String::class.java)
                        val getImage = p0.child("thumbImage").getValue(String::class.java)

                        if(p0.hasChild("online")){
                            val getOnlineStatus: Boolean = p0.child("online").value as Boolean
                            holder.setStatusOnline(getOnlineStatus)
                        }

                        holder.setDisplayName(getNama!!)
                        holder.setDisplayImage(getImage!!)

                        holder.mView?.setOnClickListener { v: View? ->
                            val intent = Intent(v?.context, ChatFrame::class.java)
                            intent.putExtra("Nama", getNama)
                            intent.putExtra("Image", getImage)
                            startActivity(intent)
                        }

                        holder.mView?.setOnLongClickListener { v: View? ->
                            val action = arrayOf("Lihat profil", "Kirim pesan")
                            val alert = AlertDialog.Builder(v?.context!!)
                            alert.setTitle("Pilih Opsi")
                            alert.setItems(action) { _, i ->
                                when (i) {
                                    0 -> {
                                        val dataForm = Intent(v.context, FriendProfile::class.java)
                                        dataForm.putExtra("user_id", list_user_id)
                                        context?.startActivity(dataForm)
                                    }
                                    1 -> {
                                        val dataForm = Intent(v.context, ChatFrame::class.java)
                                        dataForm.putExtra("user_id", list_user_id)
                                        dataForm.putExtra("Nama", getNama)
                                        dataForm.putExtra("Image", getImage)
                                        context?.startActivity(dataForm)
                                    }
                                }
                            }
                            alert.create()
                            alert.show()
                            true
                        }
                    }
                })
            }
            override fun onDataChanged() {
                super.onDataChanged()
                firebaseRecyclerAdapter?.notifyDataSetChanged()
            }
        }

        recycler?.adapter = firebaseRecyclerAdapter
    }

    class UsersViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {

        var mView: View? = mView

        fun setDisplayName(username: String){
            val userNameView: TextView = mView?.findViewById(R.id.username)!!
            userNameView.text = username
        }

        fun setDate(date: String){
            val userStatusView: TextView = mView?.findViewById(R.id.status)!!
            userStatusView.text = date
        }

        fun setStatusOnline(statusOnline: Boolean){
            val userStatusOnline: ImageView = mView?.findViewById(R.id.statusOnline)!!
            if(statusOnline){
                userStatusOnline.visibility = View.VISIBLE
            }else{
                userStatusOnline.visibility = View.INVISIBLE
            }
        }

        @SuppressLint("WrongViewCast")
        fun setDisplayImage(thumbUrl: String){
            val userImageView: ImageView = mView?.findViewById(R.id.photo)!!
            if(thumbUrl != "default"){
                Picasso.get().load(thumbUrl).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.circle_profil)
                        .into(userImageView, object : Callback {
                            override fun onSuccess() {

                            }

                            override fun onError(e: Exception?) {
                                Picasso.get().load(thumbUrl).placeholder(R.drawable.circle_profil).into(userImageView)
                            }

                        })
            }
        }
    }

    //Digunakan untuk menfilter data kontak pada SearchView
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.search_menu, menu)
        searchItem = menu?.findItem(R.id.searchInput)
        searchView = activity?.findViewById(R.id.search_view)
        searchView?.setMenuItem(searchItem)
        searchView?.setHint("Cari...")
        searchView?.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {

            @SuppressLint("SetTextI18n")
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                firebaseRecyclerSearch(newText)
                return true
            }
        })
    }
}