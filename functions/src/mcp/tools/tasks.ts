// Co-authored by Claude Code
/**
 * Task-related MCP tools for querying task data from Firestore.
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
 * Lists all tasks in a project.
 * @param projectId - The project ID
 * @param userId - The authenticated user's ID (must be a member)
 * @returns Array of task objects
 * @throws NotFoundError if the project doesn't exist
 * @throws ForbiddenError if the user is not a member
 */
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

/**
 * Retrieves a specific task by ID.
 * @param projectId - The project ID
 * @param taskId - The task ID
 * @param userId - The authenticated user's ID (must be a member)
 * @returns The task object
 * @throws NotFoundError if the project or task doesn't exist
 * @throws ForbiddenError if the user is not a member
 */
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
    throw new NotFoundError('Task not found');
  }

  return serializeDoc(taskDoc);
}
