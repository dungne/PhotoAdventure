const functions = require('firebase-functions');
const admin = require('firebase-admin')
admin.initializeApp(functions.config().firebase)

exports.makeLowercase = functions.database.ref('/Photos/{pushId}/name')
    .onWrite(event => {
        const original = event.data.val();
        console.log('lowercasing', event.params.pushId, original)
        const lowercase = original.toLowerCase();

        return event.data.ref.parent.child('lowercase').set(lowercase);
    });



// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
