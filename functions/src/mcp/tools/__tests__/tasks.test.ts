// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import { listTasksForProject, getTaskForProject } from '../tasks';

jest.mock('firebase-admin', () => {
  const mockTimestamp = class Timestamp {
    toDate() {
      return new Date();
    }
  };
  return {
    firestore: Object.assign(jest.fn(), {
      Timestamp: mockTimestamp,
    }),
  };
});

describe('tasks tools', () => {
  const mockFirestore = {
    collection: jest.fn(),
  };
  const mockCollection = {
    doc: jest.fn(),
  };
  const mockDocRef = {
    get: jest.fn(),
    collection: jest.fn(),
  };
  const mockSubCollection = {
    get: jest.fn(),
    doc: jest.fn(),
  };
  const mockTaskDoc = {
    get: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (admin.firestore as unknown as jest.Mock).mockReturnValue(mockFirestore);
    mockFirestore.collection.mockReturnValue(mockCollection);
    mockCollection.doc.mockReturnValue(mockDocRef);
    mockDocRef.collection.mockReturnValue(mockSubCollection);
    mockSubCollection.doc.mockReturnValue(mockTaskDoc);
  });

  describe('listTasksForProject', () => {
    it('listTasksForProject_validMember_returnsTasksList', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user1'] }),
      });
      const mockTasks = [
        { id: 'task1', data: () => ({ title: 'Task 1', status: 'pending' }) },
        { id: 'task2', data: () => ({ title: 'Task 2', status: 'done' }) },
      ];
      mockSubCollection.get.mockResolvedValue({ docs: mockTasks });

      const result = await listTasksForProject('proj1', 'user1');

      expect(mockFirestore.collection).toHaveBeenCalledWith('projects');
      expect(mockDocRef.collection).toHaveBeenCalledWith('tasks');
      expect(result).toHaveLength(2);
    });

    it('listTasksForProject_projectNotFound_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({ exists: false });

      await expect(listTasksForProject('nonexistent', 'user1')).rejects.toThrow(
        'Project not found'
      );
    });

    it('listTasksForProject_notAMember_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user2'] }),
      });

      await expect(listTasksForProject('proj1', 'user1')).rejects.toThrow(
        'Not a member of this project'
      );
    });
  });

  describe('getTaskForProject', () => {
    it('getTaskForProject_validTask_returnsTask', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user1'] }),
      });
      mockTaskDoc.get.mockResolvedValue({
        exists: true,
        id: 'task1',
        data: () => ({ title: 'Test Task', status: 'pending' }),
      });

      const result = await getTaskForProject('proj1', 'task1', 'user1');

      expect(mockSubCollection.doc).toHaveBeenCalledWith('task1');
      expect(result).toEqual({
        id: 'task1',
        title: 'Test Task',
        status: 'pending',
      });
    });

    it('getTaskForProject_taskNotFound_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user1'] }),
      });
      mockTaskDoc.get.mockResolvedValue({ exists: false });

      await expect(getTaskForProject('proj1', 'nonexistent', 'user1')).rejects.toThrow(
        'Task not found'
      );
    });

    it('getTaskForProject_notAMember_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user2'] }),
      });

      await expect(getTaskForProject('proj1', 'task1', 'user1')).rejects.toThrow(
        'Not a member of this project'
      );
    });
  });
});
