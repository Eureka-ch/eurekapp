// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';
import { Response } from 'express';
import { validateMcpToken } from './tokenAuth';

function serializeDoc(doc: admin.firestore.DocumentSnapshot): object {
  if (!doc.exists) return {};
  const data = doc.data()!;
  return serializeData({ id: doc.id, ...data });
}

function serializeData(data: Record<string, unknown>): object {
  const result: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(data)) {
    if (value instanceof admin.firestore.Timestamp) {
      result[key] = value.toDate().toISOString();
    } else if (value && typeof value === 'object' && !Array.isArray(value)) {
      result[key] = serializeData(value as Record<string, unknown>);
    } else {
      result[key] = value;
    }
  }
  return result;
}

function setCorsHeaders(res: Response): void {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type, Authorization');
}

export const mcpListProjects = functions.https.onRequest(async (req, res) => {
  setCorsHeaders(res);
  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  try {
    const auth = await validateMcpToken(req);

    const projectsSnapshot = await admin
      .firestore()
      .collection('projects')
      .where('memberIds', 'array-contains', auth.userId)
      .get();

    const projects = projectsSnapshot.docs.map(serializeDoc);

    res.status(200).json({ success: true, data: projects });
  } catch (error) {
    functions.logger.error('mcpListProjects error:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});

export const mcpGetProject = functions.https.onRequest(async (req, res) => {
  setCorsHeaders(res);
  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  try {
    const auth = await validateMcpToken(req);
    const projectId = req.query.projectId as string;

    if (!projectId) {
      res.status(400).json({ error: 'projectId is required' });
      return;
    }

    const projectDoc = await admin
      .firestore()
      .collection('projects')
      .doc(projectId)
      .get();

    if (!projectDoc.exists) {
      res.status(404).json({ error: 'Project not found' });
      return;
    }

    const projectData = projectDoc.data()!;
    if (!projectData.memberIds?.includes(auth.userId)) {
      res.status(403).json({ error: 'Not a member of this project' });
      return;
    }

    res.status(200).json({ success: true, data: serializeDoc(projectDoc) });
  } catch (error) {
    functions.logger.error('mcpGetProject error:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});

export const mcpListProjectMembers = functions.https.onRequest(
  async (req, res) => {
    setCorsHeaders(res);
    if (req.method === 'OPTIONS') {
      res.status(204).send('');
      return;
    }

    try {
      const auth = await validateMcpToken(req);
      const projectId = req.query.projectId as string;

      if (!projectId) {
        res.status(400).json({ error: 'projectId is required' });
        return;
      }

      const projectDoc = await admin
        .firestore()
        .collection('projects')
        .doc(projectId)
        .get();

      if (!projectDoc.exists) {
        res.status(404).json({ error: 'Project not found' });
        return;
      }

      if (!projectDoc.data()?.memberIds?.includes(auth.userId)) {
        res.status(403).json({ error: 'Not a member of this project' });
        return;
      }

      const membersSnapshot = await admin
        .firestore()
        .collection('projects')
        .doc(projectId)
        .collection('members')
        .get();

      const members = membersSnapshot.docs.map(serializeDoc);

      res.status(200).json({ success: true, data: members });
    } catch (error) {
      functions.logger.error('mcpListProjectMembers error:', error);
      if (error instanceof functions.https.HttpsError) {
        res.status(401).json({ error: error.message });
      } else {
        res.status(500).json({ error: 'Internal server error' });
      }
    }
  }
);

export const mcpGetUser = functions.https.onRequest(async (req, res) => {
  setCorsHeaders(res);
  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  try {
    await validateMcpToken(req);
    const userId = req.query.userId as string;

    if (!userId) {
      res.status(400).json({ error: 'userId is required' });
      return;
    }

    const userDoc = await admin
      .firestore()
      .collection('users')
      .doc(userId)
      .get();

    if (!userDoc.exists) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    res.status(200).json({ success: true, data: serializeDoc(userDoc) });
  } catch (error) {
    functions.logger.error('mcpGetUser error:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});
