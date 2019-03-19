package com.wildan.basajanmessenger.models

//Data-data user yang terhubung dengan Firebase Realtime Database
class dataUser{

    //Deklarasi Variable
    var userToken: String? = null
    var searchIndex: String? = null
    var namaUser: String? = null
    var imageUrl: String? = null
    var thumbImage: String? = null
    var nomorUser: String? = null
    var statusUser: String? = null

    //Konstruktor kosong, untuk data snapshot pada Firebase RDB
    constructor() {}

    constructor(searchIndex: String, userToken: String, namaUser: String, imageUrl: String,
                thumbImage: String, nomorUser: String, statusUser: String) {
        this.searchIndex = searchIndex
        this.userToken = userToken
        this.namaUser = namaUser
        this.imageUrl = imageUrl
        this.thumbImage = thumbImage
        this.nomorUser = nomorUser
        this.statusUser = statusUser
    }
}