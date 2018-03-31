const axios = require('axios');
const constants = require("constants");
const crypto = require("crypto");
const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);
const config = functions.config().mpesa;

const BASE_URL  = "http://10.201.239.73:18346/ipg/"; //TODO: Load the baseUrl from config

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
            var instanceId = context.instanceIdToken;
            const message = {
                notification:{
                    title: payload.fcmTitle ? payload.fcmTitle : "Payment Confirmed",
                    body: payload.fcmBody ? payload.fcmBody : ("We've received "
                        + payload.amount + "MZN from "+ payload.msisdn)
                }
            };
            admin.messaging().sendToDevice(instanceId, message)
                .then(function () {
                    console.log("Message Sent");
                    return response.data;
                    //return response.data;
                })
                .catch(function (error) {
                    console.log(error);
                    return error.data;
                });
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
            var instanceId = context.instanceIdToken;
            const message = {
                notification:{
                    title: payload.fcmTitle ? payload.fcmTitle : "Refund Confirmed",
                    body: payload.fcmBody ? payload.fcmBody : ("We have refunded " +
                        payload.amount + "MZN to "+ payload.msisdn)
                }
            };
            admin.messaging().sendToDevice(instanceId, message)
                .then(function () {
                    return response.data;
                })
                .catch(function (error) {
                    return error.data;
                });
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
    console.log(token);
    resp.status(200).send(token);
});

exports.paymentTest = functions.https.onCall(function (payload, context){
    var uid = context.auth.uid;
    console.log(payload);
    var instanceId = context.instanceIdToken;
    const message = {
        notification:{
            title: payload.fcmTitle ? payload.fcmTitle : "Payment Confirmed",
            body: payload.fcmBody ? payload.fcmBody : ("We've received "
                + payload.amount + "MZN from "+ payload.msisdn)
        }
    };
    admin.messaging().sendToDevice(instanceId, message).then(function () {
        return {
            output_responseCode:'INS-0',
            output_ResponseDesc:'Request processed successfully',
            output_TransactionID:'5C2300CVWN',
            output_ConversationID:'5a993e2977f9a66c027bfa68',
            output_ResponseTransactionStatus:'Paid'
        };
    }).catch(function (error) {
        console.log(error.data);
    });
});

exports.refundTest = functions.https.onCall(function (payload, context){
    var uid = context.auth.uid;
    console.log(payload);
    var instanceId = context.instanceIdToken;
    const message = {
        notification:{
            title: payload.fcmTitle ? payload.fcmTitle : "Refund Confirmed",
            body: payload.fcmBody ? payload.fcmBody : ("We have refunded " +
                payload.amount + "MZN to "+ payload.msisdn)
        }
    };
    admin.messaging().sendToDevice(instanceId, message).then(function () {
        return {
            output_responseCode:'INS-0',
            output_ResponseDesc:'Request processed successfully',
            output_TransactionID:'5C2300CVWN',
            output_ConversationID:'5a993e2977f9a66c027bfa68',
            output_ResponseTransactionStatus:'Refunded'
        };
    }).catch(function (error) {
        console.log(error.data);
    });
});

function getBearerToken()
{
    var publicKey = config.publickey;
    var buffer = new Buffer(config.apikey);
    var encrypted = crypto.publicEncrypt({"key" : publicKey, padding : constants.RSA_PKCS1_PADDING}, buffer);
    return encrypted.toString("base64");
}