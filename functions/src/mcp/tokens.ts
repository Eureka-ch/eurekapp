// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';
import * as crypto from 'crypto';
import { validateFirebaseIdToken } from './tokenAuth';

const MCP_TOKENS_COLLECTION = 'mcpTokens';
const DEFAULT_TTL_DAYS = 30;
const MAX_TTL_DAYS = 365;

function generateToken(): string {
  return crypto.randomBytes(32).toString('hex');
}

export const mcpCreateToken = functions.https.onRequest(async (req, res) => {
  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  try {
    const userId = await validateFirebaseIdToken(req);
    const { name, ttlDays } = req.body || {};

    const tokenName = name || 'MCP Token';
    const ttl = Math.min(Math.max(ttlDays || DEFAULT_TTL_DAYS, 1), MAX_TTL_DAYS);

    const tokenId = generateToken();
    const now = admin.firestore.Timestamp.now();
    const expiresAt = admin.firestore.Timestamp.fromMillis(
      now.toMillis() + ttl * 24 * 60 * 60 * 1000
    );

    await admin
      .firestore()
      .collection(MCP_TOKENS_COLLECTION)
      .doc(tokenId)
      .set({
        userId,
        name: tokenName,
        createdAt: now,
        expiresAt,
      });

    functions.logger.info(`MCP token created for user ${userId}`);

    res.status(200).json({
      success: true,
      data: {
        token: tokenId,
        name: tokenName,
        expiresAt: expiresAt.toDate().toISOString(),
      },
    });
  } catch (error) {
    functions.logger.error('Error creating MCP token:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});

export const mcpRevokeToken = functions.https.onRequest(async (req, res) => {
  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  try {
    const userId = await validateFirebaseIdToken(req);
    const { tokenId } = req.body || {};

    if (!tokenId) {
      res.status(400).json({ error: 'tokenId is required' });
      return;
    }

    const tokenRef = admin
      .firestore()
      .collection(MCP_TOKENS_COLLECTION)
      .doc(tokenId);
    const tokenDoc = await tokenRef.get();

    if (!tokenDoc.exists) {
      res.status(404).json({ error: 'Token not found' });
      return;
    }

    if (tokenDoc.data()?.userId !== userId) {
      res.status(403).json({ error: 'Not authorized to revoke this token' });
      return;
    }

    await tokenRef.delete();

    functions.logger.info(`MCP token ${tokenId} revoked by user ${userId}`);

    res.status(200).json({ success: true });
  } catch (error) {
    functions.logger.error('Error revoking MCP token:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});

export const mcpListTokens = functions.https.onRequest(async (req, res) => {
  if (req.method !== 'GET') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  try {
    const userId = await validateFirebaseIdToken(req);

    const tokensSnapshot = await admin
      .firestore()
      .collection(MCP_TOKENS_COLLECTION)
      .where('userId', '==', userId)
      .orderBy('createdAt', 'desc')
      .get();

    const tokens = tokensSnapshot.docs.map((doc) => ({
      tokenId: doc.id,
      name: doc.data().name,
      createdAt: doc.data().createdAt?.toDate().toISOString(),
      expiresAt: doc.data().expiresAt?.toDate().toISOString(),
      lastUsedAt: doc.data().lastUsedAt?.toDate().toISOString(),
    }));

    res.status(200).json({ success: true, data: tokens });
  } catch (error) {
    functions.logger.error('Error listing MCP tokens:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});
