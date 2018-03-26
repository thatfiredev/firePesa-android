package io.github.rosariopfernandes.firepesasampleapp.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by rosariopfernandes on 3/26/18.
 */
class NotificationService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Log.e("onMessage", message.toString())
    }
}