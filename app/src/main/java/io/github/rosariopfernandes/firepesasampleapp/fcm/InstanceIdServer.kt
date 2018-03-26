package io.github.rosariopfernandes.firepesasampleapp.fcm

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by rosariopfernandes on 3/26/18.
 */
class InstanceIdServer:FirebaseInstanceIdService(){
    override fun onTokenRefresh() {
        super.onTokenRefresh()
        Log.e("tokenRefresh", FirebaseInstanceId.getInstance().token)
    }
}