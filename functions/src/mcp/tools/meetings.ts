// Co-authored by Claude Code
/**
 * Meeting-related MCP tools for querying meeting data from Firestore.
 * All functions verify user membership before returning data.
 */
import * as admin from 'firebase-admin';
import { serializeDoc, NotFoundError, ForbiddenError } from '../utils';

/**
 * Verifies that a user is a member of the specified project.
 * @param projectId - The project ID to check
 * @param userId - The user ID to verify membership for
 * @throws NotFoundError if the project doesn't exist
 * @throws ForbiddenError if the user is not a member
 */
async function verifyProjectMembership(
  projectId: string,
  userId: string
): Promise<void> {
  const projectDoc = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .get();

  if (!projectDoc.exists) {
    throw new NotFoundError('Project not found');
  }

  if (!projectDoc.data()?.memberIds?.includes(userId)) {
    throw new ForbiddenError('Not a member of this project');
  }
}

/**
 * Lists all meetings in a project.
 * @param projectId - The project ID
 * @param userId - The authenticated user's ID (must be a member)
 * @returns Array of meeting objects
 * @throws NotFoundError if the project doesn't exist
 * @throws ForbiddenError if the user is not a member
 */
export async function listMeetingsForProject(
  projectId: string,
  userId: string
): Promise<object[]> {
  await verifyProjectMembership(projectId, userId);

  const meetingsSnapshot = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .collection('meetings')
    .get();

  return meetingsSnapshot.docs.map(serializeDoc);
}

/**
 * Retrieves a specific meeting by ID.
 * @param projectId - The project ID
 * @param meetingId - The meeting ID
 * @param userId - The authenticated user's ID (must be a member)
 * @returns The meeting object
 * @throws NotFoundError if the project or meeting doesn't exist
 * @throws ForbiddenError if the user is not a member
 */
export async function getMeetingForProject(
  projectId: string,
  meetingId: string,
  userId: string
): Promise<object> {
  await verifyProjectMembership(projectId, userId);

  const meetingDoc = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .collection('meetings')
    .doc(meetingId)
    .get();

  if (!meetingDoc.exists) {
    throw new NotFoundError('Meeting not found');
  }

  return serializeDoc(meetingDoc);
}
