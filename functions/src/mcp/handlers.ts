// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';
import { validateMcpToken } from './tokenAuth';
import {
  listProjectsForUser,
  getProjectForUser,
  listProjectMembersForUser,
  getUserById,
} from './tools';

export function serializeDoc(doc: admin.firestore.DocumentSnapshot): object {
  if (!doc.exists) return {};
  const data = doc.data()!;
  return serializeData({ id: doc.id, ...data });
}

export function serializeData(data: Record<string, unknown>): object {
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

export const mcpListProjects = functions.https.onRequest(async (req, res) => {
  try {
    const auth = await validateMcpToken(req);
    const projects = await listProjectsForUser(auth.userId);
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
  try {
    const auth = await validateMcpToken(req);
    const projectId = req.query.projectId as string;

    if (!projectId) {
      res.status(400).json({ error: 'projectId is required' });
      return;
    }

    const project = await getProjectForUser(projectId, auth.userId);
    res.status(200).json({ success: true, data: project });
  } catch (error) {
    functions.logger.error('mcpGetProject error:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else if (error instanceof Error) {
      if (error.message === 'Project not found') {
        res.status(404).json({ error: error.message });
      } else if (error.message === 'Not a member of this project') {
        res.status(403).json({ error: error.message });
      } else {
        res.status(500).json({ error: 'Internal server error' });
      }
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});

export const mcpListProjectMembers = functions.https.onRequest(
  async (req, res) => {
    try {
      const auth = await validateMcpToken(req);
      const projectId = req.query.projectId as string;

      if (!projectId) {
        res.status(400).json({ error: 'projectId is required' });
        return;
      }

      const members = await listProjectMembersForUser(projectId, auth.userId);
      res.status(200).json({ success: true, data: members });
    } catch (error) {
      functions.logger.error('mcpListProjectMembers error:', error);
      if (error instanceof functions.https.HttpsError) {
        res.status(401).json({ error: error.message });
      } else if (error instanceof Error) {
        if (error.message === 'Project not found') {
          res.status(404).json({ error: error.message });
        } else if (error.message === 'Not a member of this project') {
          res.status(403).json({ error: error.message });
        } else {
          res.status(500).json({ error: 'Internal server error' });
        }
      } else {
        res.status(500).json({ error: 'Internal server error' });
      }
    }
  }
);

export const mcpGetUser = functions.https.onRequest(async (req, res) => {
  try {
    await validateMcpToken(req);
    const userId = req.query.userId as string;

    if (!userId) {
      res.status(400).json({ error: 'userId is required' });
      return;
    }

    const user = await getUserById(userId);
    res.status(200).json({ success: true, data: user });
  } catch (error) {
    functions.logger.error('mcpGetUser error:', error);
    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else if (error instanceof Error && error.message === 'User not found') {
      res.status(404).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});
