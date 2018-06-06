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

import android.content.Context
import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import io.github.rosariopfernandes.firepesa.transactions.Payment
import io.github.rosariopfernandes.firepesa.transactions.Reversal

/**
 * mPesa API Transaction
 * @author Rosário Pereira Fernandes
 */
class Transaction(context: Context) {
    val functions = FirebaseFunctions.getInstance()
    val analytics:FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    val transactionsRef = FirebaseDatabase.getInstance()
            .getReference("firePesa/transactions")
    private var payload : HashMap<String, Any> = HashMap()
    private var bundle = Bundle()

    /**
     * Initiates a payment Transaction on the mPesa API
     * @param msisdn Subscriber Number. Example: 841234567
     * @param amount Amount being charged (currency:MZN)
     * @param reference
     * @param thirdPartyReference
     * @return DatabaseReference of the new payment's result
     */
    fun payment(msisdn:String, amount:Float, reference:String,
                thirdPartyReference:String): DatabaseReference {
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null)
            throw FirebaseAuthRecentLoginRequiredException("ERROR_USER_NOT_FOUND",
                        "Please sign in before starting a payment")
        else{
            val paymentsRef = transactionsRef.child("payments")
            val payment = Payment("+258$msisdn", "$amount", reference,
                    thirdPartyReference, user.uid, 0)
            val paymentKey = paymentsRef.push().key
            paymentsRef.child(paymentKey).setValue(payment)
            paymentsRef.child(paymentKey).child("timestamp")
                    .setValue(ServerValue.TIMESTAMP)
            bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.VALUE, "$amount")
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "MZN")
            analytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle)
            return transactionsRef.child("results").child(paymentKey)
        }
    }

    /**
     * Initiates a reversal (refund) Transaction on the mPesa API
     * @param transactionId ID of the transaction to be reversed
     * @param amount Amount being returned (currency:MZN)
     * @return DatabaseReference of the refund's result
     */
    fun refund(transactionId:String, amount:Float): DatabaseReference {
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null)
            throw FirebaseAuthRecentLoginRequiredException("ERROR_USER_NOT_FOUND",
                    "Please sign in before ordering a refund")
        else{
            val reversalsRef = transactionsRef.child("reversals")
            val reversal = Reversal(transactionId, amount,
                    FirebaseAuth.getInstance().currentUser!!.uid, 0)
            val reversalKey = reversalsRef.push().key
            reversalsRef.child(reversalKey).setValue(reversal)
            reversalsRef.child(reversalKey).child("timestamp")
                    .setValue(ServerValue.TIMESTAMP)
            bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.VALUE, "$amount")
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "MZN")
            bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, transactionId)
            analytics.logEvent(FirebaseAnalytics.Event.PURCHASE_REFUND, bundle)
            return transactionsRef.child("results").child(reversalKey)
        }
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