package com.wildan.basajanmessenger.models

//Data-data user yang terhubung dengan Firebase Realtime Database
class FriendModel{

    //Deklarasi Variable
    var date: String? = null

    //Konstruktor kosong, untuk data snapshot pada Firebase RDB
    constructor() {}

    constructor(date: String) {
        this.date = date
    }
}