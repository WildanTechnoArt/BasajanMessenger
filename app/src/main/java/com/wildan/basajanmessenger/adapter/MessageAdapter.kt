@file:Suppress("NAME_SHADOWING")

package com.wildan.basajanmessenger.adapter

import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.github.library.bubbleview.BubbleTextView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

import com.wildan.basajanmessenger.R
import com.wildan.basajanmessenger.models.MessageModel
import com.wildan.basajanmessenger.ui.ChatFrame
import de.hdodenhof.circleimageview.CircleImageView

//Class ini digunakan untuk mengatur bagaimana data untuk chat ditampil
class MessageAdapter(private val chatFrame: ChatFrame, options: FirebaseListOptions<MessageModel>)
      : FirebaseListAdapter<MessageModel>(options) {

    var PhotoFriendView: CircleImageView? = null

    override fun populateView(v: View?, model: MessageModel, position: Int) {
        val ChatView = v?.findViewById<BubbleTextView>(R.id.msg_text)
        val DateView = v?.findViewById<TextView>(R.id.msg_date)
        PhotoFriendView = v?.findViewById(R.id.friendPhoto)
        //Menginisialisasi view-view pada layout Chat
        Picasso.get().load(model.thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.circle_profil)
                .into(PhotoFriendView, object : Callback {
                    override fun onSuccess() {

                    }

                    override fun onError(e: Exception?) {
                        Picasso.get().load(model.thumbImage).placeholder(R.drawable.circle_profil).into(PhotoFriendView)
                    }

                })
        ChatView?.text = model.message
        DateView?.text = DateFormat.format("hh:mm a", model.time!!)
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View? {
        val view: View?
        val messageModel = getItem(position) //Mendapatkan data User berdasarkan penggunanya

        //Digunakan untuk menentukan layout chat kita atau user, agar chat tampil dua arah(kanan dan kiri)
        view = if (messageModel.userID == chatFrame.getloggedID()) {
            chatFrame.layoutInflater.inflate(R.layout.layout_msg_user, viewGroup, false)
        } else {
            chatFrame.layoutInflater.inflate(R.layout.layout_msg_friend, viewGroup, false)
        }

        populateView(view, messageModel, position)

        return view
    }

    override fun getViewTypeCount(): Int {
        //kembalikan jumlah total jenis tampilan saat runtime
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return position % 2
    }
}
