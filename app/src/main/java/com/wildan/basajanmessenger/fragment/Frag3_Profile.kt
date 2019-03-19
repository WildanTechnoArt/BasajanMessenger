package com.wildan.basajanmessenger.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.wildan.basajanmessenger.R
import com.wildan.basajanmessenger.fcm.SharedPrefManager
import com.wildan.basajanmessenger.ui.SuntingData
import java.io.ByteArrayOutputStream
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.lang.Exception

class Frag3_Profile : Fragment() {

    //Deklarasi Variable
    private var storageReference1: StorageReference? = null
    private var storageReference2: StorageReference? = null
    private var auth: FirebaseUser? = null
    private var databaseReference: DatabaseReference? = null
    private var changeImage: FloatingActionButton? = null
    private var MyName: TextView? = null
    private var MyNomor: TextView? = null
    private var MyStatus: TextView? = null
    private var suntingItem: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view_frag3 = inflater.inflate(R.layout.fragment_profile, container, false)

        setHasOptionsMenu(true)

        //Inisialisasi Widget
        MyName = view_frag3.findViewById(R.id.my_name)
        MyNomor = view_frag3.findViewById(R.id.my_nomor)
        MyStatus = view_frag3.findViewById(R.id.status)
        changeImage = view_frag3.findViewById(R.id.changeFoto)

        storageReference1 = FirebaseStorage.getInstance().reference
        storageReference2 = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance().currentUser
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users")
                .child(auth?.uid!!)
        databaseReference?.keepSynced(true)

        databaseReference?.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val getImage = p0.child("imageUrl").value.toString()

                if(getImage != "default"){
                    Picasso.get().load(getImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.circle_profil)
                            .into(my_photo, object : Callback{
                                override fun onSuccess() {

                                }

                                override fun onError(e: Exception?) {
                                    Picasso.get().load(getImage).placeholder(R.drawable.circle_profil).into(my_photo)
                                }

                            })
                }

            }
        })

        //Menempilkan Profil user yang didapat pada penyimpanan lokal
        getMyProfil(context!!)

        changeImage?.setOnClickListener({
            addImageGallery()
        })

        return view_frag3
    }

    override fun onResume() {
        super.onResume()
        getMyProfil(context!!)
    }

    //Mendapatkan data user dari penyimpanan lokal
    @SuppressLint("Recycle", "CommitPrefEdits", "SetTextI18n")
    private fun getMyProfil(context: Context) {
        MyName?.text = SharedPrefManager.getInstance(context).userName
        MyNomor?.text = SharedPrefManager.getInstance(context).userNomor
        MyStatus?.text = "\"${SharedPrefManager.getInstance(context).userStatus}\""
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.profile_menu, menu)
        suntingItem = menu?.findItem(R.id.editProfile)
        suntingItem?.setOnMenuItemClickListener({
            val editPage = Intent(context, SuntingData::class.java)
            editPage.putExtra("Nomor", my_nomor?.text)
            startActivity(editPage)
            return@setOnMenuItemClickListener true
        })
    }

    //Mengambil gambar dari Galeri
    private fun addImageGallery() {
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK)
    }

    //Menampilkan foto yang diambil dari kamera atau galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            val imageUri = data?.data
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(200, 200)
                    .start(context!!, this@Frag3_Profile)
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            val result = CropImage.getActivityResult(data)

            if(resultCode == RESULT_OK){

                progressBar.visibility = View.VISIBLE

                val resultUri = result.uri

                val thumbImage = File(resultUri.path)

                val thumbBitmap = Compressor(context)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thumbImage)

                val baos = ByteArrayOutputStream()
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageByte = baos.toByteArray()

                val imageURL = "foto_profil/${auth?.uid}.jpg"
                val thumbURL = "thumb_image/${auth?.uid}.jpg"
                val imagePath = storageReference1?.child(imageURL)
                val thumbPath = storageReference2?.child(thumbURL)
                imagePath?.putFile(resultUri)?.addOnCompleteListener { task ->
                    if(task.isSuccessful){

                        storageReference1?.child(imageURL)?.downloadUrl?.addOnSuccessListener { uri: Uri? ->

                            val uploadTask: UploadTask? = thumbPath?.putBytes(imageByte)
                            val downloadUrl = uri.toString()

                            uploadTask?.addOnCompleteListener{task ->
                                if(task.isSuccessful){

                                    storageReference2?.child(thumbURL)?.downloadUrl?.addOnSuccessListener {

                                        val thumbUrl = uri.toString()

                                        val dataMap = HashMap<String, String>()
                                        dataMap["imageUrl"] = downloadUrl
                                        dataMap["thumbImage"] = thumbUrl

                                        databaseReference?.setValue(dataMap)?.addOnCompleteListener {

                                            if(task.isSuccessful){
                                                Toast.makeText(context, "Uploading Berhasil", Toast.LENGTH_SHORT).show()
                                                progressBar.visibility = View.GONE
                                            }
                                        }
                                    }

                                }else{
                                    Toast.makeText(context, "Uploading Gagal", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                }
                            }

                        }?.addOnFailureListener {
                                    Toast.makeText(context, "Uploading Gagal", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                }

                    }else{
                        Toast.makeText(context, "Uploading Gagal", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                }
            }

            else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                val error = result.error
                Log.d("Profil", error.toString())
                Toast.makeText(context, "Crop Image Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //ID permintaan untuk memilih metode mengambilan foto
    companion object {
        const val GALLERY_PICK = 1
    }
}