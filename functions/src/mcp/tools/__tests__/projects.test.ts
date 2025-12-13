// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import {
  listProjectsForUser,
  getProjectForUser,
  listProjectMembersForUser,
} from '../projects';

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

describe('projects tools', () => {
  const mockFirestore = {
    collection: jest.fn(),
  };
  const mockCollection = {
    where: jest.fn(),
    doc: jest.fn(),
  };
  const mockQuery = {
    get: jest.fn(),
  };
  const mockDocRef = {
    get: jest.fn(),
    collection: jest.fn(),
  };
  const mockSubCollection = {
    get: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (admin.firestore as unknown as jest.Mock).mockReturnValue(mockFirestore);
    mockFirestore.collection.mockReturnValue(mockCollection);
    mockCollection.where.mockReturnValue(mockQuery);
    mockCollection.doc.mockReturnValue(mockDocRef);
    mockDocRef.collection.mockReturnValue(mockSubCollection);
  });

  describe('listProjectsForUser', () => {
    it('listProjectsForUser_userWithProjects_returnsProjectList', async () => {
      const mockProjects = [
        { id: 'proj1', data: () => ({ name: 'Project 1', memberIds: ['user1'] }) },
        { id: 'proj2', data: () => ({ name: 'Project 2', memberIds: ['user1'] }) },
      ];
      mockQuery.get.mockResolvedValue({ docs: mockProjects });

      const result = await listProjectsForUser('user1');

      expect(mockFirestore.collection).toHaveBeenCalledWith('projects');
      expect(mockCollection.where).toHaveBeenCalledWith(
        'memberIds',
        'array-contains',
        'user1'
      );
      expect(result).toHaveLength(2);
    });

    it('listProjectsForUser_userWithNoProjects_returnsEmptyArray', async () => {
      mockQuery.get.mockResolvedValue({ docs: [] });

      const result = await listProjectsForUser('user1');

      expect(result).toEqual([]);
    });
  });

  describe('getProjectForUser', () => {
    it('getProjectForUser_validMember_returnsProject', async () => {
      const mockProjectData = {
        name: 'Test Project',
        memberIds: ['user1', 'user2'],
      };
      mockDocRef.get.mockResolvedValue({
        exists: true,
        id: 'proj1',
        data: () => mockProjectData,
      });

      const result = await getProjectForUser('proj1', 'user1');

      expect(mockCollection.doc).toHaveBeenCalledWith('proj1');
      expect(result).toEqual({
        id: 'proj1',
        name: 'Test Project',
        memberIds: ['user1', 'user2'],
      });
    });

    it('getProjectForUser_projectNotFound_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({ exists: false });

      await expect(getProjectForUser('nonexistent', 'user1')).rejects.toThrow(
        'Project not found'
      );
    });

    it('getProjectForUser_notAMember_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        id: 'proj1',
        data: () => ({ name: 'Test Project', memberIds: ['user2'] }),
      });

      await expect(getProjectForUser('proj1', 'user1')).rejects.toThrow(
        'Not a member of this project'
      );
    });
  });

  describe('listProjectMembersForUser', () => {
    it('listProjectMembersForUser_validMember_returnsMembersList', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user1'] }),
      });
      const mockMembers = [
        { id: 'member1', data: () => ({ role: 'admin' }) },
        { id: 'member2', data: () => ({ role: 'member' }) },
      ];
      mockSubCollection.get.mockResolvedValue({ docs: mockMembers });

      const result = await listProjectMembersForUser('proj1', 'user1');

      expect(mockDocRef.collection).toHaveBeenCalledWith('members');
      expect(result).toHaveLength(2);
    });

    it('listProjectMembersForUser_projectNotFound_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({ exists: false });

      await expect(listProjectMembersForUser('nonexistent', 'user1')).rejects.toThrow(
        'Project not found'
      );
    });

    it('listProjectMembersForUser_notAMember_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user2'] }),
      });

      await expect(listProjectMembersForUser('proj1', 'user1')).rejects.toThrow(
        'Not a member of this project'
      );
    });
  });
});
