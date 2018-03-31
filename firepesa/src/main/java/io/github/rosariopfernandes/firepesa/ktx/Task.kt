/*
 *MIT License
 *
 *Copyright (c) 2018 Rosário Pereira Fernandes
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software and associated documentation files (the "Software"), to deal
 *in the Software without restriction, including without limitation the rights
 *to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *copies of the Software, and to permit persons to whom the Software is
 *furnished to do so, subject to the following conditions:
 *
 *The above copyright notice and this permission notice shall be included in all
 *copies or substantial portions of the Software.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *SOFTWARE.
 */
package io.github.rosariopfernandes.firepesa.ktx

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.HttpsCallableResult
import io.github.rosariopfernandes.firepesa.TransactionResponse
import java.lang.Exception

/**
 * This method will be called when the server replies to
 * the sent Transaction
 * @param response Response from the server
 * @return Task<HttpsCallableResult> from the Cloud Functions SDK
 */
inline fun Task<HttpsCallableResult>.then(crossinline action:
       (response:TransactionResponse)->Unit):
        Task<HttpsCallableResult> {
    continueWith({
        task ->
        val map = task.result.data as HashMap<String, String>
        val response= TransactionResponse(
                map["output_ResponseCode"]!!,
                map["output_ResponseDesc"]!!,
                map["output_TransactionID"]!!,
                map["output_ConversationID"]!!,
                map["output_ResponseTransactionStatus"]!!
        )
        action(response)
    })
    return this
}

/**
 * This method will be called if an Exception was thrown on the server-side
 * @param error Exception caught
 */
inline fun Task<HttpsCallableResult>.catch(crossinline action:
       (error:Exception)->Unit){
    addOnCompleteListener({
        task ->
        if(!task.isSuccessful)
            action(task.exception!!)
    })
}