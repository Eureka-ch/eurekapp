// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import { serializeDoc } from '../handlers';

export async function getUserById(userId: string): Promise<object> {
  const userDoc = await admin.firestore().collection('users').doc(userId).get();

  if (!userDoc.exists) {
    throw new Error('User not found');
  }

  return serializeDoc(userDoc);
}
