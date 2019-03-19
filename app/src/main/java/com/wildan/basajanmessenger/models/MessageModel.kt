package com.wildan.basajanmessenger.models

//Data-data user yang terhubung dengan Firebase Realtime Database
class MessageModel{

    //Deklarasi Variable
    var userID: String? = null
    var message: String? = null
    var seen: String? = null
    var type: String? = null
    var time: Long? = null
    var thumbImage: String? = null

    //Konstruktor kosong, untuk data snapshot pada Firebase RDB
    constructor() {}

    constructor(userID: String, message: String, seen: String, type: String, time: Long, thumbImage: String) {
        this.userID = userID
        this.message = message
        this.seen = seen
        this.type = type
        this.time = time
        this.thumbImage = thumbImage
    }
}