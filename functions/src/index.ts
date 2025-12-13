/**
 * Firebase Cloud Functions entry point
 */

import * as admin from 'firebase-admin';

admin.initializeApp();

export { transcribeAudio } from './transcription';
export {
  sendOnScheduleMeetingUpdate,
  sendMeetingReminder,
  sendNewMessageNotification,
  sendMessageOnMeetingCreation,
} from './notifications';

// MCP Protocol Server (called by AI clients like Claude Desktop, Cursor)
export { mcpServer } from './mcp/server';
