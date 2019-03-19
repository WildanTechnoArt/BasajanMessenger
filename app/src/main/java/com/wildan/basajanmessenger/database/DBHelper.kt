package com.wildan.basajanmessenger.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBHelper(context: Context) : SQLiteOpenHelper(context, DBName, null, DBVersion) {

    //Data-data kolom yang digunakan untuk database kontak
     class DataKontak : BaseColumns {
        companion object {
            const val TABLENAME_KONTAK = "Kontak"
            const val USERNAME = "NamaTeman"
            const val NO_TELPON = "Nomor"
        }
    }

    companion object {

        private const val DBName = "Basajan_Messenger.db"
        private const val DBVersion = 12

        //Database Kontak
        private const val SQLITE_ENTRIES1 = "CREATE TABLE " + DataKontak.TABLENAME_KONTAK +
                "(" + DataKontak.USERNAME + " TEXT NOT NULL, " +
                DataKontak.NO_TELPON + " TEXT PRIMARY KEY NOT NULL)"
        private const val UPDATE_DB_KONTAK = "DROP TABLE IF EXISTS " + DataKontak.TABLENAME_KONTAK
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQLITE_ENTRIES1)
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL(UPDATE_DB_KONTAK)
        onCreate(db)
    }
}
