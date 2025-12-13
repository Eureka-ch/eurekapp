// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import { serializeDoc } from '../handlers';

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

export async function listTasksForProject(
  projectId: string,
  userId: string
): Promise<object[]> {
  await verifyProjectMembership(projectId, userId);

  const tasksSnapshot = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .collection('tasks')
    .get();

  return tasksSnapshot.docs.map(serializeDoc);
}

export async function getTaskForProject(
  projectId: string,
  taskId: string,
  userId: string
): Promise<object> {
  await verifyProjectMembership(projectId, userId);

  const taskDoc = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .collection('tasks')
    .doc(taskId)
    .get();

  if (!taskDoc.exists) {
    throw new Error('Task not found');
  }

  return serializeDoc(taskDoc);
}
