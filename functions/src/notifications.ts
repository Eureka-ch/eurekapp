//Portions of this code were generated using the help of Claude Sonnet 4.5
import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

enum UserNotificationSettingsKeys {
  ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE = "ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE",
  ON_MEETING_OPEN_TO_VOTES_NOTIFY = "ON_MEETING_OPEN_TO_VOTES_NOTIFY",
  ON_MEETING_SCHEDULED_NOTIFY = "ON_MEETING_SCHEDULED_NOTIFY",
  ON_NEW_MESSAGE_NOTIFY = "ON_NEW_MESSAGE_NOTIFY"
}

enum MeetingStatus {
  OPEN_TO_VOTES = "OPEN_TO_VOTES",
  SCHEDULED = "SCHEDULED",
  IN_PROGRESS = "IN_PROGRESS",
  COMPLETED = "COMPLETED"
}

// Ensure admin is initialized with explicit project ID
if (!admin.apps.length) {
    admin.initializeApp({
        projectId: 'eureka-app-ch'
    });
}

async function getUserNotificationPreferences(userSnapshot: admin.firestore.DocumentSnapshot): Promise<Record<UserNotificationSettingsKeys, boolean>> {

    try {
        if (!userSnapshot.exists) {
            functions.logger.warn(`User not found when reading notification settings.`);
            
            // Return default preferences (all TRUE or FALSE — choose what fits your app)
            return {
                [UserNotificationSettingsKeys.ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE]: true,
                [UserNotificationSettingsKeys.ON_MEETING_OPEN_TO_VOTES_NOTIFY]: true,
                [UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY]: true,
                [UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY]: true
            };
        }
        const userDoc = userSnapshot.data()!!;

        const data = userDoc;
        const storedSettings = data?.notificationSettings || {};

        // Build final map ensuring all keys exist
        const preferences: Record<UserNotificationSettingsKeys, boolean> = {
            [UserNotificationSettingsKeys.ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE]:
                storedSettings[UserNotificationSettingsKeys.ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE] ?? true,

            [UserNotificationSettingsKeys.ON_MEETING_OPEN_TO_VOTES_NOTIFY]:
                storedSettings[UserNotificationSettingsKeys.ON_MEETING_OPEN_TO_VOTES_NOTIFY] ?? true,

            [UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY]:
                storedSettings[UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY] ?? true,

            [UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY]:
                storedSettings[UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY] ?? true
        };

        return preferences;

    } catch (error) {
        functions.logger.error("Error fetching user notification preferences:", error);

        // Fallback defaults (failsafe defaults principle)
        return {
            [UserNotificationSettingsKeys.ON_MEETING_NOTIFY_TEN_MINUTES_BEFORE]: false,
            [UserNotificationSettingsKeys.ON_MEETING_OPEN_TO_VOTES_NOTIFY]: false,
            [UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY]: false,
            [UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY]: false
        };
    }
}

async function sendMeetingUpdatedState(previousMeeting: admin.firestore.DocumentData, newMeeting: admin.firestore.DocumentData, meetingIdMember: string){
    const newStatus = newMeeting.status
    const oldStatus = previousMeeting.status
    if(oldStatus === newStatus){
        return;
    }
    const associatedUserSnapshot = (await admin.firestore().collection("users").doc(meetingIdMember).get());
    const associatedUser = associatedUserSnapshot.data();
    if(associatedUser && associatedUser.fcmToken){
        const userFcmToken = associatedUser.fcmToken;
        const userNotificationPreferences = await getUserNotificationPreferences(associatedUserSnapshot);
        functions.logger.info("User notification preferences: ", userNotificationPreferences);
        if(newStatus === MeetingStatus.SCHEDULED && userNotificationPreferences[UserNotificationSettingsKeys.ON_MEETING_SCHEDULED_NOTIFY]){
            try {
                            await admin.messaging().send({
                                token: userFcmToken,
                                data: {
                                    title: "Meeting Updated",
                                    body: `Your meeting: ${newMeeting.title} has been scheduled!`,
                                    type: "meeting",
                                    id: newMeeting.meetingID || "",
                                    projectId: newMeeting.projectId || ""
                                }
                            });
                }catch(error){
                    functions.logger.warn("Was not able to send scheduled meeting notification to user");
                    return
                }
        }else if(newStatus === MeetingStatus.OPEN_TO_VOTES && userNotificationPreferences[UserNotificationSettingsKeys.ON_MEETING_OPEN_TO_VOTES_NOTIFY]){
            try {
                            await admin.messaging().send({
                                token: userFcmToken,
                                data: {
                                    title: "Meeting Updated",
                                    body: `Your meeting: ${newMeeting.title} is now open to voting!`,
                                    type: "meeting",
                                    id: newMeeting.meetingID || "",
                                    projectId: newMeeting.projectId || ""
                                }
                            });
                }catch(error){
                    functions.logger.warn("Was not able to send on meeting open to votes");
                    return
                }
        }
    }
}

export const sendOnScheduleMeetingUpdate =
    functions.firestore.document("/projects/{projectId}/meetings/{meetingId}")
        .onUpdate(async (change, context) => {
            functions.logger.info("Project ID:", admin.app().options.projectId);
            try {
                const meeting = change.after.data();
                const previousMeeting = change.before.data();
                

                if (!meeting.participantIds || meeting.participantIds.length === 0) {
                        functions.logger.info("No participants in meeting");
                        return;
                }
                const promises = meeting.participantIds.map((participantId: string) => sendMeetingUpdatedState(previousMeeting, meeting, participantId));
                await Promise.allSettled(promises);
            } catch (error) {
            }
        });

