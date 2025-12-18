// Co-authored by Claude Code
import request from 'supertest';
import { NotFoundError, ForbiddenError } from '../utils';
import * as tools from '../tools';

// Mock dependencies before importing server
jest.mock('../tokenAuth', () => ({
  validateMcpToken: jest.fn(),
}));

jest.mock('../tools', () => ({
  listProjectsForUser: jest.fn(),
  getProjectForUser: jest.fn(),
  listProjectMembersForUser: jest.fn(),
  getUserById: jest.fn(),
  listTasksForProject: jest.fn(),
  getTaskForProject: jest.fn(),
  listMeetingsForProject: jest.fn(),
  getMeetingForProject: jest.fn(),
}));

jest.mock('@modelcontextprotocol/sdk/server/mcp.js', () => ({
  McpServer: jest.fn().mockImplementation(() => ({
    tool: jest.fn(),
    connect: jest.fn().mockResolvedValue(undefined),
  })),
}));

jest.mock('@modelcontextprotocol/sdk/server/streamableHttp.js', () => ({
  StreamableHTTPServerTransport: jest.fn().mockImplementation(() => ({
    handleRequest: jest.fn().mockImplementation((_req, res) => {
      res.status(200).json({ success: true });
      return Promise.resolve();
    }),
    close: jest.fn(),
  })),
}));

jest.mock('express-rate-limit', () => {
  return jest.fn(() => (_req: unknown, _res: unknown, next: () => void) => next());
});

jest.mock('firebase-functions', () => ({
  https: {
    HttpsError: class HttpsError extends Error {
      code: string;
      constructor(code: string, message: string) {
        super(message);
        this.code = code;
        this.name = 'HttpsError';
      }
    },
    onRequest: jest.fn((app) => app),
  },
  logger: {
    error: jest.fn(),
  },
}));

// Import after mocks
import { createMcpServer, app } from '../server';
import { validateMcpToken } from '../tokenAuth';
import * as functions from 'firebase-functions';

