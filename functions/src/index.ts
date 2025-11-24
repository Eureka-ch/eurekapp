/**
 * Firebase Cloud Functions entry point
 */

import * as admin from 'firebase-admin';

admin.initializeApp();

export {transcribeAudio} from './transcription';
export {sendOnScheduleMeetingUpdate, sendMeetingReminder, sendNewMessageNotification} from './notifications';
