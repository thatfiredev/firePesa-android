package io.github.rosariopfernandes.firepesa.ktx

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.HttpsCallableResult
import io.github.rosariopfernandes.firepesa.TransactionResponse
import java.lang.Exception

inline fun Task<HttpsCallableResult>.then(crossinline action:
       (response:TransactionResponse)->Unit):
        Task<HttpsCallableResult> {
    continueWith({
        task ->
        var map = task.result.data as HashMap<String, String>
        var response= TransactionResponse(
                map["code"]!!,
                map["description"]!!,
                map["transactionId"]!!,
                map["conversationId"]!!,
                map["transactionStatus"]!!
        )
        action(response)
    })
    return this
}

inline fun Task<HttpsCallableResult>.catch(crossinline action:
       (error:Exception)->Unit){
    addOnCompleteListener({
        task ->
        if(!task.isSuccessful)
            action(task.exception!!)
    })
}