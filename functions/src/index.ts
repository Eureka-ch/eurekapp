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

// MCP Server functions
export { mcpCreateToken, mcpRevokeToken, mcpListTokens } from './mcp/tokens';
export {
  mcpListProjects,
  mcpGetProject,
  mcpListProjectMembers,
  mcpGetUser,
} from './mcp/handlers';
