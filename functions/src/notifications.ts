import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';


async function getMemberIdsFcmToken(participantIds: string[]): Promise<string[]> {
    try{
        const tokens: string[] = [];
        const usersSnapshot = await admin.firestore().collection("users").where("uid", "in", participantIds).get();

        usersSnapshot.forEach(user => {
            const data = user.data();
            if(data.fcmToken) {
                tokens.push(data.fcmToken);
            }
        });
        return tokens;
    }catch {
        return []
    }
}

export const sendOnScheduleMeetingUpdate =
    functions.firestore.document("projects/{projectId}/meetings/{meetingId}").onUpdate(async (change, context) => {
        try{
            const meeting = change.after.data();
            const previousMeeting = change.before.data();
            if (JSON.stringify(meeting) === JSON.stringify(previousMeeting)) return;

            if(meeting.status == "SCHEDULED" && previousMeeting.status != "SCHEDULED"){
                const fcmTokens = await getMemberIdsFcmToken(meeting.participantIds); 
                const payload = {
                    data: {
                        title: "Meeting Updated",
                        body: "Your meeting: " + meeting.title + " has been scheduled!",
                        type: "meeting",
                        id: meeting.meetingID
                    }   
            }
            if(fcmTokens.length == 0 || !fcmTokens){
                    return;
            } 
            await admin.messaging().sendToDevice(fcmTokens, payload);
            console.log(`Notification sent to ${fcmTokens.length} participants.`);
        }
        }catch(error){
            console.error('Error sending meeting notification:', error);
        }
    })

export const sendMeetingReminder =
    functions.pubsub.schedule("every 1 minutes").onRun(async (context) => {
        const now = admin.firestore.Timestamp.now();
        const reminderTime = admin.firestore.Timestamp.fromMillis(now.toMillis() + 10 * 60 * 1000);

        const meetingsDocs = (await admin.firestore().collectionGroup("meetings")
        .where("datetime", '<=', reminderTime).where("datetime", ">", now).get()).docs;

        const tasks = meetingsDocs.map(async (doc) => {
        const meeting = doc.data();
        const participantTokens = await getMemberIdsFcmToken(
            meeting.participantIds
        );

        if (!participantTokens || participantTokens.length === 0) return;

        const payload = {
            data: {
            title: "Meeting Reminder",
            body: `You have the meeting ${meeting.title} in 10 minutes!`,
            type: "meeting",
            id: meeting.meetingID,
            },
        };

        return admin.messaging().sendToDevice(participantTokens, payload);
        });

        await Promise.all(tasks);
    });

export const sendNewMessageNotification =
    functions.firestore.document("projects/{projectId}/chat_channels/{channelId}/messages/{messageId}").onCreate(async (snapshot, context) => {
        const message = snapshot.data();
        const {projectId, channelId} = context.params;
        const senderId = message.senderId;
        const text = message.text || "";
        const messageId = message.messageId;

        const projectSnap = await admin.firestore().collection("projects").doc(projectId).get();

        if(!projectSnap.exists) return;

        const projectData = projectSnap.data();
        const memberIds: string[] = projectData!.memberIds;

        const senderDoc = (await admin.firestore().collection("users").doc(senderId).get());
        const senderName = senderDoc.data()?.displayName ?? "Unknown user";

        const recipients = memberIds.filter(uid => uid != senderId);

        const fcmTokens = await getMemberIdsFcmToken(recipients);

        const payload = {
            data: {
            title: "Chat " + projectData!.name,
            body: senderName + ": " + text,
            type: "message",
            id: messageId,
            },
        };

        await admin.messaging().sendToDevice(fcmTokens, payload);
    })

