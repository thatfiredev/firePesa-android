package io.github.rosariopfernandes.firepesa

data class TransactionResponse(var code:String, var description:String, var transactionId:String,
                               var conversationId:String, var transactionStatus:String)