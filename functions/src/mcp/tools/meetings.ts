// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import { serializeDoc } from '../utils';

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
    throw new Error('Project not found');
  }

  if (!projectDoc.data()?.memberIds?.includes(userId)) {
    throw new Error('Not a member of this project');
  }
}

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
    throw new Error('Meeting not found');
  }

  return serializeDoc(meetingDoc);
}
