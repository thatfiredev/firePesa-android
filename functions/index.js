const functions = require('firebase-functions');
/*const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);*/

const axios = require('axios');
const BASE_URL  = "http://10.201.239.73:18346/ipg/";
const config = functions.config().mpesa;

exports.payment = functions.https.onCall( function(payload, context){
    var uid = context.auth.uid;
    axios.post(BASE_URL + "c2bpayment/",
        {
            "input_ServiceProviderCode": config.serviceProviderCode,
            "input_CustomerMSISDN": payload.msisdn,
            "input_Amount": Math.round(payload.amount * 100) / 100,
            "input_TransactionReference": payload.transactionReference,
            "input_ThirdPartyReference": payload.thirdPartyReference
        },
        {
            headers:{
                'Content-Type': ' application/json',
                'Content-Length': 5,
                'Authorization':bearer
            }
        })
        .then(function (response) {
            console.log(response.data);
            return response.data;
        })
        .catch(function(error){
            console.log(error.data);
            return error.data;
        });
});

exports.refund = functions.https.onCall( function(payload, context){
    var uid = context.auth.uid;
    axios.post(BASE_URL + "reversal/",
        {
            "input_ServiceProviderCode": config.serviceProviderCode,
            "input_Amount": Math.round(payload.amount * 100) / 100,
            "input_InitiatorIdentifier": config.initiatorIdentifier,
            "input_SecurityCredential": config.securityCredential,
            "input_TransactionID": payload.transactionId
        },
        {
            headers:{
                'Content-Type': ' application/json',
                'Content-Length': 5,
                'Authorization':bearer
            }
        })
        .then(function (response) {
            console.log(response.data);
            return response.data;
        })
        .catch(function(error){
            console.log(error.data);
            return error.data;
        });
});

exports.query = functions.https.onCall( function(payload, context){
    var uid = context.auth.uid;
    axios.post(BASE_URL + "queryTxn/",
        {
            "input_ServiceProviderCode": config.serviceProviderCode,
            "input_InitiatorIdentifier": config.initiatorIdentifier,
            "input_SecurityCredential": config.securityCredential,
            "input_QueryReference": payload.queryReference
        },
        {
            headers:{
                'Content-Type': ' application/json',
                'Authorization':bearer
            }
        })
        .then(function (response) {
            console.log(response.data);
            return response.data;
        })
        .catch(function(error){
            console.log(error.data);
            return error.data;
        });

});