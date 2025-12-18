// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import { listMeetingsForProject, getMeetingForProject } from '../meetings';

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

describe('meetings tools', () => {
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
  const mockMeetingDoc = {
    get: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (admin.firestore as unknown as jest.Mock).mockReturnValue(mockFirestore);
    mockFirestore.collection.mockReturnValue(mockCollection);
    mockCollection.doc.mockReturnValue(mockDocRef);
    mockDocRef.collection.mockReturnValue(mockSubCollection);
    mockSubCollection.doc.mockReturnValue(mockMeetingDoc);
  });

  describe('listMeetingsForProject', () => {
    it('listMeetingsForProject_validMember_returnsMeetingsList', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user1'] }),
      });
      const mockMeetings = [
        { id: 'meeting1', data: () => ({ title: 'Standup', date: '2024-01-01' }) },
        { id: 'meeting2', data: () => ({ title: 'Review', date: '2024-01-02' }) },
      ];
      mockSubCollection.get.mockResolvedValue({ docs: mockMeetings });

      const result = await listMeetingsForProject('proj1', 'user1');

      expect(mockFirestore.collection).toHaveBeenCalledWith('projects');
      expect(mockDocRef.collection).toHaveBeenCalledWith('meetings');
      expect(result).toHaveLength(2);
    });

    it('listMeetingsForProject_projectNotFound_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({ exists: false });

      await expect(listMeetingsForProject('nonexistent', 'user1')).rejects.toThrow(
        'Project not found'
      );
    });

    it('listMeetingsForProject_notAMember_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user2'] }),
      });

      await expect(listMeetingsForProject('proj1', 'user1')).rejects.toThrow(
        'Not a member of this project'
      );
    });
  });

  describe('getMeetingForProject', () => {
    it('getMeetingForProject_validMeeting_returnsMeeting', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user1'] }),
      });
      mockMeetingDoc.get.mockResolvedValue({
        exists: true,
        id: 'meeting1',
        data: () => ({ title: 'Standup', date: '2024-01-01' }),
      });

      const result = await getMeetingForProject('proj1', 'meeting1', 'user1');

      expect(mockSubCollection.doc).toHaveBeenCalledWith('meeting1');
      expect(result).toEqual({
        id: 'meeting1',
        title: 'Standup',
        date: '2024-01-01',
      });
    });

    it('getMeetingForProject_meetingNotFound_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user1'] }),
      });
      mockMeetingDoc.get.mockResolvedValue({ exists: false });

      await expect(
        getMeetingForProject('proj1', 'nonexistent', 'user1')
      ).rejects.toThrow('Meeting not found');
    });

    it('getMeetingForProject_notAMember_throwsError', async () => {
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({ memberIds: ['user2'] }),
      });

      await expect(getMeetingForProject('proj1', 'meeting1', 'user1')).rejects.toThrow(
        'Not a member of this project'
      );
    });
  });
});
