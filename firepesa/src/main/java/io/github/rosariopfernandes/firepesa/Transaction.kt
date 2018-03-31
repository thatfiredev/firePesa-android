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
package io.github.rosariopfernandes.firepesa

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult

/**
 * mPesa API Transaction
 * @author Rosário Pereira Fernandes
 */
class Transaction {
    val functions = FirebaseFunctions.getInstance()
    private val payload : HashMap<String, Any> = HashMap()

    /**
     * Initiates a payment Transaction on the mPesa API
     * @param msisdn Subscriber Number. Example: 841234567
     * @param amount Amount being charged (currency:MZN)
     * @param reference
     * @param thirdPartyReference
     * @return Task<HttpsCallableResult> from the Cloud Functions SDK
     */
    fun payment(msisdn:String, amount:Float, reference:String,
                thirdPartyReference:String): Task<HttpsCallableResult> {
        payload.put("msisdn", "+258$msisdn")
        payload.put("amount", "$amount")
        payload.put("transactionReference", reference)
        payload.put("thirdPartyReference", thirdPartyReference)
        return functions.getHttpsCallable("testPayment")
                .call(payload)
    }

    /**
     * Initiates a reversal (refund) Transaction on the mPesa API
     * @param transactionId ID of the transaction to be reversed
     * @param amount Amount being returned (currency:MZN)
     * @return Task<HttpsCallableResult> from the Cloud Functions SDK
     */
    fun refund(transactionId:String, amount:Float): Task<HttpsCallableResult> {
        payload.put("transactionID", transactionId)
        payload.put("amount", "$amount")
        return functions.getHttpsCallable("refund")
                .call(payload)
    }

    /**
     * Initiates a Query Transaction on the mPesa API
     * @param queryReference
     * @return Task<HttpsCallableResult> from the Cloud Functions SDK
     */
    fun query(queryReference:String): Task<HttpsCallableResult> {
        payload.put("input_QueryReference", queryReference)
        return functions.getHttpsCallable("query")
                .call(payload)
    }

    /**
     * Replaces the default Notification with a custom one
     * @param title Custom notification title
     * @param body Custom notification body(content)
     */
    fun addNotification(title:String, body:String){
        payload.put("fcmTitle", title)
        payload.put("fcmBody", body)
    }
}