const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyNewMessage = 
    functions.firestore
            .document(`Messages/{senderId}/{receiverId}/{messageId}`)
            .onCreate((documentSnapshot, context) => {
                // get document Id from context
                const messageData = documentSnapshot.data();
                const message = messageData['message'];
                const recipientId = context.params.receiverId;
                const senderId = context.params.senderId;
                const messageId = context.params.messageId;
                const senderName = messageData['senderName'];
                console.log(`senderName=${senderName},  recipientId=${recipientId},  message=${message}, 
                 senderId=${senderId}, messageId=${messageId}
                 messageData:${messageData}`);

                var messageRef = admin.firestore().doc('Users/' + recipientId);
                console.log(`messageRef=${messageRef}`)

                return admin.firestore().doc(`Users/${recipientId}`)
                            .get()
                            .then(userDoc =>{
                                 const registrationTokens = userDoc.get('registrationTokens')
                                 const notificationBody = "You received new messages"

                                 const payload = {
                                    notification: {
                                        title: senderName + " New Message",
                                        body: notificationBody,
                                        clickAction: "ChatActivity"
                                    },
                                    data: {
                                        USER_NAME: senderName,
                                        USER_ID: senderId
                                    }
                                }

                                return admin.messaging().sendToDevice(registrationTokens, payload)
                                            .then(response => {
                                                // delete those non-response device
                                                const stillRegisteredTokens = registrationTokens
                                                
                                                response.results.forEach((result, index) => {
                                                    const error = result.error
                                                    if (error){
                                                        const failedRegistrationToken = registrationTokens[index]
                                                        console.error("failed registrationToken: ", failedRegistrationToken, error)
                                                        if (error.code === 'message/invalid-registration-token' ||
                                                                error.code === 'messaging/registration-token-not-registered'){
                                                                    const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken)
                                                                    if(failedIndex > -1) {
                                                                        // delete the element from the array
                                                                        stillRegisteredTokens.splice(failedIndex, 1)
                                                                    }
                                                                }
                                                    }
                                                });

                                                return admin.firestore()
                                                            .doc(`Users/{recipientId}`)
                                                            .set({registrationTokens: stillRegisteredTokens}, {merge: true})
                                                            // .update({registrationTokens: stillRegisteredTokens})
                                            })
                            })
            })

