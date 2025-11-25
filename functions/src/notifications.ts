//The following code was generated using the help of Claude Sonnet 4.5
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

// Ensure admin is initialized with explicit project ID
if (!admin.apps.length) {
    admin.initializeApp({
        projectId: 'eureka-app-ch'
    });
}

async function getMemberIdsFcmToken(participantIds: string[]): Promise<string[]> {
    try {
        const tokens: string[] = [];
        const usersSnapshot = await admin.firestore()
            .collection("users")
            .where("uid", "in", participantIds)
            .get();

        usersSnapshot.forEach(user => {
            const data = user.data();
            const token = data?.fcmToken;
            
            // Validate token format (FCM tokens are typically 140-200 characters)
            if (token && typeof token === 'string' && token.length > 50) {
                tokens.push(token);
            } else if (token) {
                functions.logger.warn(`Invalid FCM token for user ${user.id}: ${token?.substring(0, 20)}...`);
            }
        });
        
        functions.logger.info(`Retrieved ${tokens.length} valid FCM tokens from ${usersSnapshot.size} users`);
        return tokens;
    } catch (error) {
        functions.logger.error('Error getting participants fcmTokens:', error);
        return [];
    }
}

export const sendOnScheduleMeetingUpdate =
    functions.firestore.document("/projects/{projectId}/meetings/{meetingId}")
        .onUpdate(async (change, context) => {
            functions.logger.info("🚀 NEW CODE - Meeting update triggered");
            functions.logger.info("Project ID:", admin.app().options.projectId);
            try {
                const meeting = change.after.data();
                const previousMeeting = change.before.data();
                
                // Quick exit if nothing changed
                if (JSON.stringify(meeting) === JSON.stringify(previousMeeting)) {
                    functions.logger.info("No changes detected");
                    return;
                }

                // Check if status changed to SCHEDULED
                if (meeting.status === "SCHEDULED" && previousMeeting.status !== "SCHEDULED") {
                    functions.logger.info("Status changed to SCHEDULED");
                    functions.logger.info("Participant IDs:", meeting.participantIds);
                    
                    if (!meeting.participantIds || meeting.participantIds.length === 0) {
                        functions.logger.info("No participants in meeting");
                        return;
                    }

                    const fcmTokens = await getMemberIdsFcmToken(meeting.participantIds);
                    
                    if (!fcmTokens || fcmTokens.length === 0) {
                        functions.logger.info("No FCM tokens found");
                        return;
                    }

                    functions.logger.info(`Found ${fcmTokens.length} FCM tokens`);
                    functions.logger.info("First token length:", fcmTokens[0]?.length);
                    functions.logger.info("Meeting ID:", meeting.meetingID);
                    functions.logger.info("Meeting Title:", meeting.title);

                    // Use send() method (V1 API - one token at a time)
                    functions.logger.info("Attempting to send notifications with send()...");

                    let successCount = 0;
                    let failureCount = 0;

                    for (const token of fcmTokens) {
                        try {
                            const messageId = await admin.messaging().send({
                                token: token,
                                data: {
                                    title: "Meeting Updated",
                                    body: `Your meeting: ${meeting.title} has been scheduled!`,
                                    type: "meeting",
                                    id: meeting.meetingID || "",
                                    projectId: meeting.projectId || ""
                                }
                            });
                            successCount++;
                            functions.logger.info(`✓ Sent successfully. Message ID: ${messageId}`);
                        } catch (error: any) {
                            failureCount++;
                            functions.logger.error(`✗ Failed to send:`, error.message);
                            functions.logger.error("Error code:", error.code);
                        }
                    }

                    functions.logger.info(`Total: ${successCount} success, ${failureCount} failed`);
                }
            } catch (error) {
                functions.logger.error('Meeting data:', change.after.data());
                functions.logger.error('Error sending notification:', error);
            }
        });

export const sendMeetingReminder =
    functions.pubsub.schedule("every 1 minutes").onRun(async (context) => {
        try {
            const now = admin.firestore.Timestamp.now();
            const reminderTime = admin.firestore.Timestamp.fromMillis(
                now.toMillis() + 10 * 60 * 1000
            );

            const meetingsDocs = await admin.firestore()
                .collectionGroup("meetings")
                .where("datetime", '<=', reminderTime)
                .where("datetime", ">", now)
                .get();

            functions.logger.info(`Found ${meetingsDocs.docs.length} upcoming meetings`);

            let totalSuccess = 0;
            let totalFailure = 0;

            for (const doc of meetingsDocs.docs) {
                const meeting = doc.data();

                if (!meeting.participantIds || meeting.participantIds.length === 0) {
                    continue;
                }

                const participantTokens = await getMemberIdsFcmToken(meeting.participantIds);

                if (!participantTokens || participantTokens.length === 0) {
                    continue;
                }

                // Use send() method for each token
                for (const token of participantTokens) {
                    try {
                        await admin.messaging().send({
                            token: token,
                            data: {
                                title: "Meeting Reminder",
                                body: `You have the meeting ${meeting.title} in 10 minutes!`,
                                type: "meeting",
                                id: meeting.meetingID || "",
                                projectId: meeting.projectId || ""
                            }
                        });
                        totalSuccess++;
                    } catch (error: any) {
                        totalFailure++;
                        functions.logger.error(`Failed to send reminder: ${error.message}`);
                    }
                }
            }

            functions.logger.info(`Reminders sent: ${totalSuccess} success, ${totalFailure} failed`);
        } catch (error) {
            functions.logger.error('Error in sendMeetingReminder:', error);
        }
    });

export const sendNewMessageNotification =
    functions.firestore.document("projects/{projectId}/chat_channels/{channelId}/messages/{messageId}")
        .onCreate(async (snapshot, context) => {
            try {
                const message = snapshot.data();
                const { projectId } = context.params;
                const senderId = message.senderId;
                const text = message.text || "";
                const messageId = message.messageId;

                const projectSnap = await admin.firestore()
                    .collection("projects")
                    .doc(projectId)
                    .get();

                if (!projectSnap.exists) {
                    functions.logger.info("Project not found");
                    return;
                }

                const projectData = projectSnap.data();
                if (!projectData || !projectData.memberIds) {
                    functions.logger.info("No members in project");
                    return;
                }

                const memberIds: string[] = projectData.memberIds;
                const recipients = memberIds.filter(uid => uid !== senderId);

                if (recipients.length === 0) {
                    functions.logger.info("No recipients for message");
                    return;
                }

                const senderDoc = await admin.firestore()
                    .collection("users")
                    .doc(senderId)
                    .get();

                const senderName = senderDoc.data()?.displayName ?? "Unknown user";

                const fcmTokens = await getMemberIdsFcmToken(recipients);

                if (!fcmTokens || fcmTokens.length === 0) {
                    functions.logger.info("No FCM tokens for recipients");
                    return;
                }

                // Use send() method for each token
                let successCount = 0;
                let failureCount = 0;

                for (const token of fcmTokens) {
                    try {
                        await admin.messaging().send({
                            token: token,
                            data: {
                                title: `Chat ${projectData.name}`,
                                body: `${senderName}: ${text}`,
                                type: "message",
                                id: messageId,
                            }
                        });
                        successCount++;
                    } catch (error: any) {
                        failureCount++;
                        functions.logger.error(`Failed to send message notification: ${error.message}`);
                    }
                }

                functions.logger.info(`Message notifications: ${successCount} success, ${failureCount} failed`);
            } catch (error) {
                functions.logger.error('Error in sendNewMessageNotification:', error);
            }
        });