export const sendMessageOnMeetingCreation = 
functions.firestore.document("/projects/{projectId}/meetings/{meetingId}")
        .onCreate(async (snapshot, context) => {
            functions.logger.info("Project ID:", admin.app().options.projectId);
            try {
                const meeting = snapshot.data();
                

                if (!meeting.participantIds || meeting.participantIds.length === 0) {
                        functions.logger.info("No participants in meeting");
                        return;
                }
                const promises = meeting.participantIds.map((participantId: string) => sendMeetingUpdatedState({}, meeting, participantId));
                await Promise.allSettled(promises);
            } catch (error) {
            }
        });



async function sendMeetingReminderToUser(meeting: admin.firestore.DocumentSnapshot, meetingIdMember: string, timeToMeetingInMinutes: string){
    const associatedUserSnapshot = (await admin.firestore().collection("users").doc(meetingIdMember).get());
    const associatedUser = associatedUserSnapshot.data();
    const meetingData = meeting.data()!!;
    if(associatedUser && associatedUser.fcmToken){
        const userFcmToken = associatedUser.fcmToken;
        const userNotificationPreferences = await getUserNotificationPreferences(associatedUserSnapshot);
        functions.logger.info("User notification preferences: ", userNotificationPreferences);
        try {
                            await admin.messaging().send({
                            token: userFcmToken,
                            data: {
                                title: "Meeting Reminder",
                                body: `You have the meeting ${meetingData.title} in less than ${timeToMeetingInMinutes} minutes!`,
                                type: "meeting",
                                id: meetingData.meetingID || "",
                                projectId: meetingData.projectId || ""
                            }})
                }catch(error){
                    functions.logger.warn("Was not able to send a meeting reminder to: ", associatedUser.displayName);
                    return
                }
    }
}


export const sendMeetingReminder =
    functions.pubsub.schedule("every 5 minutes").onRun(async (context) => {
        functions.logger.info("🔥 sendMeetingReminder STARTED");
        try {
            const now = admin.firestore.Timestamp.now();
            const reminderTime = admin.firestore.Timestamp.fromMillis(
                now.toMillis() + 10 * 60 * 1000
            );
            const differenceInMinutes = Math.round(
                (reminderTime.toMillis() - now.toMillis()) / (60 * 1000)
            );

            functions.logger.info(`Looking for meetings between ${now.toDate()} and ${reminderTime.toDate()}`);

            // ✅ Use only ONE range filter
            const meetingsDocs = await admin.firestore()
                .collectionGroup("meetings")
                .where("datetime", ">", now)
                .get();

            functions.logger.info(`Query returned ${meetingsDocs.docs.length} future meetings`);

            // ✅ Filter in memory for meetings within the reminder window
            const upcomingMeetings = meetingsDocs.docs.filter(doc => {
                const meetingTime = doc.data().datetime;
                return meetingTime && meetingTime.toMillis() <= reminderTime.toMillis();
            });

            functions.logger.info(`Found ${upcomingMeetings.length} upcoming meetings needing reminders`);

            await Promise.allSettled(upcomingMeetings.map(async doc => {
                const meeting = doc.data();
                if (!meeting.participantIds || meeting.participantIds.length === 0) {
                    return;
                }
                await Promise.allSettled(
                    meeting.participantIds.map((participantId: string) => 
                        sendMeetingReminderToUser(doc, participantId, differenceInMinutes.toString())  // ✅ Pass doc (DocumentSnapshot)
                    )
                );
            }));
            
            functions.logger.info("Meeting reminders sent successfully");
        } catch (error) {
            functions.logger.error('Error in sendMeetingReminder:', error);
        }
    });

export const sendNewMessageNotification =
    functions.firestore.document("conversations/{conversationId}/messages/{messageId}")
        .onCreate(async (snapshot, context) => {
            try {
                const message = snapshot.data();
                const { conversationId } = context.params;
                const senderId = message?.senderId;
                const text = message?.text || "";
                const messageId = message?.messageId;

                if (!senderId) {
                    functions.logger.warn("Message missing senderId");
                    return;
                }

                const conversationSnap = await admin.firestore().collection("conversations").doc(conversationId).get();
                const conversation = conversationSnap.data();
                if(!conversation){
                    return;
                }
                const projectSnap = await admin.firestore().collection("projects").doc(conversation.projectId).get();

                const projectData = projectSnap.data();
                if (!projectData || !projectData.memberIds) {
                    functions.logger.info("No members in conversation");
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

                let successCount = 0;
                let failureCount = 0;

                // Check each recipient's notification preferences
                for (const recipientId of recipients) {
                    try {
                        const recipientDoc = await admin.firestore()
                            .collection("users")
                            .doc(recipientId)
                            .get();
                        
                        const recipientData = recipientDoc.data();
                        if (!recipientData?.fcmToken) {
                            continue;
                        }

                        const preferences = await getUserNotificationPreferences(recipientDoc);
                        
                        if (!preferences[UserNotificationSettingsKeys.ON_NEW_MESSAGE_NOTIFY]) {
                            functions.logger.info(`User ${recipientId} has message notifications disabled`);
                            continue;
                        }

                        await admin.messaging().send({
                            token: recipientData.fcmToken,
                            data: {
                                title: `Chat ${projectData.name}`,
                                body: `${senderName}: ${text}`,
                                type: "message",
                                id: messageId || "",
                                projectId: projectData.projectId || ""
                            }
                        });
                        successCount++;
                    } catch (error: any) {
                        failureCount++;
                        functions.logger.error(`Failed to send notification to ${recipientId}: ${error.message}`);
                    }
                }

                functions.logger.info(`Message notifications: ${successCount} success, ${failureCount} failed`);
            } catch (error) {
                functions.logger.error('Error in sendNewMessageNotification:', error);
            }
        });