// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';

const MCP_TOKENS_COLLECTION = 'mcpTokens';

export interface McpAuthResult {
  userId: string;
  tokenId: string;
}

export async function validateMcpToken(
  request: functions.https.Request
): Promise<McpAuthResult> {
  const authHeader = request.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'Missing or invalid Authorization header'
    );
  }

  const token = authHeader.substring(7);

  if (!token || token.length < 20) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'Invalid token format'
    );
  }

  // Use collection group query to find token across all users' mcpTokens subcollections
  const tokenSnapshot = await admin
    .firestore()
    .collectionGroup(MCP_TOKENS_COLLECTION)
    .where('tokenId', '==', token)
    .limit(1)
    .get();

  if (tokenSnapshot.empty) {
    throw new functions.https.HttpsError('unauthenticated', 'Token not found');
  }

  const tokenDoc = tokenSnapshot.docs[0];
  const tokenData = tokenDoc.data();
  const now = admin.firestore.Timestamp.now();

  if (tokenData.expiresAt && tokenData.expiresAt.toMillis() < now.toMillis()) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'Token has expired'
    );
  }

  // Extract userId from the document path: users/{userId}/mcpTokens/{tokenId}
  const pathParts = tokenDoc.ref.path.split('/');
  const userId = pathParts[1];

  await tokenDoc.ref.update({ lastUsedAt: now });

  return {
    userId,
    tokenId: token,
  };
}

export async function validateFirebaseIdToken(
  request: functions.https.Request
): Promise<string> {
  const authHeader = request.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'Missing or invalid Authorization header'
    );
  }

  const idToken = authHeader.substring(7);

  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    return decodedToken.uid;
  } catch {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'Invalid Firebase ID token'
    );
  }
}
