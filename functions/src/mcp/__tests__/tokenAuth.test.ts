// Co-authored by Claude Code
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';
import { validateMcpToken, validateFirebaseIdToken } from '../tokenAuth';

jest.mock('firebase-admin', () => {
  const mockTimestamp = {
    now: jest.fn(() => ({ toMillis: () => Date.now() })),
  };
  return {
    firestore: Object.assign(jest.fn(), {
      Timestamp: mockTimestamp,
    }),
    auth: jest.fn(),
  };
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
  },
}));

describe('tokenAuth', () => {
  const mockFirestore = {
    collection: jest.fn(),
  };
  const mockCollection = {
    doc: jest.fn(),
  };
  const mockDocRef = {
    get: jest.fn(),
    update: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (admin.firestore as unknown as jest.Mock).mockReturnValue(mockFirestore);
    mockFirestore.collection.mockReturnValue(mockCollection);
    mockCollection.doc.mockReturnValue(mockDocRef);
  });

  describe('validateMcpToken', () => {
    it('validateMcpToken_missingAuthHeader_throwsUnauthenticated', async () => {
      const request = { headers: {} } as functions.https.Request;

      await expect(validateMcpToken(request)).rejects.toThrow(
        'Missing or invalid Authorization header'
      );
    });

    it('validateMcpToken_invalidHeaderFormat_throwsUnauthenticated', async () => {
      const request = {
        headers: { authorization: 'Basic token123' },
      } as functions.https.Request;

      await expect(validateMcpToken(request)).rejects.toThrow(
        'Missing or invalid Authorization header'
      );
    });

    it('validateMcpToken_invalidTokenFormat_throwsUnauthenticated', async () => {
      const request = {
        headers: { authorization: 'Bearer invalid_token' },
      } as functions.https.Request;

      await expect(validateMcpToken(request)).rejects.toThrow(
        'Invalid token format'
      );
    });

    it('validateMcpToken_tokenTooShort_throwsUnauthenticated', async () => {
      const request = {
        headers: { authorization: 'Bearer mcp_short' },
      } as functions.https.Request;

      await expect(validateMcpToken(request)).rejects.toThrow(
        'Invalid token format'
      );
    });

    it('validateMcpToken_tokenNotFound_throwsUnauthenticated', async () => {
      const request = {
        headers: { authorization: 'Bearer mcp_validtokenformat1234567890' },
      } as functions.https.Request;

      mockDocRef.get.mockResolvedValue({ exists: false });

      await expect(validateMcpToken(request)).rejects.toThrow('Token not found');
    });

    it('validateMcpToken_expiredToken_throwsUnauthenticated', async () => {
      const request = {
        headers: { authorization: 'Bearer mcp_validtokenformat1234567890' },
      } as functions.https.Request;

      const pastTime = Date.now() - 1000000;
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({
          userId: 'user123',
          expiresAt: { toMillis: () => pastTime },
        }),
        ref: mockDocRef,
      });

      await expect(validateMcpToken(request)).rejects.toThrow(
        'Token has expired'
      );
    });

    it('validateMcpToken_validToken_returnsAuthResult', async () => {
      const request = {
        headers: { authorization: 'Bearer mcp_validtokenformat1234567890' },
      } as functions.https.Request;

      const futureTime = Date.now() + 1000000;
      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({
          userId: 'user123',
          expiresAt: { toMillis: () => futureTime },
        }),
        ref: mockDocRef,
      });
      mockDocRef.update.mockResolvedValue({});

      const result = await validateMcpToken(request);

      expect(result.userId).toBe('user123');
      expect(result.tokenHash).toBeDefined();
      expect(mockDocRef.update).toHaveBeenCalled();
    });

    it('validateMcpToken_validTokenNoExpiry_returnsAuthResult', async () => {
      const request = {
        headers: { authorization: 'Bearer mcp_validtokenformat1234567890' },
      } as functions.https.Request;

      mockDocRef.get.mockResolvedValue({
        exists: true,
        data: () => ({
          userId: 'user123',
          expiresAt: null,
        }),
        ref: mockDocRef,
      });
      mockDocRef.update.mockResolvedValue({});

      const result = await validateMcpToken(request);

      expect(result.userId).toBe('user123');
    });
  });

  describe('validateFirebaseIdToken', () => {
    const mockAuth = {
      verifyIdToken: jest.fn(),
    };

    beforeEach(() => {
      (admin.auth as jest.Mock).mockReturnValue(mockAuth);
    });

    it('validateFirebaseIdToken_missingAuthHeader_throwsUnauthenticated', async () => {
      const request = { headers: {} } as functions.https.Request;

      await expect(validateFirebaseIdToken(request)).rejects.toThrow(
        'Missing or invalid Authorization header'
      );
    });

    it('validateFirebaseIdToken_invalidHeaderFormat_throwsUnauthenticated', async () => {
      const request = {
        headers: { authorization: 'Basic token123' },
      } as functions.https.Request;

      await expect(validateFirebaseIdToken(request)).rejects.toThrow(
        'Missing or invalid Authorization header'
      );
    });

    it('validateFirebaseIdToken_invalidToken_throwsUnauthenticated', async () => {
      const request = {
        headers: { authorization: 'Bearer invalidtoken' },
      } as functions.https.Request;

      mockAuth.verifyIdToken.mockRejectedValue(new Error('Invalid token'));

      await expect(validateFirebaseIdToken(request)).rejects.toThrow(
        'Invalid Firebase ID token'
      );
    });

    it('validateFirebaseIdToken_validToken_returnsUserId', async () => {
      const request = {
        headers: { authorization: 'Bearer validfirebasetoken' },
      } as functions.https.Request;

      mockAuth.verifyIdToken.mockResolvedValue({ uid: 'firebase-user-123' });

      const result = await validateFirebaseIdToken(request);

      expect(result).toBe('firebase-user-123');
      expect(mockAuth.verifyIdToken).toHaveBeenCalledWith('validfirebasetoken');
    });
  });
});
