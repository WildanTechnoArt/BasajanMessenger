package com.wildan.basajanmessenger.ui

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.graphics.PorterDuff
import android.net.Uri
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*
import com.wildan.basajanmessenger.R

import java.io.File
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.wildan.basajanmessenger.adapter.PagerAdapter
import com.wildan.basajanmessenger.interfaces.dataListener

class MainActivity : AppCompatActivity(), View.OnClickListener, dataListener {

    private var kondisi = false
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    private var reference: DatabaseReference? = null
    private var mUsersDatabase: DatabaseReference? = null
    private var user: FirebaseUser? = null
    private var userID: FirebaseAuth? = null
    private var checkUser: FirebaseAuth.AuthStateListener? = null

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar) // Memasang Toolbar pada Aplikasi
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportActionBar!!.title = "Obrolan"
        user = FirebaseAuth.getInstance().currentUser
        userID = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference
        checkPlayServices()

        mUsersDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(userID?.currentUser?.uid!!)

        add_friend.setOnClickListener(this)

        //Memanggil dan Memasukan Value pada Class PagerAdapter(FragmentManager dan JumlahTab)
        val pageAdapter = PagerAdapter(supportFragmentManager, tabs.tabCount)

        //Memasang Adapter pada ViewPager
        container.adapter = pageAdapter

        /*
         Menambahkan Listener yang akan dipanggil kapan pun halaman berubah atau
         bergulir secara bertahap, sehingga posisi tab tetap singkron
         */
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))

        //Callback Interface dipanggil saat status pilihan tab berubah.
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //Dipanggil ketika tab memasuki state/keadaan yang dipilih.
                container.currentItem = tab.position

                //Menentukan judul pada Toolbar sesuai dengan posisi tab yang dipilih
                when {
                    tab.position == 0 -> {
                        supportActionBar!!.title = "Obrolan"
                        add_friend.visibility = View.VISIBLE
                    }
                    tab.position == 1 -> {
                        supportActionBar!!.title = "Kontak"
                        add_friend.visibility = View.VISIBLE
                    }
                    tab.position == 2 -> {
                        supportActionBar!!.title = "Profil"
                        add_friend.visibility = View.GONE
                    }
                }

                //Memberi warna putih pada tab icon
                val tabIconColor = ContextCompat.getColor(baseContext, R.color.background_material_dark_1)
                tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
            }

            //Mengubah warna tab menjadi kehitaman jika tida dipilih
            override fun onTabUnselected(tab: TabLayout.Tab) {
                //Dipanggil saat tab keluar dari keadaan yang dipilih.
                val tabIconColor = ContextCompat.getColor(baseContext, R.color.background_material_dark_2)
                tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                //Dipanggil ketika tab yang sudah dipilih, dipilih lagi oleh user.
            }
        })
    }

    override fun onStart() {
        super.onStart()

        checkUser = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                mUsersDatabase?.child("online")?.setValue(true)
            }else{
                startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                finish()

            }
        }

        getTabPosition()

        userID?.addAuthStateListener(checkUser!!)
    }

    //Mencopot Listener Autentikasi saat aplikasi dihentikan
    override fun onStop() {
        super.onStop()

        checkUser = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                mUsersDatabase?.child("online")?.setValue(ServerValue.TIMESTAMP)
            }
        }

        if(checkUser != null){
            userID?.removeAuthStateListener(checkUser as FirebaseAuth.AuthStateListener)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    //Menuju posisi Tab(teman) setelah user menambahkan atau mengedit kontak
    private fun getTabPosition(){
        try{
            kondisi = intent.extras.getBoolean("condition")
            if(kondisi){
                kondisi = false
                supportActionBar!!.title = "Kontak"
                tabs.getTabAt(1)?.select()
            }
        }catch (ex: NullPointerException){
            ex.printStackTrace()
        }
    }

    //Mengecek Layanan GooglePlay Service pada Perangkat Android
    private fun checkPlayServices(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show()
            }
            return false
        }
        return true
    }

    //Membuat menu Opsi
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bar, menu)
        return true
    }

    //Menu opsi pada MainActivity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> startActivity(Intent(this@MainActivity, About::class.java))
            R.id.rate -> setRate()
            R.id.share -> setShare()
        }//Statement
        return true
    }

    //Method untuk fungsi rate/memberi rating pada aplikasi
    private fun setRate() {
        try {
            //Jika Terdapat Google PlayStore pada Perangkat Android
            //Maka akan langsung terhubung dengan PlayStore Tersebut
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse
            ("market://details?id=" + "net.wildan.basajanmessenger.messenger&hl=in")))
        } catch (ex: ActivityNotFoundException) {
            //Jika tidak, maka akan terhubung dengan Browser
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse
            ("https://play.google.com/store/" + "apps/details?id=net.wildan.basajanmessenger.messenger&hl=in")))
        }

    }

    private fun setShare() {
        val appInfo = applicationContext.applicationInfo
        val apkPath = appInfo.sourceDir
        val Share = Intent()
        Share.action = Intent.ACTION_SEND
        Share.type = "application/vnd.android.package-archive"
        Share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(apkPath)))
        startActivity(Intent.createChooser(Share, "Berbagi Melalui"))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_friend -> {
                startActivity(Intent(this@MainActivity, AddFriend::class.java))
            }
        }
    }

    override fun onDeleteData(friend: String, position: Int) {
        /*
         * Kode ini akan dipanggil ketika method onDeleteData
         * dipanggil dari adapter pada RecyclerView melalui interface.
         * kemudian akan menghapus data berdasarkan Nomor Telepon Pengguna dari data tersebut
         */
        val noProvider = FirebaseAuth.getInstance().currentUser!!.phoneNumber
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Users").child(noProvider.toString()).child("PersonalChat").child(friend).removeValue()
    }
}