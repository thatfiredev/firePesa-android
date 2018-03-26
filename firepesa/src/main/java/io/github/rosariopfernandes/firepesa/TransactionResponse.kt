package io.github.rosariopfernandes.firepesa

/**
 * Created by rosariopfernandes on 3/26/18.
 */
data class TransactionResponse(var code:String, var description:String, var transactionId:String,
                               var conversationId:String, var transactionStatus:String)