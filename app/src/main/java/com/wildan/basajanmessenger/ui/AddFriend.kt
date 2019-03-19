package com.wildan.basajanmessenger.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_friend.*

import com.wildan.basajanmessenger.R
import com.wildan.basajanmessenger.models.dataUser
import de.hdodenhof.circleimageview.CircleImageView

class AddFriend : AppCompatActivity() {

    //Deklarasi Variable
    private var mAuth: FirebaseUser? = null
    private var databaseReference: DatabaseReference? = null
    private var getDatabase: FirebaseDatabase? = FirebaseDatabase.getInstance()
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<dataUser, AddFriend.UsersViewHolder>? = null
    private var searchName = true


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        //Inisialisasi dan Instance FirebaseUser(getUser)
        mAuth = FirebaseAuth.getInstance().currentUser

        //Inisialisasi Toolbar dan Menampilkan Menu Home pada Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Menentukan bagaimana item pada RecyclerView tampil
        val layout = LinearLayoutManager(this@AddFriend)
        friendSearch.layoutManager = layout
        friendSearch.setHasFixedSize(true)

        //Mendapatkan Instance dari Firebase Realtime Database
        databaseReference = getDatabase?.reference?.child("Users")

        //Tombol search, untuk mencari teman / kontak baru
        search.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            //Menyembunyikan keyboard saat tombol Search Diklik
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchInput.windowToken, 0)

                //Mencari teman berdasarkan kategori yang dipilih (Nama / Nomor)
                if(searchName){

                    //Mendapatkan Input berupa nama
                    val getFriend = searchInput.text.toString()

                    if(getFriend.isNotEmpty()){
                        //Mencari teman berdasarkan nama
                        firebaseRecyclerSearch(getFriend)
                    }else{
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@AddFriend, "Masukan Nama Yang Dicari", Toast.LENGTH_SHORT).show()
                    }

                }else{

                    //Mendapatkan Input berupa nomor telepon
                    val getFriend = phoneSearch.text.toString()

                    if(getFriend.isNotEmpty()){

                        if(getFriend.length < 10){
                            progressBar.visibility = View.GONE
                            Toast.makeText(applicationContext, "No. Telepon Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                        }else{
                            //Mencari teman berdasrkan nomor kontak
                            firebaseRecyclerSearchNomor(getFriend)
                        }
                    }else{
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@AddFriend, "Masukan Nomor Yang Dicari", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //Untuk memilih teman berdasarkan Nama / Nomor
        category.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.src_nama ->{
                    phoneSearch.visibility = View.GONE
                    searchInput.visibility = View.VISIBLE
                    searchName = true
                    searchInput.text = null
                }
                R.id.src_nomor ->{
                    searchInput.visibility = View.GONE
                    phoneSearch.visibility = View.VISIBLE
                    searchName = false
                }
            }
        }
    }

    //Digunakan untuk menentukan aksi pada tombol home
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    //Class Holder untuk menetukan data yang akan ditampilkan pada RecyclerView (Firebase)
    class UsersViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {

        var mView: View? = mView

        //Menampilkan nama teman yang dicari
        fun setDisplayName(username: String){
            val nama: TextView = mView?.findViewById(R.id.username)!!
            nama.text = username
        }

        //Menampilkan status teman yang dicari
        fun setDisplayStatus(statusUser: String){
            val status: TextView = mView?.findViewById(R.id.status)!!
            status.isSelected = true
            status.text = statusUser
        }

        //Menampilkan foto profil teman yang dicari
        @SuppressLint("WrongViewCast")
        fun setDisplayImage(thumbUrl: String){
            val defaultImage: CircleImageView = mView?.findViewById(R.id.photo)!!
            if(thumbUrl != "default"){
                Picasso.get().load(thumbUrl).placeholder(R.drawable.default_image).into(defaultImage)
            }
        }

    }

    //Method untuk mencari kontak baru berdasarkan nama
    private fun firebaseRecyclerSearch(newText: String){

        //Mengubah teks yang diinputkan menjadi huruf kecil
        val lowerText = newText.toLowerCase()

        //Mengatur Query database untuk mengurutkan data (Teman) berdasarkan input (Nama) dari User
        val query: Query = databaseReference!!.orderByChild("searchIndex").startAt(lowerText).endAt(lowerText+"\uf8ff")

        //Menerapkan Setelan pada FirebaseRecyclerOptions
        val options = FirebaseRecyclerOptions.Builder<dataUser>()
                .setQuery(query, dataUser::class.java)
                .setLifecycleOwner(this@AddFriend)
                .build()

        //Medapatkan Referensi Database, berdasarkan Input (Nama)
        databaseReference!!.orderByChild("searchIndex").startAt(lowerText).endAt(lowerText+"\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                //Menambahkan Listener dan mengecek data yang dimasukan, apakah ada atau tidak
                progressBar.visibility = View.VISIBLE
                if(!p0.exists()){
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AddFriend, "Nama Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.e("AddFriend.kt", p0.message)
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddFriend, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
            }

        })

        //Membuat Instance dan Mengatur Setelan Adapter
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<dataUser, AddFriend.UsersViewHolder>(options) {

            /*
            Menerapkan Layout untuk menampilkan Chat List
            Secara Default Chat List akan tampil di sebelah kiri (Teman)
            */
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFriend.UsersViewHolder {
                return AddFriend.UsersViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_teman, parent, false))
            }

            /*
             Menetukan data-data yang akan di tampilkan pada masing-masing View
             Didalam setiap pada RecyclerView
             */
            override fun onBindViewHolder(holder: AddFriend.UsersViewHolder, position: Int, model: dataUser) {

                //Mendaptkan data-data user (teman) dan menampilkannya pada masing2 View
                holder.setDisplayName(model.namaUser.toString())
                holder.setDisplayStatus(model.statusUser.toString())
                holder.setDisplayImage(model.thumbImage.toString())

                progressBar.visibility = View.GONE

                //Mendapatkan kode unik (User ID)
                val getKey = getRef(holder.adapterPosition).key
                val getNomor = model.nomorUser.toString()

                holder.mView?.setOnClickListener { v: View? ->
                    if(getNomor == mAuth?.phoneNumber){
                        val alert = AlertDialog.Builder(this@AddFriend)
                                .setMessage("Anda Tidak Dapat Menambahkan Diri Sendiri Sebagai Teman")
                                .setPositiveButton("OK"){ dialog, _ ->
                                    dialog.dismiss()
                                }
                        alert.create()
                        alert.show()
                    }else{
                        val intent = Intent(v?.context, FriendProfile::class.java)
                        intent.putExtra("user_id", getKey.toString())
                        startActivity(intent)
                    }
                }
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Log.e("AddFriend.kt", error.message)
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddFriend, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChanged() {
                super.onDataChanged()
                firebaseRecyclerAdapter?.notifyDataSetChanged()
            }
        }

        //Memasang adapter pada RecyclerView didalam Activity AddFriend
        friendSearch?.adapter = firebaseRecyclerAdapter
    }

    private fun firebaseRecyclerSearchNomor(newText: String){

        //Mengubah teks yang diinputkan menjadi huruf kecil
        val lowerText = newText.toLowerCase()

        //Mengatur Query database untuk mengurutkan data (Teman) berdasarkan input (Nomor) dari User
        val query: Query = databaseReference!!.orderByChild("nomorUser").startAt(lowerText).endAt(lowerText+"\uf8ff")

        //Menerapkan Setelan pada FirebaseRecyclerOptions
        val options = FirebaseRecyclerOptions.Builder<dataUser>()
                .setQuery(query, dataUser::class.java)
                .setLifecycleOwner(this@AddFriend)
                .build()

        databaseReference!!.orderByChild("searchIndex").startAt(lowerText).endAt(lowerText+"\uf8ff")
                .addListenerForSingleValueEvent(object : ValueEventListener{

                    override fun onDataChange(p0: DataSnapshot) {
                        //Menambahkan Listener dan mengecek data yang dimasukan, apakah ada atau tidak
                        progressBar.visibility = View.VISIBLE
                        if(!p0.exists()){
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@AddFriend, "Nomor Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.e("AddFriend.kt", p0.message)
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@AddFriend, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
                    }

                })

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<dataUser, AddFriend.UsersViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFriend.UsersViewHolder {
                return AddFriend.UsersViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_teman, parent, false))
            }

            override fun onBindViewHolder(holder: AddFriend.UsersViewHolder, position: Int, model: dataUser) {
                holder.setDisplayName(model.namaUser.toString())
                holder.setDisplayStatus(model.statusUser.toString())
                holder.setDisplayImage(model.thumbImage.toString())

                progressBar.visibility = View.GONE

                val getKey = getRef(holder.adapterPosition).key
                val getNomor = model.nomorUser.toString()

                holder.mView?.setOnClickListener { v: View? ->
                    if(getNomor == mAuth?.phoneNumber){
                        val alert = AlertDialog.Builder(this@AddFriend)
                                .setMessage("Anda Tidak Dapat Menambahkan Diri Sendiri Sebagai Teman")
                                .setPositiveButton("OK"){ dialog, _ ->
                                    dialog.dismiss()
                                }
                        alert.create()
                        alert.show()                    }
                    val intent = Intent(v?.context, FriendProfile::class.java)
                    intent.putExtra("user_id", getKey.toString())
                    startActivity(intent)
                }
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Log.e("AddFriend.kt", error.message)
                progressBar.visibility = View.GONE
                Toast.makeText(this@AddFriend, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChanged() {
                super.onDataChanged()
                firebaseRecyclerAdapter?.notifyDataSetChanged()
            }
        }

        //Memasang adapter pada RecyclerView didalam Activity AddFriend
        friendSearch?.adapter = firebaseRecyclerAdapter
    }
}
