// Co-authored by Claude Code
/**
 * Project-related MCP tools for querying project data from Firestore.
 * All functions verify user membership before returning data.
 */
import * as admin from 'firebase-admin';
import { serializeDoc, NotFoundError, ForbiddenError } from '../utils';

/**
 * Lists all projects where the user is a member.
 * @param userId - The authenticated user's ID
 * @returns Array of project objects the user belongs to
 */
export async function listProjectsForUser(userId: string): Promise<object[]> {
  const projectsSnapshot = await admin
    .firestore()
    .collection('projects')
    .where('memberIds', 'array-contains', userId)
    .get();
  return projectsSnapshot.docs.map(serializeDoc);
}

/**
 * Retrieves a specific project by ID if the user is a member.
 * @param projectId - The project ID to retrieve
 * @param userId - The authenticated user's ID
 * @returns The project object
 * @throws NotFoundError if the project doesn't exist
 * @throws ForbiddenError if the user is not a member
 */
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
    throw new NotFoundError('Project not found');
  }

  const projectData = projectDoc.data()!;
  if (!projectData.memberIds?.includes(userId)) {
    throw new ForbiddenError('Not a member of this project');
  }

  return serializeDoc(projectDoc);
}

/**
 * Lists all members of a specific project.
 * @param projectId - The project ID
 * @param userId - The authenticated user's ID (must be a member)
 * @returns Array of member objects
 * @throws NotFoundError if the project doesn't exist
 * @throws ForbiddenError if the user is not a member
 */
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
    throw new NotFoundError('Project not found');
  }

  if (!projectDoc.data()?.memberIds?.includes(userId)) {
    throw new ForbiddenError('Not a member of this project');
  }

  const membersSnapshot = await admin
    .firestore()
    .collection('projects')
    .doc(projectId)
    .collection('members')
    .get();

  return membersSnapshot.docs.map(serializeDoc);
}
