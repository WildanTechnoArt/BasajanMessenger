package com.wildan.basajanmessenger.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        //Mendapatkan data-data dari notifikasi yang dikirim device
        val notificationTitle = remoteMessage?.notification?.title.toString()
        val notificationBody = remoteMessage?.notification?.body.toString()
        val clickAction = remoteMessage?.notification?.clickAction.toString()
        val formUserID = remoteMessage?.data?.get("from_user_id")

        //Memeriksa apakan pesan berisi muatan pemberitahuan
        if (remoteMessage!!.notification != null) {
            val mBuilder = NotificationCompat.Builder(this, "BasajanMessenger")
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val resultIntent = Intent(clickAction)
            resultIntent.putExtra("user_id", formUserID)

            val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            mBuilder.setContentIntent(pendingIntent)

            val notificationId = System.currentTimeMillis().toInt()

            val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notifyManager.notify(notificationId, mBuilder.build())
        }
    }

    //Method untuk mendapatkan Token Baru
    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        //Mendapatkan Token dari FCM untuk klien
        Log.d(TAG, "Token Saya : " + token!!)
        storeToken(token)
    }

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

    //Mengirim dan menyimpan Token pada SharedPreference
    private fun storeToken(refreshedToken: String?) {
        SharedPrefManager.getInstance(applicationContext).storeToken(refreshedToken!!)
    }
}