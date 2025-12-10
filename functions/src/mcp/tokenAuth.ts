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

  const tokenDoc = await admin
    .firestore()
    .collection(MCP_TOKENS_COLLECTION)
    .doc(token)
    .get();

  if (!tokenDoc.exists) {
    throw new functions.https.HttpsError('unauthenticated', 'Token not found');
  }

  const tokenData = tokenDoc.data()!;
  const now = admin.firestore.Timestamp.now();

  if (tokenData.expiresAt && tokenData.expiresAt.toMillis() < now.toMillis()) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'Token has expired'
    );
  }

  await tokenDoc.ref.update({ lastUsedAt: now });

  return {
    userId: tokenData.userId,
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
