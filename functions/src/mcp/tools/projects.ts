// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import { serializeDoc } from '../handlers';

export async function listProjectsForUser(userId: string): Promise<object[]> {
  const projectsSnapshot = await admin
    .firestore()
    .collection('projects')
    .where('memberIds', 'array-contains', userId)
    .get();
  return projectsSnapshot.docs.map(serializeDoc);
}

export async function getProjectForUser(
  projectId: string,
  userId: string
): Promise<object> {
  const projectDoc = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .get();

  if (!projectDoc.exists) {
    throw new Error('Project not found');
  }

  const projectData = projectDoc.data()!;
  if (!projectData.memberIds?.includes(userId)) {
    throw new Error('Not a member of this project');
  }

  return serializeDoc(projectDoc);
}

export async function listProjectMembersForUser(
  projectId: string,
  userId: string
): Promise<object[]> {
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

  const membersSnapshot = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .collection('members')
    .get();

  return membersSnapshot.docs.map(serializeDoc);
}
