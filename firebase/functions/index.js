/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

const TIMEOUT_IN_SECONDS = 10;

// TODO(yeni api yi kullanarak yaz bunu)
const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");


// TODO(Kullanıcı authla eklendiğinde otomatik olarak database e de ekle şu an clienttan ekliyor fakat bunu bu şekilde server fonksiyonu olarak ekle)
/*
import { getDatabase, ref, set } from "firebase/database";

function writeUserData(userId, name, email, imageUrl) {
  const db = getDatabase();
  set(ref(db, 'users/' + userId), {
    username: name,
    email: email,
    profile_picture : imageUrl
  });
}
*/


admin.initializeApp();

// Eğer tanımlanan zaman aralığına kadar cevap oluşmazsa PENDING -> TIMEOUT oluyor
exports.checkCallTimeout = functions.database
  .ref('/calls/{callId}/status')
  .onCreate(async (snapshot, context) => {
    const status = snapshot.val();
    const callId = context.params.callId;
    //const timestampServer = admin.database.ServerValue.TIMESTAMP || { '.sv': 'timestamp' };
    const timestampServer = { '.sv': 'timestamp' };

	await admin.database().ref(`/calls/${callId}`).update({
	  timestamp: timestampServer
	}); 

    //console.log(`Call ${callId} status created with value:  ${status}`);

    if (status !== "PENDING") {
      console.log("Status is not PENDING, exiting early");
      return null;
    }

    console.log("Waiting for timeout...");
    await new Promise(resolve => setTimeout(resolve, TIMEOUT_IN_SECONDS * 1000));
    console.log("Timeout elapsed, checking status again...");

    const statusSnapshot = await admin.database().ref(`/calls/${callId}/status`).once("value");
    const currentStatus = statusSnapshot.val();

    if (currentStatus === "PENDING") {
      await admin.database().ref(`/calls/${callId}`).update({
        status: "TIMEOUT",
      });
      ////console.log(`Call ${callId} timed out with server timestamp.`);
    }

    return null;
  });


// Çağrı sonuç oluşturduğunda TIMEOUT, ENDED, REJECTED çağrıyla ilgili webrtc tablosu, eğer varsa, siliniyor
exports.cleanUpWebrtcData = functions.database
  .ref('/calls/{callId}/status')
  .onUpdate(async (change, context) => {
    const after = change.after.val();
    const callId = context.params.callId;

    const shouldCleanup = ["TIMEOUT", "ENDED", "REJECTED"].includes(after);
    if (!shouldCleanup) return null;

    const webrtcRef = admin.database().ref(`/webrtc/${callId}`);

    try {
      const snapshot = await webrtcRef.once("value");

      if (snapshot.exists()) {
        console.log(`Deleting webrtc/${callId} because call ended with status: ${after}`);
        await webrtcRef.remove();
      } else {
        console.log(`No webrtc data found for call ${callId}, nothing to delete.`);
      }

    } catch (error) {
      console.error(`Error checking/removing webrtc/${callId}:`, error);
    }

    return null;
  });
  

// Auth modülüyle yeni bir kullanıcı eklendiğinde realtime database e de ekleniyor
// Bu fikir uğraştırıcı
/*
exports.addUserToDatabase = functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;
  const email = user.email || "";
  const displayName = user.displayName || "";
  //const photoURL = user.photoURL || "";
  
  const str = JSON.stringify(user, null, 4); // (Optional) beautiful indented output.
  console.log(str)
	
  const userData = {
    uid: uid,
    email: email,
    name: displayName,
    //photoURL: photoURL,
    //createdAt: admin.database.ServerValue.TIMESTAMP,
  };

  try {
    await admin.database().ref(`users/${uid}`).set(userData);
    console.log(`User ${uid} added to Realtime Database`);
  } catch (error) {
    console.error(`Failed to add user ${uid} to database:`, error);
  }

  return null;
});
*/


