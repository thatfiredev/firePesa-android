const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const axios = require('axios');
const BASE_URL  = "http://10.201.239.73:18346/ipg/";
const config = functions.config().mpesa;

const constants = require("constants");
const crypto = require("crypto");

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
                'Content-Type': 'application/json',
                'Content-Length': 5,
                'Authorization': 'Bearer '+getBearerToken()
            }
        })
        .then(function (response) {
            console.log(response.data);
            //TODO: Check the response to see if the request was successful before sending the notification
            var tokenRef = admin.database.ref('consumers/'+uid+'fcmToken');
            tokenRef.once('value', function(snapshot){
                var instanceId = snapshot.val();
                const message = {
                    notification:{
                        title: "Payment Confirmed",
                        body: "We confirm that we received " + payload.amount +
                                " from "+ payload.msisdn
                    }
                };
                admin.messaging().sendToDevice(instanceId, message)
                    .then(function () {
                        return response.data;
                    })
                    .catch(function (error) {
                        return error.data;
                    });
            });
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
                'Content-Type': 'application/json',
                'Content-Length': 5,
                'Authorization': 'Bearer '+getBearerToken()
            }
        })
        .then(function (response) {
            console.log(response.data);
            //TODO: Check the response to see if the request was successful before sending the notification
            var tokenRef = admin.database.ref('consumers/'+uid+'fcmToken');
            tokenRef.once('value', function(snapshot){
                var instanceId = snapshot.val();
                const message = {
                    notification:{
                        title: "Refund Confirmed",
                        body: "We have refunded " + payload.amount +
                        " to "+ payload.msisdn
                    }
                };
                admin.messaging().sendToDevice(instanceId, message)
                    .then(function () {
                        return response.data;
                    })
                    .catch(function (error) {
                        return error.data;
                    });
            });
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
                'Content-Type': 'application/json',
                'Authorization': 'Bearer '+getBearerToken()
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

exports.testBear = functions.https.onRequest(function (req, resp) {
    var token = getBearerToken();
    resp.status(200).send(token);
});

function getBearerToken()
{
    var publicKey = config.publickey;
    var buffer = new Buffer(config.apikey);
    var encrypted = crypto.publicEncrypt({"key" : publicKey, padding : constants.RSA_PKCS1_PADDING}, buffer);
    return encrypted.toString("base64");
}