describe('server', () => {
  const mockAuth = { userId: 'test-user-123', tokenHash: 'hash123' };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createMcpServer', () => {
    it('createMcpServer_withValidAuth_returnsServerWithTools', () => {
      const server = createMcpServer(mockAuth);

      expect(server).toBeDefined();
      expect(server.tool).toHaveBeenCalledTimes(8);
    });

    it('createMcpServer_registersListProjectsTool', () => {
      const server = createMcpServer(mockAuth);

      expect(server.tool).toHaveBeenCalledWith(
        'list-projects',
        'List all projects the authenticated user is a member of',
        {},
        expect.any(Function)
      );
    });

    it('createMcpServer_registersGetProjectTool', () => {
      const server = createMcpServer(mockAuth);

      expect(server.tool).toHaveBeenCalledWith(
        'get-project',
        'Get details of a specific project by ID',
        expect.any(Object),
        expect.any(Function)
      );
    });

    it('createMcpServer_registersListTasksTool', () => {
      const server = createMcpServer(mockAuth);

      expect(server.tool).toHaveBeenCalledWith(
        'list-tasks',
        'List all tasks in a project',
        expect.any(Object),
        expect.any(Function)
      );
    });

    it('createMcpServer_registersListMeetingsTool', () => {
      const server = createMcpServer(mockAuth);

      expect(server.tool).toHaveBeenCalledWith(
        'list-meetings',
        'List all meetings in a project',
        expect.any(Object),
        expect.any(Function)
      );
    });
  });

  describe('error classes', () => {
    it('notFoundError_instanceofCheck_returnsTrue', () => {
      const error = new NotFoundError('Not found');

      expect(error instanceof NotFoundError).toBe(true);
      expect(error instanceof Error).toBe(true);
    });

    it('forbiddenError_instanceofCheck_returnsTrue', () => {
      const error = new ForbiddenError('Forbidden');

      expect(error instanceof ForbiddenError).toBe(true);
      expect(error instanceof Error).toBe(true);
    });

    it('notFoundError_notInstanceOfForbiddenError', () => {
      const error = new NotFoundError('Not found');

      expect(error instanceof ForbiddenError).toBe(false);
    });

    it('forbiddenError_notInstanceOfNotFoundError', () => {
      const error = new ForbiddenError('Forbidden');

      expect(error instanceof NotFoundError).toBe(false);
    });
  });

  describe('tool handlers', () => {
    let toolHandlers: Record<string, Function>;

    beforeEach(() => {
      toolHandlers = {};
      const mockTool = jest.fn().mockImplementation((name, _desc, _schema, handler) => {
        toolHandlers[name] = handler;
      });

      jest.mocked(require('@modelcontextprotocol/sdk/server/mcp.js').McpServer).mockImplementation(() => ({
        tool: mockTool,
        connect: jest.fn().mockResolvedValue(undefined),
      }));

      createMcpServer(mockAuth);
    });

    it('listProjectsHandler_callsToolWithUserId', async () => {
      const mockProjects = [{ id: 'proj1', name: 'Project 1' }];
      jest.mocked(tools.listProjectsForUser).mockResolvedValue(mockProjects);

      const result = await toolHandlers['list-projects']();

      expect(tools.listProjectsForUser).toHaveBeenCalledWith('test-user-123');
      expect(result.content[0].type).toBe('text');
      expect(JSON.parse(result.content[0].text)).toEqual({ projects: mockProjects });
    });

    it('getProjectHandler_callsToolWithProjectIdAndUserId', async () => {
      const mockProject = { id: 'proj1', name: 'Project 1' };
      jest.mocked(tools.getProjectForUser).mockResolvedValue(mockProject);

      const result = await toolHandlers['get-project']({ projectId: 'proj1' });

      expect(tools.getProjectForUser).toHaveBeenCalledWith('proj1', 'test-user-123');
      expect(JSON.parse(result.content[0].text)).toEqual({ project: mockProject });
    });

    it('listTasksHandler_callsToolWithProjectIdAndUserId', async () => {
      const mockTasks = [{ id: 'task1', title: 'Task 1' }];
      jest.mocked(tools.listTasksForProject).mockResolvedValue(mockTasks);

      const result = await toolHandlers['list-tasks']({ projectId: 'proj1' });

      expect(tools.listTasksForProject).toHaveBeenCalledWith('proj1', 'test-user-123');
      expect(JSON.parse(result.content[0].text)).toEqual({ tasks: mockTasks });
    });

    it('getTaskHandler_callsToolWithAllParams', async () => {
      const mockTask = { id: 'task1', title: 'Task 1' };
      jest.mocked(tools.getTaskForProject).mockResolvedValue(mockTask);

      const result = await toolHandlers['get-task']({ projectId: 'proj1', taskId: 'task1' });

      expect(tools.getTaskForProject).toHaveBeenCalledWith('proj1', 'task1', 'test-user-123');
      expect(JSON.parse(result.content[0].text)).toEqual({ task: mockTask });
    });

    it('listMeetingsHandler_callsToolWithProjectIdAndUserId', async () => {
      const mockMeetings = [{ id: 'meet1', title: 'Meeting 1' }];
      jest.mocked(tools.listMeetingsForProject).mockResolvedValue(mockMeetings);

      const result = await toolHandlers['list-meetings']({ projectId: 'proj1' });

      expect(tools.listMeetingsForProject).toHaveBeenCalledWith('proj1', 'test-user-123');
      expect(JSON.parse(result.content[0].text)).toEqual({ meetings: mockMeetings });
    });

    it('getMeetingHandler_callsToolWithAllParams', async () => {
      const mockMeeting = { id: 'meet1', title: 'Meeting 1' };
      jest.mocked(tools.getMeetingForProject).mockResolvedValue(mockMeeting);

      const result = await toolHandlers['get-meeting']({ projectId: 'proj1', meetingId: 'meet1' });

      expect(tools.getMeetingForProject).toHaveBeenCalledWith('proj1', 'meet1', 'test-user-123');
      expect(JSON.parse(result.content[0].text)).toEqual({ meeting: mockMeeting });
    });

    it('getUserHandler_callsToolWithUserId', async () => {
      const mockUser = { id: 'user1', name: 'User 1' };
      jest.mocked(tools.getUserById).mockResolvedValue(mockUser);

      const result = await toolHandlers['get-user']({ userId: 'user1' });

      expect(tools.getUserById).toHaveBeenCalledWith('user1');
      expect(JSON.parse(result.content[0].text)).toEqual({ user: mockUser });
    });

    it('listProjectMembersHandler_callsToolWithParams', async () => {
      const mockMembers = [{ id: 'member1', role: 'admin' }];
      jest.mocked(tools.listProjectMembersForUser).mockResolvedValue(mockMembers);

      const result = await toolHandlers['list-project-members']({ projectId: 'proj1' });

      expect(tools.listProjectMembersForUser).toHaveBeenCalledWith('proj1', 'test-user-123');
      expect(JSON.parse(result.content[0].text)).toEqual({ members: mockMembers });
    });
  });

  describe('express endpoint', () => {
    beforeEach(() => {
      jest.mocked(validateMcpToken).mockReset();
    });

    it('postMcp_validToken_returns200', async () => {
      jest.mocked(validateMcpToken).mockResolvedValue(mockAuth);

      const response = await request(app)
        .post('/mcp')
        .set('Authorization', 'Bearer mcp_validtoken12345678901234567890')
        .send({ method: 'tools/list' });

      expect(response.status).toBe(200);
      expect(validateMcpToken).toHaveBeenCalled();
    });

    it('postMcp_httpsError_returns401', async () => {
      const httpsError = new functions.https.HttpsError('unauthenticated', 'Invalid token');
      jest.mocked(validateMcpToken).mockRejectedValue(httpsError);

      const response = await request(app)
        .post('/mcp')
        .set('Authorization', 'Bearer invalid')
        .send({});

      expect(response.status).toBe(401);
      expect(response.body).toEqual({ error: 'Invalid token' });
    });

    it('postMcp_notFoundError_returns404', async () => {
      jest.mocked(validateMcpToken).mockRejectedValue(new NotFoundError('Resource not found'));

      const response = await request(app)
        .post('/mcp')
        .send({});

      expect(response.status).toBe(404);
      expect(response.body).toEqual({ error: 'Resource not found' });
    });

    it('postMcp_forbiddenError_returns403', async () => {
      jest.mocked(validateMcpToken).mockRejectedValue(new ForbiddenError('Access denied'));

      const response = await request(app)
        .post('/mcp')
        .send({});

      expect(response.status).toBe(403);
      expect(response.body).toEqual({ error: 'Access denied' });
    });

    it('postMcp_genericError_returns500', async () => {
      jest.mocked(validateMcpToken).mockRejectedValue(new Error('Something went wrong'));

      const response = await request(app)
        .post('/mcp')
        .send({});

      expect(response.status).toBe(500);
      expect(response.body).toEqual({ error: 'Internal server error' });
    });

    it('postMcp_nonErrorThrown_returns500', async () => {
      jest.mocked(validateMcpToken).mockRejectedValue('string error');

      const response = await request(app)
        .post('/mcp')
        .send({});

      expect(response.status).toBe(500);
      expect(response.body).toEqual({ error: 'Internal server error' });
    });

    it('postMcp_logsErrorWithContext', async () => {
      const testError = new Error('Test error');
      jest.mocked(validateMcpToken).mockRejectedValue(testError);

      await request(app)
        .post('/mcp')
        .send({});

      expect(functions.logger.error).toHaveBeenCalledWith(
        'MCP server error:',
        expect.objectContaining({
          message: 'Test error',
          path: '/mcp',
          method: 'POST',
        })
      );
    });
  });
});
