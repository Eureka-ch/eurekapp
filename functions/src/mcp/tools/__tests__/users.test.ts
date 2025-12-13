// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import { getUserById } from '../users';

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

describe('users tools', () => {
  const mockFirestore = {
    collection: jest.fn(),
  };
  const mockCollection = {
    doc: jest.fn(),
  };
  const mockDoc = {
    get: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (admin.firestore as unknown as jest.Mock).mockReturnValue(mockFirestore);
    mockFirestore.collection.mockReturnValue(mockCollection);
    mockCollection.doc.mockReturnValue(mockDoc);
  });

  describe('getUserById', () => {
    it('getUserById_validUser_returnsUserData', async () => {
      const mockUserData = {
        displayName: 'Test User',
        email: 'test@example.com',
      };
      mockDoc.get.mockResolvedValue({
        exists: true,
        id: 'user123',
        data: () => mockUserData,
      });

      const result = await getUserById('user123');

      expect(mockFirestore.collection).toHaveBeenCalledWith('users');
      expect(mockCollection.doc).toHaveBeenCalledWith('user123');
      expect(result).toEqual({
        id: 'user123',
        displayName: 'Test User',
        email: 'test@example.com',
      });
    });

    it('getUserById_userNotFound_throwsError', async () => {
      mockDoc.get.mockResolvedValue({
        exists: false,
      });

      await expect(getUserById('nonexistent')).rejects.toThrow('User not found');
    });
  });
});
