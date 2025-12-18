// Co-authored by Claude Code
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StreamableHTTPServerTransport } from '@modelcontextprotocol/sdk/server/streamableHttp.js';
import * as functions from 'firebase-functions';
import express, { Request, Response } from 'express';
import rateLimit from 'express-rate-limit';
import { z } from 'zod/v4';
import { validateMcpToken, McpAuthResult } from './tokenAuth';
import { NotFoundError, ForbiddenError } from './utils';
import {
  listProjectsForUser,
  getProjectForUser,
  listProjectMembersForUser,
  getUserById,
  listTasksForProject,
  getTaskForProject,
  listMeetingsForProject,
  getMeetingForProject,
} from './tools';

export function createMcpServer(auth: McpAuthResult): McpServer {
  const server = new McpServer({
    name: 'eureka-mcp-server',
    version: '1.0.0',
  });

  server.tool(
    'list-projects',
    'List all projects the authenticated user is a member of',
    {},
    async () => {
      const projects = await listProjectsForUser(auth.userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ projects }, null, 2) }],
      };
    }
  );

  server.tool(
    'get-project',
    'Get details of a specific project by ID',
    { projectId: z.string().describe('The project ID') },
    async ({ projectId }: { projectId: string }) => {
      const project = await getProjectForUser(projectId, auth.userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ project }, null, 2) }],
      };
    }
  );

  server.tool(
    'list-project-members',
    'List all members of a specific project',
    { projectId: z.string().describe('The project ID') },
    async ({ projectId }: { projectId: string }) => {
      const members = await listProjectMembersForUser(projectId, auth.userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ members }, null, 2) }],
      };
    }
  );

  server.tool(
    'get-user',
    'Get user information by ID',
    { userId: z.string().describe('The user ID') },
    async ({ userId }: { userId: string }) => {
      const user = await getUserById(userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ user }, null, 2) }],
      };
    }
  );

  server.tool(
    'list-tasks',
    'List all tasks in a project',
    { projectId: z.string().describe('The project ID') },
    async ({ projectId }: { projectId: string }) => {
      const tasks = await listTasksForProject(projectId, auth.userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ tasks }, null, 2) }],
      };
    }
  );

  server.tool(
    'get-task',
    'Get details of a specific task',
    {
      projectId: z.string().describe('The project ID'),
      taskId: z.string().describe('The task ID'),
    },
    async ({ projectId, taskId }: { projectId: string; taskId: string }) => {
      const task = await getTaskForProject(projectId, taskId, auth.userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ task }, null, 2) }],
      };
    }
  );

  server.tool(
    'list-meetings',
    'List all meetings in a project',
    { projectId: z.string().describe('The project ID') },
    async ({ projectId }: { projectId: string }) => {
      const meetings = await listMeetingsForProject(projectId, auth.userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ meetings }, null, 2) }],
      };
    }
  );

  server.tool(
    'get-meeting',
    'Get details of a specific meeting',
    {
      projectId: z.string().describe('The project ID'),
      meetingId: z.string().describe('The meeting ID'),
    },
    async ({ projectId, meetingId }: { projectId: string; meetingId: string }) => {
      const meeting = await getMeetingForProject(projectId, meetingId, auth.userId);
      return {
        content: [{ type: 'text', text: JSON.stringify({ meeting }, null, 2) }],
      };
    }
  );

  return server;
}

export const app = express();
app.use(express.json());

// Rate limiting: 30 requests per minute per IP
const limiter = rateLimit({
  windowMs: 60 * 1000,
  max: 30,
  message: { error: 'Too many requests, please try again later' },
  standardHeaders: true,
  legacyHeaders: false,
});

app.use('/mcp', limiter);

app.post('/mcp', async (req: Request, res: Response) => {
  try {
    const auth = await validateMcpToken(req as unknown as functions.https.Request);
    const server = createMcpServer(auth);

    const transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: undefined,
      enableJsonResponse: true,
    });

    res.on('close', () => transport.close());
    await server.connect(transport);
    await transport.handleRequest(req, res, req.body);
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error';
    const errorStack = error instanceof Error ? error.stack : undefined;

    functions.logger.error('MCP server error:', {
      message: errorMessage,
      stack: errorStack,
      path: req.path,
      method: req.method,
    });

    if (error instanceof functions.https.HttpsError) {
      res.status(401).json({ error: error.message });
    } else if (error instanceof NotFoundError) {
      res.status(404).json({ error: error.message });
    } else if (error instanceof ForbiddenError) {
      res.status(403).json({ error: error.message });
    } else {
      res.status(500).json({ error: 'Internal server error' });
    }
  }
});

export const mcpServer = functions.https.onRequest(app);
