// Co-authored by Claude Code
/**
 * User-related MCP tools for querying user data from Firestore.
 */
import * as admin from 'firebase-admin';
import { serializeDoc, NotFoundError } from '../utils';

/**
 * Retrieves a user by their ID.
 * @param userId - The ID of the user to retrieve
 * @returns The user object
 * @throws NotFoundError if the user doesn't exist
 */
export async function getUserById(userId: string): Promise<object> {
  const userDoc = await admin.firestore().collection('users').doc(userId).get();

  if (!userDoc.exists) {
    throw new NotFoundError('User not found');
  }

  return serializeDoc(userDoc);
}
